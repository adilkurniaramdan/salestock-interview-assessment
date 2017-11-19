package models.forms.reference

import models.entities.Price
import play.api.libs.json.Json

/**
  * Created by adildramdan on 11/19/17.
  */
object ProductForm {

  case class Create(name         : String,
                    description  : String,
                    qty          : Int,
                    unitPrice    : Price)

  object Create {
    implicit val createJsonFormat   = Json.format[Create]
  }

  case class Update(name         : String,
                    description  : String,
                    qty          : Int,
                    unitPrice    : Price)

  object Update {
    implicit val updateJsonFormat   = Json.format[Update]
  }
}
