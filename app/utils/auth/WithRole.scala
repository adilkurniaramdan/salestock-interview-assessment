package utils.auth

import com.mohiva.play.silhouette.api.Authorization
import models.entities.User
import play.api.mvc.Request
import utils.auth.Roles.Role

import scala.concurrent.Future
import scala.concurrent.Future.{successful => future}

/**
  * Created by adildramdan on 5/26/17.
  */
case class WithRole(roles: Role*) extends Authorization[User, DefaultEnv#A] {

  override def isAuthorized[B](user: User, authenticator: DefaultEnv#A)(implicit request: Request[B]): Future[Boolean] =
    future(roles.contains(user.role))
}
