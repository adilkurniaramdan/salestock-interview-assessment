package models.entities.order

import actors.entities.cart.Item
import models.entities.Amount
import org.joda.time.LocalDate

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

case class OrderInformation(name        : String,
                            phone       : String,
                            email       : String,
                            address     : String)
case class OrderCoupon(id          : Option[Long],
                       code        : Option[String],
                       name        : String,
                       description : String,
                       amount      : Amount,
                       rate        : String,
                       start       : LocalDate,
                       end         : LocalDate)
case class Payment(method: String, name: Option[String] = None)
case class OrderShipment(name:  String = "JNE")
