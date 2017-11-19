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
class OrderActorFinishedSpec extends PersistentActorSpec with BaseData with BeforeAndAfterEach with EasyMockSugar{
  implicit val ec: ExecutionContext = system.dispatcher

  override protected def beforeEach(): Unit = {
  }


  "A OrderActorFinishedSpec " must {
    val userId              = "USER_ID"
    val order               = dataOrder()
    val product             = dataProduct().copy(qty = 100)
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
    val cartManager         = system.actorOf(CartManager.props(), CartManager.Name)
    val shipmentActor       = system.actorOf(FakeShipmentActor.props())
    var orderActor          = system.actorOf(OrderActor.props(productActor, couponActor, cartManager, shipmentActor), OrderActor.Name)


    "reply Submitted for Submit Message with valid data" in {
      waitingFor[CartManager.Created]{
        cartManager  ! CartManager.Create(userId)
      }
      waitingFor[ProductAddedToCart]{
        cartManager  ! CartManager.Execute(userId, AddProductToCart(product, 10))
      }
      orderActor ! Submit(order.id, order.userId, order.coupon.flatMap(_.code), order.payment, order.info)
      expectMsg(Submitted(order.id, order.userId, List(Item(product, 10)), order.coupon, order.payment, order.info, OrderStatus.OrderSubmitted))
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
      expectMsg(ResponseOrder(List(OrderDetail(order, OrderStatus.OrderFinish, 9000))))
    }
  }
}
