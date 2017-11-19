package services.entities.reference

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import models.entities.User
import repositories.reference.UserRepository

import scala.concurrent.Future

/**
  * Created by adildramdan on 11/19/17.
  */
trait UserService extends IdentityService[User] {
}

class UserServiceImpl @Inject() (userRepository: UserRepository) extends UserService {
  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] =
    userRepository.find(loginInfo)
}