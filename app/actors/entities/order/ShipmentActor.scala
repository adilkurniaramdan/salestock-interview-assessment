package actors.entities.order

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.event.LoggingReceive

/**
  * Created by adildramdan on 11/18/17.
  */
class ShipmentActor extends Actor with ActorLogging {
  import ShipmentActor._

  def receive = LoggingReceive {
    case m: RequestShipmentID  =>
      sender() ! ResponseShipmentID(m.source, m.data, UUID.randomUUID().toString)
  }
}

object ShipmentActor {
  final val Name  = "shipment-actor"

  case class RequestShipmentID(source: ActorRef, data: RequestShipment)

  case class ResponseShipmentID(source: ActorRef, data: RequestShipment, shipmentId: String)
}
