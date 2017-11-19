package models.dto.reference

import models.entities.Price
import play.api.libs.json.Json

/**
  * Created by adildramdan on 11/19/17.
  */
case class ProductDto(id           : Option[Long]  = None,
                      name         : String,
                      description  : String,
                      qty          : Int,
                      unitPrice    : Price)

object ProductDto {
  implicit val productDtoJsonFormat = Json.format[ProductDto]
}
