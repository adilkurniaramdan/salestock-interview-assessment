package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.entities.order.{GetOrderById, _}
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import com.mohiva.play.silhouette.api.Silhouette
import models.forms.order.OrderForm
import play.api.Configuration
import play.api.libs.json.Json._
import play.api.mvc.{AbstractController, ControllerComponents}
import services.application.RandomService
import utils.Constants.ErrorCode
import utils.ResponseUtil
import utils.auth.Roles.{AdminRole, UserRole}
import utils.auth.{DefaultEnv, WithRole}

import scala.concurrent.ExecutionContext
import scala.concurrent.Future.{successful => future}
import scala.concurrent.duration._

/**
  * Created by adildramdan on 11/19/17.
  */
@Singleton
class OrderController @Inject()(cc                      : ControllerComponents,
                                silhouette              : Silhouette[DefaultEnv],
                               configuration            : Configuration,
                               responseUtil             : ResponseUtil,
                               randomService            : RandomService,
                               @Named(OrderActor.Name)
                               orderActor               : ActorRef)(implicit ec: ExecutionContext, m: Materializer) extends AbstractController(cc) {
  implicit val timeout  = Timeout(100 seconds)


  def submit() =
    silhouette.SecuredAction(WithRole(UserRole)).async(parse.json)(implicit r =>
      r.body.validate[OrderForm.Submit].fold(responseUtil.error(classOf[OrderForm.Submit]), data =>
        (orderActor ? Submit(generateOrderId(), r.identity.email, data.coupon, data.payment, data.info))
          .map {
            case m: ItemNotAvailable  =>
              responseUtil.badRequest(ErrorCode.InvalidData, m.reason)
            case m: CouponNotValid    =>
              responseUtil.badRequest(ErrorCode.InvalidData, m.reason)
            case m: Submitted         =>
              Ok
          }
      )
    )

  def requestVerification() =
    silhouette.SecuredAction(WithRole(UserRole)).async(parse.json)(implicit r =>
      r.body.validate[OrderForm.RequestVerification].fold(responseUtil.error(classOf[OrderForm.RequestVerification]), data =>
        (orderActor ? RequestVerification(data.orderId, data.paymentProof))
          .mapTo[ResponseVerification]
          .map(_ => Ok)
      )
    )

  def verify() =
    silhouette.SecuredAction(WithRole(AdminRole)).async(parse.json)(implicit r =>
      r.body.validate[OrderForm.Verify].fold(responseUtil.error(classOf[OrderForm.Verify]), data =>
        (orderActor ? Verify(data.orderId))
          .mapTo[Verified]
          .map(_ => Ok)
      )
    )

  def cancel() =
    silhouette.SecuredAction(WithRole(AdminRole)).async(parse.json)(implicit r =>
      r.body.validate[OrderForm.Cancel].fold(responseUtil.error(classOf[OrderForm.Cancel]), data =>
        (orderActor ? Cancel(data.orderId))
          .mapTo[Canceled]
          .map(_ => Ok)
      )
    )

  def requestShipment() =
    silhouette.SecuredAction(WithRole(AdminRole)).async(parse.json)(implicit r =>
      r.body.validate[OrderForm.RequestShipment].fold(responseUtil.error(classOf[OrderForm.RequestShipment]), data =>
        (orderActor ? RequestShipment(data.orderId, data.shipment))
          .mapTo[Shipped]
          .map(_ => Ok)
      )
    )

  def finish() =
    silhouette.SecuredAction(WithRole(AdminRole)).async(parse.json)(implicit r =>
      r.body.validate[OrderForm.Finish].fold(responseUtil.error(classOf[OrderForm.Finish]), data =>
        (orderActor ? Finish(data.orderId))
          .mapTo[Finished]
          .map(_ => Ok)
      )
    )

  def getAll() =
    silhouette.SecuredAction(WithRole(AdminRole)).async(parse.empty)(implicit r =>
      (orderActor ? GetOrder)
        .mapTo[ResponseOrder]
        .map(_.order)
        .map(toJson(_))
        .map(Ok(_))
    )

  def get(id: String) =
    silhouette.SecuredAction(WithRole(AdminRole, UserRole)).async(parse.empty)(implicit r =>
      (orderActor ? GetOrderById(id))
        .mapTo[ResponseOrderOpt]
        .map(_.order)
        .map(responseUtil.option)
    )

  def getByShippingId(shippingId: String) =
    silhouette.SecuredAction(WithRole(AdminRole, UserRole)).async(parse.empty)(implicit r =>
      (orderActor ? GetOrderByShippingId(shippingId))
        .mapTo[ResponseOrderOpt]
        .map(_.order)
        .map(responseUtil.option)
    )

  def getByUser() =
    silhouette.SecuredAction(WithRole(UserRole)).async(parse.empty)(implicit r =>
      (orderActor ? GetOrderByUser(r.identity.email))
        .mapTo[ResponseOrder]
        .map(_.order)
        .map(toJson(_))
        .map(Ok(_))
    )


  private def generateOrderId() = {
    randomService.randomAlphaNumericString(10)
  }
  
  
  
}
