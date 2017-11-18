package actors.entities.reference

import actors.entities.cart.Item
import actors.entities.order.Submit
import akka.actor.ActorRef
import utils.OriginalSender



case class ValidateItem(source: ActorRef, data: Submit, items: List[Item]) extends OriginalSender

object ValidateItem {

  sealed abstract class Response(response: ValidateItem) extends OriginalSender {

    lazy val source = response.source
  }

  case class Successful(response: ValidateItem) extends Response(response)

  case class Unsuccessful(response: ValidateItem, reason: String) extends Response(response)

}
