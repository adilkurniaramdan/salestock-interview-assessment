package actors.entities.order

import actors.entities.cart.Item
import models.entities.order._
import utils.Constants.Rate

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

  def orderDetail: List[OrderDetail] = orders.map{case (_, v) => OrderDetail(v._1, v._2, calculateTotal(v._1))}.toList

  def get(orderId: String) =
    orders.get(orderId).map{v => OrderDetail(v._1, v._2, calculateTotal(v._1))}

  def getByShippingId(shippingId: String) =
    orders.values
      .filter{case (order, _) => order.shipmentId.contains(shippingId)}
      .map{v => OrderDetail(v._1, v._2, calculateTotal(v._1))}
      .headOption

  def getByUser(userId: String) =
    orders.values
      .filter{case (order, _) => order.userId == userId}
      .map{v => OrderDetail(v._1, v._2, calculateTotal(v._1))}
      .toList

  private def calculateTotal(order: Order) = {
    val total   = order.items.map(item => item.product.unitPrice * item.qty).sum
    order.coupon match {
      case Some(coupon) =>
        coupon.rate match {
          case Rate.Nominal     =>
            total - coupon.amount
          case Rate.Percentage  =>
            total - ((coupon.amount / 100) * total)
        }
      case None         =>
        total
    }
  }
}
