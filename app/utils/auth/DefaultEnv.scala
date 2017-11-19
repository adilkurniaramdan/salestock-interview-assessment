package utils.auth

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import models.entities.User

/** The default Silhouette Environment.
  */
trait DefaultEnv extends Env {

  /** Identity
    */
  type I = User

  /** Authenticator used for identification.
    */
  type A = JWTAuthenticator

}