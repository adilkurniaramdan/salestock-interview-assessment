package models.dto

import play.api.libs.json.Json

/**
  * Created by adildramdan on 5/29/17.
  */
case class Token(token: String)

object Token {
  implicit val tokenJsonFormatter = Json.format[Token]
}

