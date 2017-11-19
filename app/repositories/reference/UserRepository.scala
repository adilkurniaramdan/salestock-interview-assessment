package repositories.reference

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.entities.User
import repositories.reference.UserRepositoryImpl._

import scala.collection.mutable
import scala.concurrent.Future

/**
  * Created by adildramdan on 11/19/17.
  */
trait UserRepository {
  def find(loginInfo: LoginInfo): Future[Option[User]]

  def save(user: User): Future[User]
}
class UserRepositoryImpl extends UserRepository {

  def find(loginInfo: LoginInfo) = Future.successful(
    users.find { case (id, user) => user.loginInfo == loginInfo }.map(_._2)
  )

  def save(user: User) = {
    users += (user.userID -> user)
    Future.successful(user)
  }
}

object UserRepositoryImpl {
  val users: mutable.HashMap[UUID, User] = mutable.HashMap()
}
