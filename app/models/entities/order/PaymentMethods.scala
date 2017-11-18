package models.entities.order

import exceptions.ObjectNotFoundException
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, Reads, Writes, _}

/**
  * Created by adildramdan on 11/18/17.
  */

object PaymentMethods {

  sealed abstract class PaymentMethod(val name: String)

  case object BankTransfer extends PaymentMethod("bank-transfer")
  

  object PaymentMethod {
    def fromString(s: String): PaymentMethod = s match {
      case "bank-transfer"                     => BankTransfer
    }
    def values = Seq(
      BankTransfer
    )
  }

  val payments = Seq[PaymentMethod](
    BankTransfer
  )

  /* JSON implicits */
  val rateReads: Reads[PaymentMethod] = __.read[String].map { s =>
    payments.find { r => r.name.equals(s) }.getOrElse(throw ObjectNotFoundException("payment-unknown"))
  }
  val rateWrites: Writes[PaymentMethod] = __.write[String].contramap { (rate: PaymentMethod) =>
    rate.name
  }
  implicit val jsonFormat = Format(rateReads, rateWrites)

}


