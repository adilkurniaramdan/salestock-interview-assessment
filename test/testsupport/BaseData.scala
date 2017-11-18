package testsupport

import models.entities.reference.Rates.NominalRate
import models.entities.reference.{Coupon, Product}
import utils.helpers.JodaHelper

/**
  * Created by adildramdan on 11/17/17.
  */
trait BaseData {

  val idLong  = 1

  def dataProduct() = Product(
    Some(idLong),
    "Name",
    "Description",
    1,
    1000
  )

  def dataCoupon() = Coupon(
    Some(idLong),
    Some("CODECOUPON"),
    "Name",
    "Description",
    10000,
    NominalRate,
    1,
    JodaHelper.localDateParse("01/01/2017"),
    JodaHelper.localDateParse("31/12/2017")
  )
}
