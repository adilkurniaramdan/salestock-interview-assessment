package actors.entities.cart

import java.util.UUID

import actors.entities.cart.CartMessage._
import actors.entities.reference.ProductStockActor
import akka.actor.{PoisonPill, Terminated}
import org.easymock.EasyMock.{anyInt, replay, verify}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.easymock.EasyMockSugar
import repositories.reference.ProductRepository
import testsupport.{BaseData, PersistentActorSpec}

import scala.concurrent.ExecutionContext
import scala.concurrent.Future.{successful => future}
import scala.concurrent.duration._

/**
  * Created by adildramdan on 11/18/17.
  */
class CartAggregateSpec extends PersistentActorSpec with BaseData with BeforeAndAfterEach with EasyMockSugar{
  implicit val ec: ExecutionContext = system.dispatcher
  val cartId  = UUID.randomUUID().toString

  override protected def beforeEach(): Unit = {
  }


  "A CartAggretageActor " must {
    "reply ProductAddedToCart for AddProductToCart Message with valid data" in {
      val product             = dataProduct()
      val productRepository   = mock[ProductRepository]
      expecting{
        productRepository
          .findById(anyInt())
          .andReturn(future(Some(product)))
      }
      replay(productRepository)

      val productStockActor   = system.actorOf(ProductStockActor.props(productRepository))
      val cartActor           = system.actorOf(CartAggregate.props(cartId, productStockActor))

      cartActor ! AddProductToCart(product, 1)
      expectMsg(ProductAddedToCart(product, 1))

      verify(productRepository)
    }

    "reply ProductNotAvailable for AddProductToCart Message with invalid data" in {
      val product             = dataProduct()
      val productRepository   = mock[ProductRepository]
      expecting{
        productRepository
          .findById(anyInt())
          .andReturn(future(Some(product)))
      }
      replay(productRepository)

      val productStockActor   = system.actorOf(ProductStockActor.props(productRepository))
      val cartActor           = system.actorOf(CartAggregate.props(cartId, productStockActor))

      cartActor ! AddProductToCart(product, 100)
      expectMsg(ProductNotAvailable(product, 100, "Sold out"))

      verify(productRepository)
    }

    "reply ResponseProduct for GetProduct Message with valid data" in {
      val product             = dataProduct().copy(qty = 100)
      val productRepository   = mock[ProductRepository]
      expecting{
        productRepository
          .findById(anyInt())
          .andStubReturn(future(Some(product)))
      }
      replay(productRepository)

      val productStockActor   = system.actorOf(ProductStockActor.props(productRepository))
      val cartActor           = system.actorOf(CartAggregate.props(cartId, productStockActor))

      waitingFor[ProductAddedToCart] {
        cartActor ! AddProductToCart(product, 1)
      }

      waitingFor[ProductAddedToCart] {
        cartActor ! AddProductToCart(product, 1)
      }

      cartActor ! GetProduct
      expectMsg(ResponseProduct(List(CartItem(product, 2))))

      verify(productRepository)
    }

    "reply ProductRemovedFromCart for RemoveProductFromCart Message with valid data" in {
      val product             = dataProduct()
      val productRepository   = mock[ProductRepository]
      expecting{
        productRepository
          .findById(anyInt())
          .andStubReturn(future(Some(product)))
      }
      replay(productRepository)

      val productStockActor   = system.actorOf(ProductStockActor.props(productRepository))
      val cartActor           = system.actorOf(CartAggregate.props(cartId, productStockActor))

      waitingFor[ProductAddedToCart] {
        cartActor ! AddProductToCart(product, 1)
      }

      cartActor ! RemoveProductFromCart(product, 1)
      expectMsg(ProductRemovedFromCart(product, 1))

      verify(productRepository)
    }

    "reply Nothing for RemoveProductFromCart Message with invalid data" in {
      val product             = dataProduct()
      val productRepository   = mock[ProductRepository]
      expecting{
        productRepository
          .findById(anyInt())
          .andStubReturn(future(Some(product)))
      }
      replay(productRepository)

      val productStockActor   = system.actorOf(ProductStockActor.props(productRepository))
      val cartActor           = system.actorOf(CartAggregate.props(cartId, productStockActor))

      cartActor ! RemoveProductFromCart(product, 1)
      expectNoMsg(1 second)

      verify(productRepository)
    }

    "reply ResponseProduct subtracted for ProductAddedToCart Message with valid data" in {
      val product             = dataProduct().copy(qty = 100)
      val productRepository   = mock[ProductRepository]
      expecting{
        productRepository
          .findById(anyInt())
          .andStubReturn(future(Some(product)))
      }
      replay(productRepository)

      val productStockActor   = system.actorOf(ProductStockActor.props(productRepository))
      val cartActor           = system.actorOf(CartAggregate.props(cartId, productStockActor))

      waitingFor[ProductAddedToCart] {
        cartActor ! AddProductToCart(product, 3)
      }

      waitingFor[ProductRemovedFromCart] {
        cartActor ! RemoveProductFromCart(product, 2)
      }

      cartActor ! GetProduct
      expectMsg(ResponseProduct(List(CartItem(product, 1))))

      verify(productRepository)
    }

    "reply ResponseProduct Nil for ProductAddedToCart Message with valid data" in {
      val product             = dataProduct().copy(qty = 100)
      val productRepository   = mock[ProductRepository]
      expecting{
        productRepository
          .findById(anyInt())
          .andStubReturn(future(Some(product)))
      }
      replay(productRepository)

      val productStockActor   = system.actorOf(ProductStockActor.props(productRepository))
      val cartActor           = system.actorOf(CartAggregate.props(cartId, productStockActor))

      waitingFor[ProductAddedToCart] {
        cartActor ! AddProductToCart(product, 3)
      }

      waitingFor[ProductRemovedFromCart] {
        cartActor ! RemoveProductFromCart(product, 3)
      }

      cartActor ! GetProduct
      expectMsg(ResponseProduct(Nil))

      verify(productRepository)
    }

    "reply ProductCleared for ClearProduct Message with valid data" in {
      val product             = dataProduct().copy(qty = 100)
      val productRepository   = mock[ProductRepository]
      expecting{
        productRepository
          .findById(anyInt())
          .andStubReturn(future(Some(product)))
      }
      replay(productRepository)

      val productStockActor   = system.actorOf(ProductStockActor.props(productRepository))
      val cartActor           = system.actorOf(CartAggregate.props(cartId, productStockActor))

      waitingFor[ProductAddedToCart] {
        cartActor ! AddProductToCart(product, 3)
      }

      waitingFor[ProductRemovedFromCart] {
        cartActor ! RemoveProductFromCart(product, 3)
      }

      cartActor ! ClearProduct
      expectMsg(ProductCleared)

      cartActor ! GetProduct
      expectMsg(ResponseProduct(Nil))

      verify(productRepository)
    }

    "recovered after kill" which {
      val product             = dataProduct().copy(qty = 100)
      val productRepository   = mock[ProductRepository]
      val cartId              = "id-actortoberecover"
      expecting{
        productRepository
          .findById(anyInt())
          .andStubReturn(future(Some(product)))
      }
      replay(productRepository)

      val productStockActor   = system.actorOf(ProductStockActor.props(productRepository))
      var cartActor           = system.actorOf(CartAggregate.props(cartId, productStockActor), cartId)
      watch(cartActor)
      "kill the actor" in {

        waitingFor[ProductAddedToCart] {
          cartActor ! AddProductToCart(product, 1)
        }
        waitingFor[Terminated] {
          cartActor ! PoisonPill
        }
        unwatch(cartActor)
      }

      "recover the data" in {
        cartActor           = system.actorOf(CartAggregate.props(cartId, productStockActor), cartId)
        cartActor ! GetProduct
        expectMsg(ResponseProduct(List(CartItem(product, 1))))
      }
    }

  }
}
