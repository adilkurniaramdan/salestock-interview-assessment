package models.entities.order

import play.api.libs.json.Json

/**
  * Created by adildramdan on 11/19/17.
  */
case class OrderShipment(name:  String = "JNE")

object OrderShipment {
  implicit val orderShipmentJsonFormat = Json.format[OrderShipment]
}

