package models.entities.order

import models.entities.Amount
import org.joda.time.LocalDate
import play.api.libs.json.Json
import utils.RestJsonFormatExt

/**
  * Created by adildramdan on 11/19/17.
  */
case class OrderCoupon(id          : Option[Long],
                       code        : Option[String],
                       name        : String,
                       description : String,
                       amount      : Amount,
                       rate        : String,
                       start       : LocalDate,
                       end         : LocalDate)

object OrderCoupon extends RestJsonFormatExt{
  implicit val orderCouponJsonFormat = Json.format[OrderCoupon]
}
