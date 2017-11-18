package actors.entities.reference

import akka.testkit.TestProbe
import org.easymock.EasyMock._
import org.scalatest.easymock.EasyMockSugar
import repositories.reference.ProductRepository
import testsupport.{ActorSpec, BaseData}

import scala.concurrent.ExecutionContext
import scala.concurrent.Future.{successful => future}

/**
  * Created by adildramdan on 11/17/17.
  */
class ProductStockActorSpec extends ActorSpec with BaseData with EasyMockSugar {
  implicit val ec: ExecutionContext = system.dispatcher

  "A ProductCheckerActor " must {
    "Reply Stock.Successful for Stock message with valid data " in {
      val product             = dataProduct()
      val productRepository   = mock[ProductRepository]

      expecting{
        productRepository
          .findById(anyInt())
          .andReturn(future(Some(product)))
      }
      replay(productRepository)

      val dummyActor          = TestProbe()
      val productCheckerActor  = system.actorOf(ProductStockActor.props(productRepository))

      val msg                 = Stock(dummyActor.ref, product, 1)
      productCheckerActor   ! msg
      expectMsg(Stock.Successful(msg))

      verify(productRepository)
    }

    "Reply Stock.Unsuccessful for Stock message with invalid data " in {
      val product             = dataProduct()
      val productRepository   = mock[ProductRepository]

      expecting{
        productRepository
          .findById(anyInt())
          .andReturn(future(Some(product)))
      }
      replay(productRepository)
      val dummyActor          = TestProbe()
      val productCheckerActor  = system.actorOf(ProductStockActor.props(productRepository))

      val msg                 = Stock(dummyActor.ref, product, 100)
      productCheckerActor   ! msg
      expectMsg(Stock.Unsuccessful(msg, "Sold out"))

      verify(productRepository)
    }
  }

}
