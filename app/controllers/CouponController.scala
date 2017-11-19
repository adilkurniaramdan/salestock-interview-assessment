package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.entities.reference.CouponActor
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import com.mohiva.play.silhouette.api.Silhouette
import models.dto.DtoMapperFormats._
import models.dto.Page
import models.dto.reference.CouponDto
import models.entities.reference.Coupon
import models.forms.reference.CouponForm
import play.api.Configuration
import play.api.libs.json.Json._
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.auth.Roles.{AdminRole, UserRole}
import utils.auth.{DefaultEnv, WithRole}
import utils.{Mapper, ResponseUtil}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * Created by adildramdan on 11/19/17.
  */
@Singleton
class CouponController @Inject()(cc                      : ControllerComponents,
                                 silhouette              : Silhouette[DefaultEnv],
                                 configuration           : Configuration,
                                 responseUtil            : ResponseUtil,
                                 @Named(CouponActor.Name)
                                 couponActor             : ActorRef)(implicit ec: ExecutionContext, m: Materializer) extends AbstractController(cc) {
  implicit val timeout  = Timeout(100 seconds)

  def create() =
    silhouette.SecuredAction(WithRole(AdminRole)).async(parse.json)(implicit r =>
      r.body.validate[CouponForm.Create].fold(responseUtil.error(classOf[CouponForm.Create]), data =>
        (couponActor ? CouponActor.Create(data.name, data.description, data.amount, data.rate, data.qty, data.start, data.end))
          .mapTo[CouponActor.Created]
          .map(_.coupon)
          .map(Mapper.map[Coupon, CouponDto])
          .map(toJson(_))
          .map(Created(_))
      )
    )

  def update(id: Long) =
    silhouette.SecuredAction(WithRole(AdminRole)).async(parse.json)(implicit r =>
      r.body.validate[CouponForm.Update].fold(responseUtil.error(classOf[CouponForm.Update]), data =>
        (couponActor ? CouponActor.Update(id, data.name, data.description, data.amount, data.rate, data.qty, data.start, data.end))
          .mapTo[CouponActor.Updated]
          .map(_.coupon)
          .map(Mapper.map[Coupon, CouponDto])
          .map(toJson(_))
          .map(Ok(_))
      )
    )

  def get(id: Long) =
    silhouette.SecuredAction(WithRole(AdminRole, UserRole)).async(parse.empty)(implicit r =>
      (couponActor ? CouponActor.Get(id))
        .mapTo[CouponActor.GetResponse]
        .map(_.coupon)
        .map(_.map(Mapper.map[Coupon, CouponDto]))
        .map(responseUtil.option)
    )

  def delete(id: Long) =
    silhouette.SecuredAction(WithRole(AdminRole, UserRole)).async(parse.empty)(implicit r =>
      (couponActor ? CouponActor.Delete(id))
        .mapTo[CouponActor.Deleted]
        .map(_.success)
        .map(responseUtil.boolean)
    )

  def page(page: Int, size: Int, sort: String, sortBy: String, filter: String) =
    silhouette.SecuredAction(WithRole(AdminRole, UserRole)).async(parse.empty)(implicit r =>
      (couponActor ? CouponActor.RequestPage(page, size, sort, sortBy, filter))
        .mapTo[CouponActor.ResponsePage]
        .map(response =>
          Page(response.data.map(Mapper.map[Coupon, CouponDto]), page, size, sort, sortBy, response.total, filter)
        )
        .map(toJson(_))
        .map(Ok(_))
    )
}
