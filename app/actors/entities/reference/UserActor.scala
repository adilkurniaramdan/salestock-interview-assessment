package actors.entities.reference

import java.util.UUID
import javax.inject.Inject

import actors.Command
import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.entities.User
import repositories.reference.UserRepository
import utils.auth.Roles.Role

import scala.concurrent.ExecutionContext
import scala.concurrent.Future.{successful => future}

/**
  * Created by adildramdan on 11/17/17.
  */
class UserActor @Inject()(userRepository        : UserRepository,
                          passwordHasherRegistry: PasswordHasherRegistry,
                          authInfoRepository    : AuthInfoRepository)(implicit ec: ExecutionContext) extends Actor with ActorLogging {
  import UserActor._
  def receive = LoggingReceive {
    case m: Create  =>
      create(m.email, m.password, m.role)
  }

  private def create(email: String, password: String, role: Role) = {
    val loginInfo = LoginInfo(CredentialsProvider.ID, email)
    val authInfo  = passwordHasherRegistry.current.hash(password)
    val user      = User(
      userID    = UUID.randomUUID(),
      loginInfo = loginInfo,
      email     = email,
      role      = role
    )
    for {
      user      <- userRepository.save(user)
      authInfo  <- authInfoRepository.add(loginInfo, authInfo)
    } yield {}
  }
}


object UserActor {
  final val Name  = "user-actor"

  case class Create(email: String, password: String, role: Role) extends Command

}
