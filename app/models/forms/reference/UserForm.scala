package models.forms.reference

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

/**
  * Created by adildramdan on 11/19/17.
  */
object UserForm {

  val signInForm = Form(
    mapping(
      "email"       -> email,
      "password"    -> nonEmptyText,
      "rememberMe"  -> boolean
    )(SignIn.apply)(SignIn.unapply)
  )
  case class SignIn(email: String, password: String, rememberMe: Boolean)
  object SignIn {
    implicit val signInJsonFormat = Json.format[SignIn]
  }
}
