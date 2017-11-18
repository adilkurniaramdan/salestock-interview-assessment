package actors.entities.reference

import akka.actor.Status.Failure
import exceptions.ObjectNotFoundException
import models.dto.Page
import org.easymock.EasyMock._
import org.scalatest.easymock.EasyMockSugar
import repositories.reference.CouponRepository
import services.application.RandomService
import testsupport.{ActorSpec, BaseData}

import scala.concurrent.ExecutionContext
import scala.concurrent.Future.{successful => future}

/**
  * Created by adildramdan on 11/17/17.
  */
class CouponActorSpec extends ActorSpec with BaseData with EasyMockSugar {
  implicit val ec: ExecutionContext = system.dispatcher

  "A CouponActor " must {
    "Reply ResponsePage for RequestPage message with valid data " in {
      val page                = Page(List(dataCoupon()), 1, 10, "asc", "id", 1, "")
      val couponRepository    = mock[CouponRepository]
      val randomService       = mock[RandomService]

      expecting{
        couponRepository
          .page(anyInt(), anyInt(), anyString(), anyString(), anyString())
          .andReturn(future(page))
      }
      replay(couponRepository)
      val couponActor  = system.actorOf(CouponActor.props(couponRepository, randomService))

      couponActor ! CouponActor.RequestPage(1, 10, "asc", "id", "")
      expectMsg(CouponActor.ResponsePage(page))

      verify(couponRepository)
    }

    "Reply Created for Create message with valid data " in {
      val coupon             = dataCoupon()
      val couponRepository   = mock[CouponRepository]
      val randomService       = mock[RandomService]

      expecting{
        couponRepository
          .findOneByCode(anyString())
          .andReturn(future(None))
        couponRepository
          .insert(anyObject(classOf[models.entities.reference.Coupon]))
          .andReturn(future(coupon))
        randomService
          .randomAlphaNumericString(anyInt())
          .andReturn(coupon.code.get)
      }
      replay(couponRepository, randomService)
      val couponActor  = system.actorOf(CouponActor.props(couponRepository, randomService))
      couponActor ! CouponActor.Create(
        coupon.name, coupon.description, coupon.amount, coupon.rate, coupon.qty, coupon.start, coupon.end
      )

      expectMsg(CouponActor.Created(coupon))
      verify(couponRepository, randomService)
    }


    "Reply  GetResponse for Get message with valid data" in {
      val coupon               = dataCoupon()
      val couponRepository     = mock[CouponRepository]
      val randomService        = mock[RandomService]
      expecting{
        couponRepository.findById(anyLong()).andReturn(future(Some(coupon)))
      }
      replay(couponRepository)
      val couponActor   = system.actorOf(CouponActor.props(couponRepository, randomService))
      couponActor ! CouponActor.Get(idLong)
      expectMsg(CouponActor.GetResponse(Some(coupon)))

      verify(couponRepository)
    }

    "Reply  GetResponse(None) for Get message with invalid data" in {
      val couponRepository      = mock[CouponRepository]
      val randomService         = mock[RandomService]
      expecting{
        couponRepository.findById(anyLong()).andReturn(future(None))
      }
      replay(couponRepository)
      val couponActor   = system.actorOf(CouponActor.props(couponRepository, randomService))

      couponActor ! CouponActor.Get(idLong)
      expectMsg(CouponActor.GetResponse(None))

      verify(couponRepository)
    }

    "Reply Updated for Update message with valid data " in {
      val coupon                    = dataCoupon()
      val couponRepository          = mock[CouponRepository]
      val randomService         = mock[RandomService]

      expecting{
        couponRepository.findById(anyLong()).andReturn(future(Some(coupon)))
        couponRepository.update(anyLong(), anyObject(classOf[models.entities.reference.Coupon])).andReturn(future(coupon))
      }
      replay(couponRepository)

      val couponActor   = system.actorOf(CouponActor.props(couponRepository, randomService))
      couponActor ! CouponActor.Update(
        idLong, coupon.name, coupon.description, coupon.amount, coupon.rate, coupon.qty, coupon.start, coupon.end
      )
      expectMsg(CouponActor.Updated(coupon))

      verify(couponRepository)
    }

    "Reply ObjectNotFoundException for Update message with invalid data " in {
      val coupon                = dataCoupon()
      val couponRepository      = mock[CouponRepository]
      val randomService         = mock[RandomService]

      expecting{
        couponRepository.findById(anyLong()).andReturn(future(None))
      }
      replay(couponRepository)
      val couponActor   = system.actorOf(CouponActor.props(couponRepository, randomService))
      couponActor ! CouponActor.Update(
        idLong, coupon.name, coupon.description, coupon.amount, coupon.rate, coupon.qty, coupon.start, coupon.end
      )
      expectMsg(Failure(ObjectNotFoundException(s"Coupon with id $idLong not found")))

      verify(couponRepository)
    }

    "Reply Deleted for Delete message with valid data" in {
      val couponRepository      = mock[CouponRepository]
      val randomService         = mock[RandomService]

      expecting{
        couponRepository.delete(anyLong()).andReturn(future(true))
      }
      replay(couponRepository)
      val couponActor   = system.actorOf(CouponActor.props(couponRepository, randomService))
      couponActor ! CouponActor.Delete(idLong)
      expectMsg(CouponActor.Deleted(success = true))

      verify(couponRepository)
    }
  }

}
