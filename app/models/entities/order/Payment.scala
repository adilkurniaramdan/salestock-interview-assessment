package models.entities.order

import play.api.libs.json.Json

/**
  * Created by adildramdan on 11/19/17.
  */
case class Payment(method: String, name: Option[String] = None)

object Payment {
  implicit val paymentJsonFormat = Json.format[Payment]
}
