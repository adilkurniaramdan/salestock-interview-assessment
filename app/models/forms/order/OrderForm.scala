package models.forms.order

import models.entities.order.{OrderInformation, OrderShipment, Payment}
import play.api.libs.json.Json

/**
  * Created by adildramdan on 11/19/17.
  */
object OrderForm {

  case class Submit(coupon: Option[String], payment: Payment, info: OrderInformation)

  object Submit {
    implicit val submitJsonFormat = Json.format[Submit]
  }

  case class RequestVerification(orderId: String, paymentProof: String)

  object RequestVerification {
    implicit val requestVerificationJsonFormat = Json.format[RequestVerification]
  }

  case class Verify(orderId: String)

  object Verify {
    implicit val verifyJsonFormat              = Json.format[Verify]
  }

  case class RequestShipment(orderId: String, shipment: OrderShipment)

  object RequestShipment{
    implicit val requestShipmentJsonFormat      = Json.format[RequestShipment]
  }


  case class Finish(orderId: String)

  object Finish{
    implicit val finishJsonFormat               = Json.format[Finish]
  }

  case class Cancel(orderId: String)

  object Cancel {
    implicit val cancelJsonFormat               = Json.format[Cancel]
  }



}
