package models.entities.order

import exceptions.ObjectNotFoundException
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, Reads, Writes, _}

/**
  * Created by adildramdan on 11/18/17.
  */

object OrderStatuses {

  sealed abstract class OrderStatus(val name: String)

  case object OrderSubmitted extends OrderStatus("order-submitted")

  case object OrderRequestVerification extends OrderStatus("order-request-verification")

  case object OrderVerified extends OrderStatus("order-verified")

  case object OrderShipped extends OrderStatus("order-shipped")

  case object OrderFinish extends OrderStatus("order-finish")

  case object OrderCanceled extends OrderStatus("order-canceled")

  object OrderStatus {
    def fromString(s: String): OrderStatus = s match {
      case "order-submitted"                  => OrderSubmitted
      case "order-request-verification"       => OrderRequestVerification
      case "order-verified"                   => OrderVerified
      case "order-shipped"                    => OrderShipped
      case "order-finish"                     => OrderFinish
      case "order-canceled"                   => OrderCanceled
    }
    def values = Seq(
      OrderSubmitted,
      OrderRequestVerification,
      OrderVerified,
      OrderShipped,
      OrderFinish,
      OrderCanceled
    )
  }

  val orderStatuses = Seq[OrderStatus](
    OrderSubmitted,
    OrderRequestVerification,
    OrderVerified,
    OrderShipped,
    OrderFinish,
    OrderCanceled
  )

  /* JSON implicits */
  val rateReads: Reads[OrderStatus] = __.read[String].map { s =>
    orderStatuses.find { r => r.name.equals(s) }.getOrElse(throw ObjectNotFoundException("status-unknown"))
  }
  val rateWrites: Writes[OrderStatus] = __.write[String].contramap { (rate: OrderStatus) =>
    rate.name
  }
  implicit val jsonFormat = Format(rateReads, rateWrites)

}


