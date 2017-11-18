package actors.entities.order

import models.entities.order.Order
import models.entities.order.OrderStatuses.OrderStatus

/**
  * Created by adildramdan on 11/18/17.
  */
case class OrderDetail(order: Order, status: OrderStatus)
