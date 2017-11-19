package services.application

import javax.inject.{Inject, Named, Singleton}

import actors.entities.reference.{CouponActor, ProductActor, UserActor}
import akka.actor.ActorRef
import utils.Constants.Rate
import utils.auth.Roles.{AdminRole, UserRole}
import utils.helpers.JodaHelper

import scala.concurrent.ExecutionContext

/**
  * Created by adildramdan on 11/19/17.
  */
class BootService @Inject()(@Named(UserActor.Name)
                            userActor   : ActorRef,
                            @Named(ProductActor.Name)
                            productActor: ActorRef,
                            @Named(CouponActor.Name)
                            couponActor : ActorRef)(implicit ec: ExecutionContext){

  userActor ! UserActor.Create("user@user.com", "123456", UserRole)
  userActor ! UserActor.Create("admin@admin.com", "123456", AdminRole)

  productActor ! ProductActor.Create("Item 1", "Item 1 Description", 100, 1000)
  productActor ! ProductActor.Create("Item 2", "Item 2 Description", 100, 1000)
  productActor ! ProductActor.Create("Item 3", "Item 3 Description", 100, 1000)
  productActor ! ProductActor.Create("Item 4", "Item 4 Description", 100, 1000)

  couponActor  ! CouponActor.Create(
    "Coupon 1", "Coupon 1 Description", 1000, Rate.Nominal, 100, JodaHelper.localDateParse("01/01/2017"), JodaHelper.localDateParse("31/12/2018")
  )

}
