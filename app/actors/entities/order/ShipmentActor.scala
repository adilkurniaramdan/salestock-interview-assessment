package actors.entities.order

import javax.inject.Inject

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.event.LoggingReceive
import services.application.RandomService

/**
  * Created by adildramdan on 11/18/17.
  */
class ShipmentActor @Inject()(randomService: RandomService) extends Actor with ActorLogging {
  import ShipmentActor._

  def receive = LoggingReceive {
    case m: RequestShipmentID  =>
      sender() ! ResponseShipmentID(m.source, m.data, randomService.randomAlphaNumericString(15))
  }
}

object ShipmentActor {
  final val Name  = "shipment-actor"

  case class RequestShipmentID(source: ActorRef, data: RequestShipment)

  case class ResponseShipmentID(source: ActorRef, data: RequestShipment, shipmentId: String)
}
