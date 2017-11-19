package models.entities.order

import actors.entities.cart.Item
import play.api.libs.json.Json

/**
  * Created by adildramdan on 11/18/17.
  */
case class Order(id          : String,
                 userId      : String,
                 items       : List[Item],
                 coupon      : Option[OrderCoupon],
                 info        : OrderInformation,
                 payment     : Payment,
                 paymentProof: Option[String]         = None,
                 shipment    : Option[OrderShipment]  = None,
                 shipmentId  : Option[String]         = None)

object Order {
  implicit val orderJsonFormat = Json.format[Order]
}







