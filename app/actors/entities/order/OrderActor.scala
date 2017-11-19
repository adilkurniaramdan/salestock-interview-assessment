package actors.entities.order

import javax.inject.{Inject, Named}

import actors.entities.cart._
import actors.entities.order.ShipmentActor.{RequestShipmentID, ResponseShipmentID}
import actors.entities.reference.ProductActor.UpdateQty
import actors.entities.reference.{CouponActor, ProductActor, ValidateCoupon, ValidateItem}
import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence.PersistentActor
import models.entities.order.OrderCoupon
import utils.Constants.OrderStatus

/**
  * Created by adildramdan on 11/18/17.
  */
class OrderActor @Inject()(@Named(ProductActor.Name) 
                           productActor           : ActorRef,
                           @Named(CouponActor.Name)
                           couponActor            : ActorRef,
                           @Named(CartManager.Name)
                           cartManager            : ActorRef,
                           @Named(ShipmentActor.Name)
                           shipmentActor: ActorRef) extends PersistentActor with ActorLogging {

  private val state   = OrderState()

  override def persistenceId: String = self.path.name

  private def updateState(evt: Event) = {
    evt match {
      case e: Submitted             =>
        state.add(e.orderId, e.userId, e.items, e.coupon, e.payment, e.info, e.status)
        context.become(handleRequestVerificationCommand orElse handleQueryCommand)

      case e: ResponseVerification  =>
        state.updatePaymentProof(e.orderId, e.paymentProof, e.status)
        context.become(handleVerifyCommand orElse handleQueryCommand)

      case e: Verified              =>
        state.updateStatus(e.orderId, e.status)
        context.become(handleRequestShipmentCommand orElse handleQueryCommand)

      case e: Shipped               =>
        state.updateShipment(e.orderId, e.shipment, e.shipmentId, e.status)
        context.become(handleFinishCommand orElse handleQueryCommand)

      case e: Finished              =>
        state.updateStatus(e.orderId, e.status)
        context.become(handleQueryCommand)

      case e: Canceled              =>
        state.updateStatus(e.orderId, e.status)
        context.become(handleQueryCommand)
    }
  }

  override def receiveRecover: Receive = {
    case e: Event => updateState(e)
  }

  override def receiveCommand: Receive = handleSubmitCommand orElse handleQueryCommand

  // 2. Customer can apply one coupon to order, only one coupon can be applied to order
  // 3. Customer can submit an order and the order is finalized
  // 4. Customer can only pay via bank transfer
  // 5. When placing order the following data is required: name, phone number, email, address
  private def handleSubmitCommand : Receive = {
    case m: Submit                =>
      lookUpCartItem(sender(), m)

    case m: ResponseProductOrder  =>
      if(m.items.nonEmpty)
        validateItemAvailability(m.source, m.data, m.items)
      else
        notifyCartIsEmpty(m.source)

    case m: ValidateItem.Successful       =>
      validateCouponValidity(m.response.source, m.response.data, m.response.items)

    case m: ValidateItem.Unsuccessful     =>
      notifyItemNotAvailable(m)

    case m: ValidateCoupon.Successful     =>
      handleSubmitValidationSuccess(m.response.source, m.response.data, m.response.items, Some(m.orderCoupon))

    case m: ValidateCoupon.Unsuccessful   =>
      notifyCouponNotValid(m)

  }
  private def lookUpCartItem(source: ActorRef, m: Submit) = {
    cartManager ! CartManager.Execute(m.userId, GetProductOrder(sender(), m))
  }

  private def notifyCartIsEmpty(source: ActorRef) = {
    source            ! ItemNotAvailable("Cart is empty")
  }

  private def validateItemAvailability(source: ActorRef, m: Submit, items: List[Item]) = {
    productActor    ! ValidateItem(source,  m, items )
  }

  private def notifyItemNotAvailable(validate: ValidateItem.Unsuccessful) = {
    validate.source ! ItemNotAvailable(validate.reason)
  }

  private def validateCouponValidity(source: ActorRef, m: Submit, items: List[Item]) = {
    m.coupon match {
      case Some(coupon) =>
        couponActor  ! ValidateCoupon(source, m, items, coupon)
      case None         =>
        handleSubmitValidationSuccess(source, m, items, None)
    }
  }

  private def notifyCouponNotValid(validate: ValidateCoupon.Unsuccessful) = {
    validate.source ! CouponNotValid(validate.reason)
  }

  private def handleSubmitValidationSuccess(source: ActorRef, m: Submit, items: List[Item], coupon: Option[OrderCoupon]) = {
    persistAndUpdateState(
      Submitted(m.orderId, m.userId, items, coupon, m.payment, m.info, OrderStatus.OrderSubmitted),
      source
    )
    // 6. When an order is submitted, the quantity for ordered product will be reduced based on the quantity.
    items.foreach{ item =>
      productActor ! UpdateQty(item.product.id.get, -item.qty)
    }
    coupon.foreach{c =>
      couponActor ! UpdateQty(c.id.get, -1)
    }
    cartManager ! CartManager.Execute(m.userId, ClearProduct)
  }


  private def handleRequestVerificationCommand: Receive = {
    case m: RequestVerification =>
      persistAndUpdateState(ResponseVerification(m.orderId, m.paymentProof, OrderStatus.OrderRequestVerification))
  }

  private def handleVerifyCommand: Receive = {
    case m: Verify  =>
      persistAndUpdateState(Verified(m.orderId, OrderStatus.OrderVerified))

    case m: Cancel  =>
      persistAndUpdateState(Canceled(m.orderId, OrderStatus.OrderCanceled))

  }

  private def handleRequestShipmentCommand: Receive = {
    case m: RequestShipment     =>
      shipmentActor ! RequestShipmentID(sender(), m)
    case m: ResponseShipmentID  =>
      persistAndUpdateState(Shipped(m.data.orderId, m.data.shipment, m.shipmentId, OrderStatus.OrderShipped), m.source)

  }

  private def handleFinishCommand: Receive = {
    case m: Finish  =>
      persistAndUpdateState(Finished(m.orderId, OrderStatus.OrderFinish))
  }

  private def handleQueryCommand: Receive = {
    case GetOrder          => sender() ! ResponseOrder(state.orderDetail)
    case m: GetOrderById   => sender() ! ResponseOrderOpt(state.get(m.orderId))
    case m: GetOrderByUser => sender() ! ResponseOrder(state.getByUser(m.userId))
    case m: GetOrderByShippingId => sender() ! ResponseOrderOpt(state.getByShippingId(m.shippingId))
  }

  private def persistAndUpdateState(e: Event, replyTo: ActorRef = sender()) {
    persist(e) { event =>
      updateState(event)
      replyTo ! event
    }
  }
}

object OrderActor {
  final val Name  = "order-actor"
  def props(productActor: ActorRef, couponActor: ActorRef, cartManager: ActorRef, shipmentActor: ActorRef) =
    Props(new OrderActor(productActor, couponActor, cartManager, shipmentActor))
}
