package actors.entities.order

import java.util.UUID

import actors.entities.cart.Item
import models.entities.order.OrderStatuses.OrderStatus
import models.entities.order._

/**
  * Created by adildramdan on 11/18/17.
  */

case class OrderState(private var orders: Map[String, (Order, OrderStatus)] = Map.empty) {
  def add(userId: String, items : List[Item], coupon: Option[OrderCoupon], payment: Payment, info: OrderInformation, status: OrderStatus): Unit = {
    val orderId = UUID.randomUUID().toString
    val order = Order(
      id      = orderId,
      userId  = userId,
      items   = items,
      coupon  = coupon,
      info    = info,
      payment = payment
    )
    orders = orders + (orderId -> (order, status))
  }

  def updatePaymentProof(orderId: String, paymentProof: String, status: OrderStatus) = {
    orders.get(orderId) match {
      case Some(order)  =>
        orders = orders + (orderId -> (order._1.copy(paymentProof = Some(paymentProof)), status))
      case None         => // do nothing
    }
  }

  def updateShipment(orderId: String, shipment: OrderShipment, shipmentId: String, status: OrderStatus) = {
    orders.get(orderId) match {
      case Some(order)  =>
        orders = orders + (orderId -> (order._1.copy(shipment = Some(shipment), shipmentId = Some(shipmentId)), status))
      case None         => // do nothing
    }
  }

  def updateStatus(orderId: String, status: OrderStatus) = {
    orders.get(orderId) match {
      case Some(order)  =>
        orders = orders + (orderId -> (order._1, status))
      case None         => // do nothing
    }
  }

  def orderDetail: List[OrderDetail] = orders.map{case (_, v) => OrderDetail(v._1, v._2)}.toList

}
