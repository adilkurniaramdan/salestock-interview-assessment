package utils

/**
  * Created by adildramdan on 11/19/17.
  */
import javax.inject.Inject

import play.api.i18n.{I18nSupport, Langs, Messages, MessagesImpl}
import play.api.libs.json.Json._
import play.api.libs.json.{JsPath, JsValue, JsonValidationError}
import play.api.mvc.{AbstractController, ControllerComponents, Result}
import play.api.{Configuration, Logger}
import utils.Constants.ErrorCode

import scala.concurrent.Future.{successful => future}
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by adildramdan on 10/27/17.
  */
class ResponseUtil @Inject() (components            : ControllerComponents,
                              langs                 : Langs,
                              configuration         : Configuration)(implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport {
  implicit val lang             = langs.availables.head
  implicit val messagesProvider = MessagesImpl(lang, messagesApi)

  def errorForm(errors: JsValue) =
    future(BadRequest(obj("code" -> ErrorCode.JsonError, "errors" -> errors)))

  def internalServerError(code: String, msg: String) =
    InternalServerError(obj("code" -> code, "message" -> Messages(msg)))

  def badRequest(code: String, msg: String) =
    BadRequest(obj("code" -> code, "message" -> Messages(msg)))

  def futureInternalServerError(code: String, msg: String) =
    future(InternalServerError(obj("code" -> code, "message" -> Messages(msg))))

  def futureBadRequest(code: String, msg: String) =
    future(BadRequest(obj("code" -> code, "message" -> Messages(msg))))

  def error[T](t : Class[T]) : Seq[(JsPath, Seq[JsonValidationError])] => Future[Result] = (errors) => {
    Logger.error(s"Failed to insert  ${t.getName} invalid json format: ${errors.flatMap(_._2.toList).map(_.message)}")
    futureBadRequest(ErrorCode.InvalidData, s"Invalid json format: ${errors.flatMap(_._2.toList).map(_.message)}")
  }

  def option[T](implicit w: play.api.libs.json.Writes[T]) =
    (r: Option[T]) => r
      .map(toJson(_))
      .map(Ok(_))
      .getOrElse(BadRequest)

  def boolean =
    (r: Boolean) =>
      r match {
        case true   => Ok
        case false  => BadRequest
      }
}

