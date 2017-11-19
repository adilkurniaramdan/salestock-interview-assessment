package modules

import com.google.inject.AbstractModule
import play.api.{Configuration, Environment}
import services.application.BootService

/**
  * Created by adildramdan on 11/19/17.
  */
case class BootModule(environment: Environment, config: Configuration) extends AbstractModule {
  def configure(): Unit = {
    bind(classOf[BootService]).asEagerSingleton()
  }
}
