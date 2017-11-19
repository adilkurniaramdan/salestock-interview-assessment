package modules

import actors.entities.reference.{CouponActor, ProductActor}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

/**
  * Created by adildramdan on 11/19/17.
  */
class ActorModule extends AbstractModule with AkkaGuiceSupport {
  def configure() = {
    bindActor[CouponActor]            (CouponActor.Name)
    bindActor[ProductActor]           (ProductActor.Name)
  }
}
