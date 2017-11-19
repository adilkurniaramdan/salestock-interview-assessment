package actors.entities.cart
import models.entities.reference.Product
import play.api.libs.json.Json
/**
  * Created by adildramdan on 11/18/17.
  */
case class Item(product: Product, qty: Int)


object Item {
  implicit val itemJsonFormat = Json.format[Item]
}