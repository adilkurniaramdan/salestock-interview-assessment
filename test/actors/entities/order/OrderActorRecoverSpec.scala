package actors.entities.order

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

import scala.concurrent.ExecutionContext
import scala.concurrent.Future.{successful => future}
import scala.concurrent.duration._

/**
  * Created by adildramdan on 11/18/17.
  */
class OrderActorRecoverSpec extends PersistentActorSpec with BaseData with BeforeAndAfterEach with EasyMockSugar{
  implicit val ec: ExecutionContext = system.dispatcher

  override protected def beforeEach(): Unit = {
  }

  "A OrderActorRecoverSpec " must {
    val userId              = "USER_ID"
    val order               = dataOrder().copy(shipment = None, shipmentId = None)
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

    "recovered after kill" which {
      watch(orderActor)
      "kill the actor" in {
        waitingFor[CartManager.Created]{
          cartManager  ! CartManager.Create(userId)
        }
        waitingFor[ProductAddedToCart]{
          cartManager  ! CartManager.Execute(userId, AddProductToCart(product, 10))
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
        orderActor          = system.actorOf(OrderActor.props(productActor, couponActor, cartManager, shipmentActor), OrderActor.Name)
        orderActor ! GetOrder
        expectMsg(1 minute, ResponseOrder(List(OrderDetail(order, OrderStatus.OrderCanceled, 9000))))
      }
    }
  }
}
