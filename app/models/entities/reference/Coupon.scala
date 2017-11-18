package models.entities.reference

import models.entities.Amount
import models.entities.reference.Rates.Rate
import org.joda.time.LocalDate
import play.api.libs.json.Json
import utils.RestJsonFormatExt

/**
  * Created by adildramdan on 11/17/17.
  */
case class Coupon(id          : Option[Long]  = None,
                  code        : Option[String],
                  name        : String,
                  description : String,
                  amount      : Amount,
                  rate        : Rate,
                  qty         : Int,
                  start       : LocalDate,
                  end         : LocalDate)

object Coupon extends RestJsonFormatExt{
  implicit val couponJsonFormat   = Json.format[Coupon]
}