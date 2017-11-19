package models.forms.reference

import models.entities.{Amount, Price}
import org.joda.time.LocalDate
import play.api.libs.json.Json
import utils.RestJsonFormatExt

/**
  * Created by adildramdan on 11/19/17.
  */
object CouponForm {

  case class Create(name        : String,
                    description : String,
                    amount      : Amount,
                    rate        : String,
                    qty         : Int,
                    start       : LocalDate,
                    end         : LocalDate)

  object Create extends RestJsonFormatExt{
    implicit val createJsonFormat   = Json.format[Create]
  }

  case class Update(name        : String,
                    description : String,
                    amount      : Amount,
                    rate        : String,
                    qty         : Int,
                    start       : LocalDate,
                    end         : LocalDate)

  object Update extends RestJsonFormatExt{
    implicit val updateJsonFormat   = Json.format[Update]
  }
}
