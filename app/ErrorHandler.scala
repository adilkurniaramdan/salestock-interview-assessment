/**
  * Created by adildramdan on 11/19/17.
  */

import javax.inject.{Inject, Singleton}

import exceptions.ObjectNotFoundException
import play.api.http.HttpErrorHandler
import play.api.mvc.Results._
import play.api.mvc._
import utils.Constants.ErrorCode
import utils.ResponseUtil

import scala.concurrent._

@Singleton
class ErrorHandler @Inject()(responseUtil: ResponseUtil) extends HttpErrorHandler {

  def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    Future.successful(
      Status(statusCode)("A client error occurred: " + message)
    )
  }

  def onServerError(request: RequestHeader, ex: Throwable) = {
    ex match {
      case e : ObjectNotFoundException    =>
        responseUtil.futureBadRequest(ErrorCode.InvalidData, e.message)
      case e : Exception  =>
        responseUtil.futureInternalServerError(ErrorCode.OtherError, e.getMessage)
    }
  }
}