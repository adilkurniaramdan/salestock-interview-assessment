package modules

import actors.entities.cart.CartManager
import actors.entities.order.{OrderActor, ShipmentActor}
import actors.entities.reference.{CouponActor, ProductActor, UserActor}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

/**
  * Created by adildramdan on 11/19/17.
  */
class ActorModule extends AbstractModule with AkkaGuiceSupport {
  def configure() = {
    bindActor[CouponActor]            (CouponActor.Name)
    bindActor[ProductActor]           (ProductActor.Name)
    bindActor[CartManager]            (CartManager.Name)
    bindActor[OrderActor]             (OrderActor.Name)
    bindActor[ShipmentActor]          (ShipmentActor.Name)
    bindActor[UserActor]              (UserActor.Name)
  }
}
