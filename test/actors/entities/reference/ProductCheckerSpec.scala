package actors.entities.reference

import akka.testkit.ImplicitSender
import base.{BaseAkkaSpec, BaseData}
import org.easymock.EasyMock._
import org.scalatest.easymock.EasyMockSugar
import repositories.reference.ProductRepository

import scala.concurrent.ExecutionContext
import scala.concurrent.Future.{successful => future}

/**
  * Created by adildramdan on 11/17/17.
  */
class ProductCheckerActorSpec extends BaseAkkaSpec with BaseData with ImplicitSender with EasyMockSugar {
  implicit val ec: ExecutionContext = system.dispatcher

  "An ProductCheckerActor " must {
    "Reply Available for Check message with valid data " in {
      val product             = dataProduct()
      val productRepository   = mock[ProductRepository]

      expecting{
        productRepository
          .findById(anyInt())
          .andReturn(future(Some(product)))
      }
      replay(productRepository)
      val productCheckerActor  = system.actorOf(ProductCheckerActor.props(productRepository))

      productCheckerActor ! ProductCheckerActor.Check(idLong, 1)
      expectMsg(ProductCheckerActor.Available)
      verify(productRepository)
    }
    "Reply NotAvailable for Check message with invalid data " in {
      val product             = dataProduct()
      val productRepository   = mock[ProductRepository]

      expecting{
        productRepository
          .findById(anyInt())
          .andReturn(future(Some(product)))
      }
      replay(productRepository)
      val productCheckerActor  = system.actorOf(ProductCheckerActor.props(productRepository))

      productCheckerActor ! ProductCheckerActor.Check(idLong, 100)
      expectMsg(ProductCheckerActor.NotAvailable)
      verify(productRepository)
    }
  }

}
