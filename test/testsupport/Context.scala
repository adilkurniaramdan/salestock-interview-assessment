package testsupport

import java.util.UUID

import com.google.inject.AbstractModule
import com.mohiva.play.silhouette.api.{Environment, LoginInfo}
import com.mohiva.play.silhouette.test._
import models.entities.User
import modules.BootModule
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.concurrent.Execution.Implicits._
import services.application.RandomService
import utils.auth.DefaultEnv
import utils.auth.Roles.{AdminRole, UserRole}

/**
  * Created by adildramdan on 11/19/17.
  */
trait Context {

  /**
    * A fake Guice module.
    */
  class FakeModule extends AbstractModule with ScalaModule {
    def configure() = {
      bind[Environment[DefaultEnv]].toInstance(env)
    }
  }

  /**
    * Identity.
    */
  val user = User(
    userID    = UUID.randomUUID(),
    loginInfo = LoginInfo("user", "user@user.com"),
    email     = "user@user.com",
    role      = UserRole
  )
  val admin = User(
    userID    = UUID.randomUUID(),
    loginInfo = LoginInfo("admin", "admin@admin.com"),
    email     = "admin@admin.com",
    role      = AdminRole
  )

  /**
    * A Silhouette fake environment.
    */
  implicit val env: Environment[DefaultEnv] = new FakeEnvironment[DefaultEnv](Seq(admin.loginInfo -> admin, user.loginInfo -> user))

  /**
    * The application.
    */
  lazy val application = new GuiceApplicationBuilder()
    .loadConfig(env => Configuration.load(env))
    .disable[BootModule]
    .overrides(bind[RandomService].to(new FakeRandomServiceImpl("THIS_IS_RANDOM_RESULT")))
    .overrides(new FakeModule)
    .build()
}
