package actors.entities.cart

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
class CartManagerSpec extends PersistentActorSpec with BaseData with BeforeAndAfterEach with EasyMockSugar{
  implicit val ec: ExecutionContext = system.dispatcher
  override protected def beforeEach(): Unit = {
  }


  "A CartAggretageActor " must {
    val userId              = "USER_ID"
    val product             = dataProduct()
    val cartManager         = system.actorOf(CartManager.props(), CartManager.Name)

    "reply Created for Create Message with valid data" in {
      cartManager ! CartManager.Create(userId)
      expectMsg(CartManager.Created(userId))
    }

    "reply ProductAddedToCart for AddProductToCart Message with valid data" in {
      cartManager ! CartManager.Execute(userId, AddProductToCart(product, 10))
      expectMsg(ProductAddedToCart(product, 10))
    }

    "reply ResponseProduct for GetProduct Message with valid data" in {
      cartManager ! CartManager.Execute(userId, GetProduct)
      expectMsg(ResponseProduct(List(Item(product, 10))))
    }

    "reply ProductRemovedFromCart for RemoveProductFromCart Message with valid data" in {
      cartManager ! CartManager.Execute(userId, RemoveProductFromCart(product, 10))
      expectMsg(ProductRemovedFromCart(product, 10))
    }

    "reply Nothing for RemoveProductFromCart Message with invalid data" in {
      cartManager ! CartManager.Execute(userId, RemoveProductFromCart(product, 100))
      expectNoMsg(1 second)
    }

    "reply ResponseProduct subtracted for ProductAddedToCart Message with valid data" in {
      waitingFor[ProductAddedToCart] {
        cartManager ! CartManager.Execute(userId, AddProductToCart(product, 10))
      }
      waitingFor[ProductRemovedFromCart] {
        cartManager ! CartManager.Execute(userId, RemoveProductFromCart(product, 5))
      }

      cartManager ! CartManager.Execute(userId, GetProduct)
      expectMsg(ResponseProduct(List(Item(product, 5))))
    }

    "reply ResponseProduct Nil for ProductAddedToCart Message with valid data" in {
      waitingFor[ProductRemovedFromCart] {
        cartManager ! CartManager.Execute(userId, RemoveProductFromCart(product, 5))
      }
      cartManager ! CartManager.Execute(userId, GetProduct)
      expectMsg(ResponseProduct(Nil))
    }

    "reply ProductCleared for ClearProduct Message with valid data" in {
      waitingFor[ProductAddedToCart] {
        cartManager ! CartManager.Execute(userId, AddProductToCart(product, 10))
      }

      waitingFor[ProductRemovedFromCart] {
        cartManager ! CartManager.Execute(userId, RemoveProductFromCart(product, 5))
      }
      cartManager ! CartManager.Execute(userId, ClearProduct)
      expectMsg(ProductCleared)
    }
  }
}
