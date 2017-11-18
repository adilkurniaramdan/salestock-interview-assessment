package actors.entities.reference

import actors.entities.cart.Item
import actors.entities.order.Submit
import akka.actor.ActorRef
import models.entities.order.OrderCoupon
import utils.OriginalSender



case class ValidateCoupon(source: ActorRef, data: Submit, items: List[Item], code: String) extends OriginalSender

object ValidateCoupon {

  sealed abstract class Response(response: ValidateCoupon) extends OriginalSender {

    lazy val source = response.source
  }

  case class Successful(response: ValidateCoupon, orderCoupon: OrderCoupon) extends Response(response)

  case class Unsuccessful(response: ValidateCoupon, reason: String) extends Response(response)

}
