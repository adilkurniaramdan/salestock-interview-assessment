package actors.entities.cart

import java.util.UUID

import akka.actor.{PoisonPill, Terminated}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.easymock.EasyMockSugar
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
      val cartActor           = system.actorOf(CartAggregate.props())

      cartActor ! AddProductToCart(product, 1)
      expectMsg(ProductAddedToCart(product, 1))

    }

    "reply ResponseProduct for GetProduct Message with valid data" in {
      val product             = dataProduct().copy(qty = 100)
      val cartActor           = system.actorOf(CartAggregate.props())

      waitingFor[ProductAddedToCart] {
        cartActor ! AddProductToCart(product, 1)
      }

      waitingFor[ProductAddedToCart] {
        cartActor ! AddProductToCart(product, 1)
      }

      cartActor ! GetProduct
      expectMsg(ResponseProduct(List(Item(product, 2))))
    }

    "reply ProductRemovedFromCart for RemoveProductFromCart Message with valid data" in {
      val product             = dataProduct()
      val cartActor           = system.actorOf(CartAggregate.props())

      waitingFor[ProductAddedToCart] {
        cartActor ! AddProductToCart(product, 1)
      }

      cartActor ! RemoveProductFromCart(product, 1)
      expectMsg(ProductRemovedFromCart(product, 1))
    }

    "reply Nothing for RemoveProductFromCart Message with invalid data" in {
      val product             = dataProduct()
      val cartActor           = system.actorOf(CartAggregate.props())

      cartActor ! RemoveProductFromCart(product, 1)
      expectNoMsg(1 second)
    }

    "reply ResponseProduct subtracted for ProductAddedToCart Message with valid data" in {
      val product             = dataProduct().copy(qty = 100)
      val cartActor           = system.actorOf(CartAggregate.props())

      waitingFor[ProductAddedToCart] {
        cartActor ! AddProductToCart(product, 3)
      }

      waitingFor[ProductRemovedFromCart] {
        cartActor ! RemoveProductFromCart(product, 2)
      }

      cartActor ! GetProduct
      expectMsg(ResponseProduct(List(Item(product, 1))))
    }

    "reply ResponseProduct Nil for ProductAddedToCart Message with valid data" in {
      val product             = dataProduct().copy(qty = 100)
      val cartActor           = system.actorOf(CartAggregate.props())

      waitingFor[ProductAddedToCart] {
        cartActor ! AddProductToCart(product, 3)
      }

      waitingFor[ProductRemovedFromCart] {
        cartActor ! RemoveProductFromCart(product, 3)
      }

      cartActor ! GetProduct
      expectMsg(ResponseProduct(Nil))
    }

    "reply ProductCleared for ClearProduct Message with valid data" in {
      val product             = dataProduct().copy(qty = 100)
      val cartActor           = system.actorOf(CartAggregate.props())

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
    }

    "recovered after kill" which {
      val product             = dataProduct().copy(qty = 100)
      val cartId              = "id-actortoberecover"
      var cartActor           = system.actorOf(CartAggregate.props(), cartId)
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
        cartActor           = system.actorOf(CartAggregate.props(), cartId)
        cartActor ! GetProduct
        expectMsg(ResponseProduct(List(Item(product, 1))))
      }
    }

  }
}
