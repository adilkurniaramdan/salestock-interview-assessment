package actors.entities.order

import models.entities.Amount
import models.entities.order.Order
import play.api.libs.json.Json

/**
  * Created by adildramdan on 11/18/17.
  */
case class OrderDetail(order: Order, status: String, total: Amount)

object OrderDetail {
  implicit val orderDetailJsonFormat = Json.format[OrderDetail]
}
