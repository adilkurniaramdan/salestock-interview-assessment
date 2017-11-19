package actors.entities.order

import java.util.UUID

import actors.entities.cart._
import actors.entities.reference.{CouponActor, ProductActor}
import akka.actor.{PoisonPill, Terminated}
import org.easymock.EasyMock.{anyLong, _}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.easymock.EasyMockSugar
import repositories.reference.{CouponRepository, ProductRepository}
import services.application.RandomService
import testsupport.{BaseData, FakeShipmentActor, PersistentActorSpec}
import utils.Constants.OrderStatus
import utils.helpers.JodaHelper
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future.{successful => future}

/**
  * Created by adildramdan on 11/18/17.
  */
class OrderAggregateSpec extends PersistentActorSpec with BaseData with BeforeAndAfterEach with EasyMockSugar{
  implicit val ec: ExecutionContext = system.dispatcher

  override protected def beforeEach(): Unit = {
  }


  "A OrderAggretageActor " must {
    "Order success flow" which {
      val cartId              = UUID.randomUUID().toString
      val orderId             = UUID.randomUUID().toString
      val order               = dataOrder().copy(id = orderId)
      val product             = dataProduct()
      val coupon              = dataCoupon()
      val productRepository   = mock[ProductRepository]
      val couponRepository    = mock[CouponRepository]
      val randomService       = mock[RandomService]

      expecting{
        productRepository.findById(anyLong()).andStubReturn(future(Some(product)))
        couponRepository.findOneByCode(anyString()).andStubReturn(future(Some(coupon)))
        couponRepository.findById(anyLong()).andStubReturn(future(Some(coupon)))
      }
      replay(productRepository, couponRepository)

      val productActor        = system.actorOf(ProductActor.props(productRepository))
      val couponActor         = system.actorOf(CouponActor.props(couponRepository, randomService))
      val cartActor           = system.actorOf(CartAggregate.props(cartId), "cart-actor-" + cartId)
      val shipmentActor       = system.actorOf(FakeShipmentActor.props())
      val orderActor          = system.actorOf(OrderAggregate.props(productActor, couponActor, cartActor, shipmentActor))


      "reply Submitted for Submit Message with valid data" in {
        waitingFor[ProductAddedToCart]{
          cartActor  ! AddProductToCart(product, 1)
        }
        orderActor ! Submit(order.id, order.userId, order.coupon.flatMap(_.code), order.payment, order.info)
        expectMsg(Submitted(order.id, order.userId, List(Item(product, 1)), order.coupon, order.payment, order.info, OrderStatus.OrderSubmitted))
      }

      "reply RequestVerification for ResponseVerification Message with valid data" in {
        orderActor ! RequestVerification(order.id, order.paymentProof.get)
        expectMsg(ResponseVerification(order.id, order.paymentProof.get, OrderStatus.OrderRequestVerification))
      }

      "reply Verify for Verified Message with valid data" in {
        orderActor    ! Verify(order.id)
        expectMsg(Verified(order.id, OrderStatus.OrderVerified))
      }

      "reply RequestShipment for Shipped Message with valid data" in {
        orderActor ! RequestShipment(order.id, order.shipment.get)
        expectMsg(Shipped(order.id, order.shipment.get, order.shipmentId.get, OrderStatus.OrderShipped))
      }
      "reply Finished for Finish Message with valid data" in {
        orderActor ! Finish(order.id)
        expectMsg(Finished(order.id, OrderStatus.OrderFinish))
      }

      "reply Finished for GetOrder Message with valid data" in {
        orderActor ! GetOrder
        expectMsg(ResponseOrder(List(OrderDetail(order, OrderStatus.OrderFinish))))
      }
    }

    " reply Canceled for Cancel with valid data  " in {
      val cartId              = UUID.randomUUID().toString
      val orderId             = UUID.randomUUID().toString
      val order               = dataOrder().copy(id = orderId)
      val product             = dataProduct()
      val coupon              = dataCoupon()
      val productRepository   = mock[ProductRepository]
      val couponRepository    = mock[CouponRepository]
      val randomService       = mock[RandomService]

      expecting{
        productRepository.findById(anyLong()).andStubReturn(future(Some(product)))
        couponRepository.findOneByCode(anyString()).andStubReturn(future(Some(coupon)))
        couponRepository.findById(anyLong()).andStubReturn(future(Some(coupon)))
      }
      replay(productRepository, couponRepository)

      val productActor        = system.actorOf(ProductActor.props(productRepository))
      val couponActor         = system.actorOf(CouponActor.props(couponRepository, randomService))
      val cartActor           = system.actorOf(CartAggregate.props(cartId), "cart-actor-" + cartId)
      val shipmentActor       = system.actorOf(FakeShipmentActor.props())
      val orderActor          = system.actorOf(OrderAggregate.props(productActor, couponActor, cartActor, shipmentActor))

      waitingFor[ProductAddedToCart]{
        cartActor  ! AddProductToCart(product, 1)
      }
      waitingFor[Submitted]{
        orderActor ! Submit(order.id, order.userId, order.coupon.flatMap(_.code), order.payment, order.info)
      }
      waitingFor[ResponseVerification]{
        orderActor ! RequestVerification(order.id, order.paymentProof.get)
      }
      orderActor ! Cancel(order.id)
      expectMsg(Canceled(order.id, OrderStatus.OrderCanceled))
    }

    " reply ItemNotAvailable for Submit with item not found data " in {
      val cartId              = UUID.randomUUID().toString
      val orderId             = UUID.randomUUID().toString
      val order               = dataOrder().copy(id = orderId)
      val product             = dataProduct()
      val coupon              = dataCoupon()
      val productRepository   = mock[ProductRepository]
      val couponRepository    = mock[CouponRepository]
      val randomService       = mock[RandomService]

      expecting{
        productRepository.findById(anyLong()).andStubReturn(future(None))
        couponRepository.findOneByCode(anyString()).andStubReturn(future(Some(coupon)))
        couponRepository.findById(anyLong()).andStubReturn(future(Some(coupon)))
      }
      replay(productRepository, couponRepository)

      val productActor        = system.actorOf(ProductActor.props(productRepository))
      val couponActor         = system.actorOf(CouponActor.props(couponRepository, randomService))
      val cartActor           = system.actorOf(CartAggregate.props(cartId), "cart-actor-" + cartId)
      val shipmentActor       = system.actorOf(FakeShipmentActor.props())
      val orderActor          = system.actorOf(OrderAggregate.props(productActor, couponActor, cartActor, shipmentActor))

      waitingFor[ProductAddedToCart]{
        cartActor  ! AddProductToCart(product, 1)
      }

      orderActor ! Submit(order.id, order.userId, order.coupon.flatMap(_.code), order.payment, order.info)
      expectMsg(ItemNotAvailable("Not all item available"))
    }

    " reply ItemNotAvailable for Submit with item qty not enough " in {
      val cartId              = UUID.randomUUID().toString
      val orderId             = UUID.randomUUID().toString
      val order               = dataOrder().copy(id = orderId)
      val product             = dataProduct()
      val coupon              = dataCoupon()
      val productRepository   = mock[ProductRepository]
      val couponRepository    = mock[CouponRepository]
      val randomService       = mock[RandomService]

      expecting{
        productRepository.findById(anyLong()).andStubReturn(future(Some(product.copy(qty = 0))))
        couponRepository.findOneByCode(anyString()).andStubReturn(future(Some(coupon)))
        couponRepository.findById(anyLong()).andStubReturn(future(Some(coupon)))
      }
      replay(productRepository, couponRepository)

      val productActor        = system.actorOf(ProductActor.props(productRepository))
      val couponActor         = system.actorOf(CouponActor.props(couponRepository, randomService))
      val cartActor           = system.actorOf(CartAggregate.props(cartId), "cart-actor-" + cartId)
      val shipmentActor       = system.actorOf(FakeShipmentActor.props())
      val orderActor          = system.actorOf(OrderAggregate.props(productActor, couponActor, cartActor, shipmentActor))

      waitingFor[ProductAddedToCart]{
        cartActor  ! AddProductToCart(product, 1)
      }

      orderActor ! Submit(order.id, order.userId, order.coupon.flatMap(_.code), order.payment, order.info)
      expectMsg(ItemNotAvailable("Not all item available"))
    }

    " reply CouponNotValid for Submit with invalid coupon code  " in {
      val cartId              = UUID.randomUUID().toString
      val orderId             = UUID.randomUUID().toString
      val order               = dataOrder().copy(id = orderId)
      val product             = dataProduct()
      val coupon              = dataCoupon()
      val productRepository   = mock[ProductRepository]
      val couponRepository    = mock[CouponRepository]
      val randomService       = mock[RandomService]

      expecting{
        productRepository.findById(anyLong()).andStubReturn(future(Some(product)))
        couponRepository.findOneByCode(anyString()).andStubReturn(future(None))
        couponRepository.findById(anyLong()).andStubReturn(future(Some(coupon)))
      }
      replay(productRepository, couponRepository)

      val productActor        = system.actorOf(ProductActor.props(productRepository))
      val couponActor         = system.actorOf(CouponActor.props(couponRepository, randomService))
      val cartActor           = system.actorOf(CartAggregate.props(cartId), "cart-actor-" + cartId)
      val shipmentActor       = system.actorOf(FakeShipmentActor.props())
      val orderActor          = system.actorOf(OrderAggregate.props(productActor, couponActor, cartActor, shipmentActor))

      waitingFor[ProductAddedToCart]{
        cartActor  ! AddProductToCart(product, 1)
      }

      orderActor ! Submit(order.id, order.userId, order.coupon.flatMap(_.code), order.payment, order.info)
      expectMsg(CouponNotValid("Coupon is not valid"))
    }
    " reply CouponNotValid for Submit with expire coupon code  " in {
      val cartId              = UUID.randomUUID().toString
      val orderId             = UUID.randomUUID().toString
      val order               = dataOrder().copy(id = orderId)
      val product             = dataProduct()
      val coupon              = dataCoupon()
      val productRepository   = mock[ProductRepository]
      val couponRepository    = mock[CouponRepository]
      val randomService       = mock[RandomService]

      expecting{
        productRepository.findById(anyLong()).andStubReturn(future(Some(product)))
        couponRepository.findOneByCode(anyString()).andStubReturn(future(None))
        couponRepository
          .findById(anyLong())
          .andStubReturn(future(Some(coupon.copy(start = JodaHelper.localDateParse("01/01/2015"), end = JodaHelper.localDateParse("31/12/2015")))))
      }
      replay(productRepository, couponRepository)

      val productActor        = system.actorOf(ProductActor.props(productRepository))
      val couponActor         = system.actorOf(CouponActor.props(couponRepository, randomService))
      val cartActor           = system.actorOf(CartAggregate.props(cartId), "cart-actor-" + cartId)
      val shipmentActor       = system.actorOf(FakeShipmentActor.props())
      val orderActor          = system.actorOf(OrderAggregate.props(productActor, couponActor, cartActor, shipmentActor))

      waitingFor[ProductAddedToCart]{
        cartActor  ! AddProductToCart(product, 1)
      }

      orderActor ! Submit(order.id, order.userId, order.coupon.flatMap(_.code), order.payment, order.info)
      expectMsg(CouponNotValid("Coupon is not valid"))
    }
    " reply CouponNotValid for Submit with qty coupon = 0  " in {
      val cartId              = UUID.randomUUID().toString
      val orderId             = UUID.randomUUID().toString
      val order               = dataOrder().copy(id = orderId)
      val product             = dataProduct()
      val coupon              = dataCoupon()
      val productRepository   = mock[ProductRepository]
      val couponRepository    = mock[CouponRepository]
      val randomService       = mock[RandomService]

      expecting{
        productRepository.findById(anyLong()).andStubReturn(future(Some(product)))
        couponRepository.findOneByCode(anyString()).andStubReturn(future(Some(coupon.copy(qty = 0))))
        couponRepository.findById(anyLong()).andStubReturn(future(Some(coupon)))
      }
      replay(productRepository, couponRepository)

      val productActor        = system.actorOf(ProductActor.props(productRepository))
      val couponActor         = system.actorOf(CouponActor.props(couponRepository, randomService))
      val cartActor           = system.actorOf(CartAggregate.props(cartId), "cart-actor-" + cartId)
      val shipmentActor       = system.actorOf(FakeShipmentActor.props())
      val orderActor          = system.actorOf(OrderAggregate.props(productActor, couponActor, cartActor, shipmentActor))

      waitingFor[ProductAddedToCart]{
        cartActor  ! AddProductToCart(product, 1)
      }

      orderActor ! Submit(order.id, order.userId, order.coupon.flatMap(_.code), order.payment, order.info)
      expectMsg(CouponNotValid("Coupon is not valid"))
    }

    "recovered after kill" which {
      val cartId              = UUID.randomUUID().toString
      val orderId             = UUID.randomUUID().toString
      val order               = dataOrder().copy(id = orderId)
      val product             = dataProduct()
      val coupon              = dataCoupon()
      val productRepository   = mock[ProductRepository]
      val couponRepository    = mock[CouponRepository]
      val randomService       = mock[RandomService]

      expecting{
        productRepository.findById(anyLong()).andStubReturn(future(Some(product)))
        couponRepository.findOneByCode(anyString()).andStubReturn(future(Some(coupon)))
        couponRepository.findById(anyLong()).andStubReturn(future(Some(coupon)))
      }
      replay(productRepository, couponRepository)

      val productActor        = system.actorOf(ProductActor.props(productRepository))
      val couponActor         = system.actorOf(CouponActor.props(couponRepository, randomService))
      val cartActor           = system.actorOf(CartAggregate.props(cartId), "cart-actor-" + cartId)
      val shipmentActor       = system.actorOf(FakeShipmentActor.props())
      var orderActor          = system.actorOf(OrderAggregate.props(productActor, couponActor, cartActor, shipmentActor), "order-actor-"+orderId)

      watch(orderActor)
      "kill the actor" in {

        waitingFor[ProductAddedToCart]{
          cartActor  ! AddProductToCart(product, 1)
        }
        waitingFor[Submitted]{
          orderActor ! Submit(order.id, order.userId, order.coupon.flatMap(_.code), order.payment, order.info)
        }
        waitingFor[ResponseVerification]{
          orderActor ! RequestVerification(order.id, order.paymentProof.get)
        }
        waitingFor[Canceled]{
          orderActor ! Cancel(order.id)
        }
        waitingFor[Terminated] {
          orderActor ! PoisonPill
        }
        unwatch(orderActor)
      }

      "recover the data" in {
        orderActor          = system.actorOf(OrderAggregate.props(productActor, couponActor, cartActor, shipmentActor), "order-actor-"+orderId)
        orderActor ! GetOrder
        expectMsg(1 minute, ResponseOrder(List(OrderDetail(order.copy(shipment = None, shipmentId = None), OrderStatus.OrderCanceled))))
      }
    }
  }
}
