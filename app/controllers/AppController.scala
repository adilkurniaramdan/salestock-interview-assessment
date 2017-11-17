package controllers

import java.lang.management.ManagementFactory
import javax.inject._

import akka.actor.ActorSystem
import akka.stream._
import models.dto.AppInfo
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.libs.json.Json._
import play.api.mvc._

import scala.concurrent.ExecutionContext
import scala.concurrent.Future.{successful => future}
import scala.concurrent.duration._
/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class AppController @Inject()(cc                      : ControllerComponents,
                              configuration           : Configuration,
                              actorSystem             : ActorSystem)(implicit ec: ExecutionContext, m: Materializer) extends AbstractController(cc) {

  val config  = configuration.underlying
  val name    = config.as[String]("app.info.name")
  val version = config.as[String]("app.info.version")

  private def getUptime = Duration(ManagementFactory.getRuntimeMXBean.getUptime, MILLISECONDS).toSeconds

  def index = Action.async {
    val info = AppInfo(name, version, getUptime)
    future(info)
      .map(toJson(_))
      .map(Ok(_))
  }
}
