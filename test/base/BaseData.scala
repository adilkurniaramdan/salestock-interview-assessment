package base

import models.entities.reference.Product

/**
  * Created by adildramdan on 11/17/17.
  */
trait BaseData {

  val idLong  = 1

  def dataProduct() = Product(
    Some(idLong),
    "Name",
    "Description",
    1
  )
}
