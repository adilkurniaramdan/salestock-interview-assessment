package models.entities.order

import play.api.libs.json.Json

/**
  * Created by adildramdan on 11/19/17.
  */
case class OrderInformation(name        : String,
                            phone       : String,
                            email       : String,
                            address     : String)

object OrderInformation {
  implicit val orderInformationJsonFormat = Json.format[OrderInformation]
}
