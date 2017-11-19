package actors.entities.cart

import akka.actor.{PoisonPill, Terminated}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.easymock.EasyMockSugar
import testsupport.{BaseData, PersistentActorSpec}

import scala.concurrent.ExecutionContext
import scala.concurrent.Future.{successful => future}

/**
  * Created by adildramdan on 11/18/17.
  */
class CartAggregateRecoverSpec extends PersistentActorSpec with BaseData with BeforeAndAfterEach with EasyMockSugar{
  implicit val ec: ExecutionContext = system.dispatcher
  override protected def beforeEach(): Unit = {
  }


  "A CartAggregateRecoverSpec " must {
    val userId              = "USER_ID"
    val product             = dataProduct()
    var cartAggregate       = system.actorOf(CartAggregate.props(), "cart-aggregate-"+userId)

    "recovered after kill" which {
      watch(cartAggregate)
      "kill the actor" in {

        waitingFor[ProductAddedToCart] {
          cartAggregate ! AddProductToCart(product, 1)
        }
        waitingFor[ProductAddedToCart] {
          cartAggregate ! AddProductToCart(product, 1)
        }

        waitingFor[Terminated] {
          cartAggregate ! PoisonPill
        }
        unwatch(cartAggregate)
      }
      "recover the data" in {
        cartAggregate           = system.actorOf(CartAggregate.props(), "cart-aggregate-"+userId)
        cartAggregate ! GetProduct
        expectMsg(ResponseProduct(List(Item(product, 2))))
      }
    }
  }
}
