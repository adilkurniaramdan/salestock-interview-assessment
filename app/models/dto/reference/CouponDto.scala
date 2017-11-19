package models.dto.reference

import models.entities.{Amount, Price}
import org.joda.time.LocalDate
import play.api.libs.json.Json
import utils.RestJsonFormatExt

/**
  * Created by adildramdan on 11/19/17.
  */
case class CouponDto(id          : Option[Long]  = None,
                     code        : Option[String],
                     name        : String,
                     description : String,
                     amount      : Amount,
                     rate        : String,
                     qty         : Int,
                     start       : LocalDate,
                     end         : LocalDate)

object CouponDto extends RestJsonFormatExt{
  implicit val couponDtoJsonFormat = Json.format[CouponDto]
}
