package actors.entities.cart
import models.entities.reference.Product
/**
  * Created by adildramdan on 11/18/17.
  */

class CartState(private var items: Map[Product, Int] = Map.empty) {

  def contains(product: Product): Boolean = items.contains(product)

  def clear(): Unit = items = Map.empty

  def remove(product: Product, qty: Int): Unit = {
    val currentQty = items.get(product).map(_ - qty).filter(_ > 0).getOrElse(0)
    if (currentQty == 0) {
      items = items - product
    } else {
      items = items + (product -> currentQty)
    }
  }

  def products: List[CartItem] = items.map{case (k,v) => CartItem(k, v)}.toList

  def add(product: Product, qty: Int): Unit = {
    val newQty = items.get(product).map(_ + qty).getOrElse(qty)
    items = items + (product -> newQty)
  }
}
