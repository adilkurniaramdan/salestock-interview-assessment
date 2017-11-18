package models.entities.order

import models.entities.Price
import play.api.libs.json.Json

/**
  * Created by adildramdan on 11/17/17.
  */
case class CartItem(productId : Option[Long],
                    qty       : Int,
                    price     : Price)


object CartItem {
  implicit val cartItemJsonFormat = Json.format[CartItem]
}