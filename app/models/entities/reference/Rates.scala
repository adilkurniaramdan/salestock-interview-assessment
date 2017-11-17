package models.entities.reference
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, Reads, Writes, _}

/**
  * Created by adildramdan on 11/17/17.
  */

object Rates {

  sealed abstract class Rate(val name: String)

  case object PercentageRate extends Rate("percentage")

  case object NominalRate extends Rate("nominal")
  
  object Rate {
    def fromString(s: String): Rate = s match {
      case "percentage"   => PercentageRate
      case "nominal"      => NominalRate
    }
    def values = Seq(PercentageRate, NominalRate)
  }


  val rates = Seq[Rate](PercentageRate, NominalRate)

  /* JSON implicits */
  val rateReads: Reads[Rate] = __.read[String].map { s =>
    rates.find { r => r.name.equals(s) }.getOrElse(NominalRate)
  }
  val rateWrites: Writes[Rate] = __.write[String].contramap { (rate: Rate) =>
    rate.name
  }
  implicit val jsonFormat = Format(rateReads, rateWrites)

}


