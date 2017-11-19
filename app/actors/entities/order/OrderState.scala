package actors.entities.order

import actors.entities.cart.Item
import models.entities.order._

/**
  * Created by adildramdan on 11/18/17.
  */

case class OrderState(private var orders: Map[String, (Order, String)] = Map.empty) {
  def add(orderId: String, userId: String, items : List[Item], coupon: Option[OrderCoupon], payment: Payment, info: OrderInformation, status: String): Unit = {
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

  def updatePaymentProof(orderId: String, paymentProof: String, status: String) = {
    orders.get(orderId) match {
      case Some(order)  =>
        orders = orders + (orderId -> (order._1.copy(paymentProof = Some(paymentProof)), status))
      case None         => // do nothing
    }
  }

  def updateShipment(orderId: String, shipment: OrderShipment, shipmentId: String, status: String) = {
    orders.get(orderId) match {
      case Some(order)  =>
        orders = orders + (orderId -> (order._1.copy(shipment = Some(shipment), shipmentId = Some(shipmentId)), status))
      case None         => // do nothing
    }
  }

  def updateStatus(orderId: String, status: String) = {
    orders.get(orderId) match {
      case Some(order)  =>
        orders = orders + (orderId -> (order._1, status))
      case None         => // do nothing
    }
  }

  def orderDetail: List[OrderDetail] = orders.map{case (_, v) => OrderDetail(v._1, v._2)}.toList

}
