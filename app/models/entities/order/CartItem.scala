package models.entities.order

/**
  * Created by adildramdan on 11/17/17.
  */
case class CartItem(productId : Option[Long],
                    qty       : Int,
                    price     : Price)