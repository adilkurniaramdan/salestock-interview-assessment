package modules

import com.google.inject.AbstractModule
import play.api.{Configuration, Environment}
import repositories.reference.{CouponRepository, CouponRepositoryMemory, ProductRepository, ProductRepositoryMemory}

/**
  * Created by adildramdan on 11/19/17.
  */
case class RepositoryModule(environment: Environment, config: Configuration) extends AbstractModule {
  def configure(): Unit = {
    bind(classOf[CouponRepository]).to(classOf[CouponRepositoryMemory])
    bind(classOf[ProductRepository]).to(classOf[ProductRepositoryMemory])
  }
}
