package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.entities.order._
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import models.forms.order.OrderForm
import play.api.Configuration
import play.api.libs.json.Json._
import play.api.mvc.{AbstractController, ControllerComponents}
import services.application.RandomService
import utils.Constants.ErrorCode
import utils.ResponseUtil

import scala.concurrent.ExecutionContext
import scala.concurrent.Future.{successful => future}
import scala.concurrent.duration._

/**
  * Created by adildramdan on 11/19/17.
  */
@Singleton
class OrderController @Inject()(cc                      : ControllerComponents,
                               configuration            : Configuration,
                               responseUtil             : ResponseUtil,
                               randomService            : RandomService,
                               @Named(OrderActor.Name)
                               orderActor               : ActorRef)(implicit ec: ExecutionContext, m: Materializer) extends AbstractController(cc) {
  implicit val timeout  = Timeout(100 seconds)


  def submit() = Action.async(parse.json)(implicit r =>
    r.body.validate[OrderForm.Submit].fold(responseUtil.error(classOf[OrderForm.Submit]), data =>
      (orderActor ? Submit(generateOrderId(), "USER_ID", data.coupon, data.payment, data.info))
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

  def requestVerification() = Action.async(parse.json)(implicit r =>
    r.body.validate[OrderForm.RequestVerification].fold(responseUtil.error(classOf[OrderForm.RequestVerification]), data =>
      (orderActor ? RequestVerification(data.orderId, data.paymentProof))
        .mapTo[ResponseVerification]
        .map(_ => Ok)
    )
  )

  def verify() = Action.async(parse.json)(implicit r =>
    r.body.validate[OrderForm.Verify].fold(responseUtil.error(classOf[OrderForm.Verify]), data =>
      (orderActor ? Verify(data.orderId))
        .mapTo[Verified]
        .map(_ => Ok)
    )
  )

  def cancel() = Action.async(parse.json)(implicit r =>
    r.body.validate[OrderForm.Cancel].fold(responseUtil.error(classOf[OrderForm.Cancel]), data =>
      (orderActor ? Cancel(data.orderId))
        .mapTo[Canceled]
        .map(_ => Ok)
    )
  )

  def requestShipment() = Action.async(parse.json)(implicit r =>
    r.body.validate[OrderForm.RequestShipment].fold(responseUtil.error(classOf[OrderForm.RequestShipment]), data =>
      (orderActor ? RequestShipment(data.orderId, data.shipment))
        .mapTo[Shipped]
        .map(_ => Ok)
    )
  )

  def finish() = Action.async(parse.json)(implicit r =>
    r.body.validate[OrderForm.Finish].fold(responseUtil.error(classOf[OrderForm.Finish]), data =>
      (orderActor ? Finish(data.orderId))
        .mapTo[Finished]
        .map(_ => Ok)
    )
  )

  def get() = Action.async(parse.empty) (implicit r =>
    (orderActor ? GetOrder)
      .mapTo[ResponseOrder]
      .map(_.order)
      .map(toJson(_))
      .map(Ok(_))
  )
  
  
  private def generateOrderId() = {
    randomService.randomAlphaNumericString(10)
  }
  
  
  
}
