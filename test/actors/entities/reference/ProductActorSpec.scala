package actors.entities.reference

import akka.actor.Status.Failure
import akka.testkit.ImplicitSender
import testsupport.{ActorSpec, BaseData}
import exceptions.ObjectNotFoundException
import models.dto.Page
import org.easymock.EasyMock._
import org.scalatest.easymock.EasyMockSugar
import repositories.reference.ProductRepository

import scala.concurrent.ExecutionContext
import scala.concurrent.Future.{successful => future}

/**
  * Created by adildramdan on 11/17/17.
  */
class ProductActorSpec extends ActorSpec with BaseData with EasyMockSugar {
  implicit val ec: ExecutionContext = system.dispatcher

  "A ProductActor " must {
    "Reply ResponsePage for RequestPage message with valid data " in {
      val page                = Page(List(dataProduct()), 1, 10, "asc", "id", 1, "")
      val productRepository   = mock[ProductRepository]

      expecting{
        productRepository
          .page(anyInt(), anyInt(), anyString(), anyString(), anyString())
          .andReturn(future(page))
      }
      replay(productRepository)
      val productActor  = system.actorOf(ProductActor.props(productRepository))

      productActor ! ProductActor.RequestPage(1, 10, "asc", "id", "")
      expectMsg(ProductActor.ResponsePage(page))

      verify(productRepository)
    }

    "Reply Created for Create message with valid data " in {
      val product             = dataProduct()
      val productRepository   = mock[ProductRepository]
      expecting{
        productRepository
          .insert(anyObject(classOf[models.entities.reference.Product]))
          .andReturn(future(product))
      }
      replay(productRepository)
      val productActor  = system.actorOf(ProductActor.props(productRepository))
      productActor ! ProductActor.Create(product.name, product.description, product.qty, product.unitPrice)

      expectMsg(ProductActor.Created(product))
      verify(productRepository)
    }


    "Reply  GetResponse for Get message with valid data" in {
      val product                    = dataProduct()
      val productRepository          = mock[ProductRepository]

      expecting{
        productRepository.findById(anyLong()).andReturn(future(Some(product)))
      }
      replay(productRepository)
      val productActor   = system.actorOf(ProductActor.props(productRepository))
      productActor ! ProductActor.Get(idLong)
      expectMsg(ProductActor.GetResponse(Some(product)))

      verify(productRepository)
    }

    "Reply  GetResponse(None) for Get message with invalid data" in {
      val productRepository          = mock[ProductRepository]

      expecting{
        productRepository.findById(anyLong()).andReturn(future(None))
      }
      replay(productRepository)
      val productActor   = system.actorOf(ProductActor.props(productRepository))
      productActor ! ProductActor.Get(idLong)
      expectMsg(ProductActor.GetResponse(None))

      verify(productRepository)
    }

    "Reply Updated for Update message with valid data " in {
      val product                    = dataProduct()
      val productRepository          = mock[ProductRepository]

      expecting{
        productRepository.findById(anyLong()).andReturn(future(Some(product)))
        productRepository.update(anyLong(), anyObject(classOf[models.entities.reference.Product])).andReturn(future(product))
      }
      replay(productRepository)
      val productActor   = system.actorOf(ProductActor.props(productRepository))
      productActor ! ProductActor.Update(idLong, product.name, product.description, product.qty, product.unitPrice)
      expectMsg(ProductActor.Updated(product))

      verify(productRepository)
    }

    "Reply ObjectNotFoundException for Update message with invalid data " in {
      val product                    = dataProduct()
      val productRepository          = mock[ProductRepository]
      expecting{
        productRepository.findById(anyLong()).andReturn(future(None))
      }
      replay(productRepository)
      val productActor   = system.actorOf(ProductActor.props(productRepository))
      productActor ! ProductActor.Update(idLong, product.name, product.description, product.qty, product.qty)
      expectMsg(Failure(ObjectNotFoundException(s"Product with id $idLong not found")))

      verify(productRepository)
    }

    "Reply Deleted for Delete message with valid data" in {
      val productRepository           = mock[ProductRepository]
      expecting{
        productRepository.delete(anyLong()).andReturn(future(true))
      }
      replay(productRepository)
      val productActor   = system.actorOf(ProductActor.props(productRepository))
      productActor ! ProductActor.Delete(idLong)
      expectMsg(ProductActor.Deleted(success = true))

      verify(productRepository)
    }
  }

}
