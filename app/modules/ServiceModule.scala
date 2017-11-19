package modules

import com.google.inject.AbstractModule
import play.api.{Configuration, Environment}
import services.application.{RandomService, RandomServiceImpl}

/**
  * Created by adildramdan on 11/19/17.
  */
case class ServiceModule(environment: Environment, config: Configuration) extends AbstractModule {
  def configure(): Unit = {
    bind(classOf[RandomService]).to(classOf[RandomServiceImpl])
  }
}
