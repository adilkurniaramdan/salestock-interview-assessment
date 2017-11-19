package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.{Clock, Credentials}
import com.mohiva.play.silhouette.api.{LoginEvent, LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.dto.Token
import models.forms.reference.UserForm
import net.ceedubs.ficus.Ficus._
import org.slf4j.LoggerFactory
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.libs.json.Json._
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import services.entities.reference.UserService
import utils.Constants.ErrorCode
import utils.ResponseUtil
import utils.auth.DefaultEnv

import scala.concurrent.ExecutionContext
import scala.concurrent.Future.{successful => future}
import scala.concurrent.duration._

/**
  * Created by adildramdan on 10/27/17.
  */
class AuthController @Inject()(components            : ControllerComponents,
                               val silhouette        : Silhouette[DefaultEnv],
                               userService           : UserService,
                               credentialsProvider   : CredentialsProvider,
                               configuration         : Configuration,
                               clock                 : Clock,
                               responseUtil          : ResponseUtil)(implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport {
  final val log = LoggerFactory.getLogger(classOf[AuthController])

  def signIn = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    UserForm.signInForm.bindFromRequest.fold(
      form => responseUtil.errorForm(form.errorsAsJson),
      data => {
        val credentials = Credentials(data.email, data.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          userService
            .retrieve(loginInfo)
            .flatMap {
              case Some(user) =>
                val c = configuration.underlying
                silhouette.env.authenticatorService.create(loginInfo).map {
                  case authenticator if data.rememberMe =>
                    authenticator.copy(
                      expirationDateTime  = clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
                      idleTimeout         = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout")
                    )
                  case authenticator => authenticator
                }
                .flatMap { authenticator =>
                  silhouette.env.eventBus.publish(LoginEvent(user, request))
                  silhouette.env.authenticatorService.init(authenticator).map { token =>
                    Token(token)
                  }
                  .map(toJson(_))
                  .map(Ok(_))
                }
              case None =>
                responseUtil.futureBadRequest(ErrorCode.AuthenticationFailed, "user.error.invalid_username_or_password")
            }
        }.recover {
          case _: ProviderException =>
            responseUtil.badRequest(ErrorCode.AuthenticationFailed, "user.error.invalid_username_or_password")
        }
      }
    )
  }

  def signOut = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, Ok)
  }
}

