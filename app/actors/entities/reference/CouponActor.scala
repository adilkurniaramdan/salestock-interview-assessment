package actors.entities.reference

import javax.inject.Inject

import akka.actor.{Actor, ActorLogging, Props}
import akka.event.LoggingReceive
import akka.pattern.pipe
import exceptions.ObjectNotFoundException
import models.dto.Page
import models.entities.Amount
import models.entities.order.OrderCoupon
import models.entities.reference.Coupon
import models.entities.reference.Rates.Rate
import org.joda.time.LocalDate
import repositories.reference.CouponRepository
import services.application.RandomService

import scala.concurrent.Future.{failed, successful => future}
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by adildramdan on 11/17/17.
  */
class CouponActor @Inject()(couponRepository: CouponRepository,
                            randomService   : RandomService)(implicit ec: ExecutionContext) extends Actor with ActorLogging{

  import CouponActor._
  def receive = LoggingReceive {
    case m: RequestPage =>
      requestPage(m.page, m.size, m.sort, m.sortBy, m.filter)
        .map(ResponsePage)
        .pipeTo(sender())

    case m: Create  =>
      create(m.name, m.description, m.amount, m.rate, m.qty, m.start, m.end)
        .map(Created)
        .pipeTo(sender())

    case m: Get     =>
      get(m.id)
        .map(GetResponse)
        .pipeTo(sender())

    case m: Update  =>
      update(m.id, m.name, m.description, m.amount, m.rate, m.qty, m.start, m.end)
        .map(Updated)
        .pipeTo(sender())

    case m: Delete  =>
      delete(m.id)
        .map(Deleted)
        .pipeTo(sender())

    case m: UpdateQty =>
      updateQty(m.id, m.qty)

    case m: ValidateCoupon  =>
      validateCoupon(m)
        .pipeTo(sender())

  }

  private def requestPage(page: Int, size: Int, sort: String, sortBy: String, filter: String) = {
    couponRepository.page(page, size, sort, sortBy, filter)
  }

  private def create(name : String, description : String, amount: Amount, rate: Rate, qty: Int, start: LocalDate, end: LocalDate) = {
    val coupon = Coupon(
      id          = None,
      code        = None,
      name        = name,
      description = description,
      amount      = amount,
      rate        = rate,
      qty         = qty,
      start       = start,
      end         = end
    )

    for {
      code   <- generateCouponCode()
      coupon <- couponRepository.insert(coupon.copy(code = Some(code)))
    } yield coupon
  }

  def generateCouponCode() = {
    def loop(): Future[String] = {
      val code  = randomService.randomAlphaNumericString(6).toUpperCase
      couponRepository
        .findOneByCode(code)
        .flatMap{
          case Some(_) => loop()
          case None    => future(code)
        }
    }
    loop()
  }

  private def get(id: Long) = {
    couponRepository.findById(id)
  }

  private def update(id: Long, name : String, description : String, amount: Amount, rate: Rate, qty: Int, start: LocalDate, end: LocalDate) = {
    couponRepository
      .findById(id)
      .flatMap {
        case Some(coupon)  =>
          couponRepository
            .update(
              id,
              coupon.copy(name = name, description = description, amount = amount, rate = rate, qty = qty, start = start, end = end)
            )
        case None           =>
          failed(ObjectNotFoundException(s"Coupon with id $id not found"))
      }
  }

  private def updateQty(id: Long, qty: Int) =
    couponRepository
      .findById(id)
      .flatMap {
        case Some(product)  =>
          couponRepository.update(id, product.copy(qty = product.qty + qty))
        case None           =>
          failed(ObjectNotFoundException(s"Coupon with id $id not found"))
      }

  private def delete(id: Long)  = {
    couponRepository.delete(id)
  }

  private def validateCoupon(m: ValidateCoupon) = {
    couponRepository
      .findOneByCode(m.code)
      .map{
        case Some(coupon) if coupon.start.isBefore(LocalDate.now())
          && coupon.end.isAfter(LocalDate.now())
          && coupon.qty >= 0 =>
          ValidateCoupon.Successful(
            m,
            OrderCoupon(
              coupon.id,
              coupon.code,
              coupon.name,
              coupon.description,
              coupon.amount,
              coupon.rate,
              coupon.start,
              coupon.end
            )
          )
        case _  =>
          ValidateCoupon.Unsuccessful(m, "Coupon is not valid")
      }
  }

}


object CouponActor {
  final val Name  = "coupon-actor"

  def props(couponRepository: CouponRepository, randomService: RandomService)(implicit ec: ExecutionContext) =
    Props(new CouponActor(couponRepository, randomService))

  sealed trait Command
  case class RequestPage(page: Int, size: Int, sort: String, sortBy: String, filter: String) extends Command
  case class Create(name : String, description : String, amount: Amount, rate: Rate, qty: Int, start: LocalDate, end: LocalDate) extends Command
  case class Get(id: Long) extends Command
  case class Update(id: Long, name : String, description : String, amount: Amount, rate: Rate, qty: Int, start: LocalDate, end: LocalDate) extends Command
  case class Delete(id: Long) extends Command
  case class UpdateQty(id: Long, qty: Int) extends Command

  sealed trait Event
  case class ResponsePage(page: Page[Coupon]) extends Event
  case class Created(coupon: Coupon) extends Event
  case class GetResponse(coupon: Option[Coupon]) extends Command
  case class Updated(coupon: Coupon) extends Event
  case class Deleted(success: Boolean) extends Event

}
