package testsupport

import actors.entities.cart.Item
import models.entities.order._
import models.entities.reference.{Coupon, Product}
import models.forms.reference.ProductForm
import utils.Constants.{PaymentMethod, Rate}
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
    Some("THIS_IS_RANDOM_RESULT"),
    "Name",
    "Description",
    1000,
    Rate.Nominal,
    1,
    JodaHelper.localDateParse("01/01/2017"),
    JodaHelper.localDateParse("31/12/2017")
  )

  def dataOrder() = Order(
    "THIS_IS_RANDOM_RESULT",
    "USER_ID",
    List(Item(dataProduct().copy(qty = 100), 10)),
    Some(OrderCoupon(Some(idLong), Some("THIS_IS_RANDOM_RESULT"), "Name", "Description", 1000, Rate.Nominal, JodaHelper.localDateParse("01/01/2017"), JodaHelper.localDateParse("31/12/2017"))),
    OrderInformation("Name", "Phone", "Email", "Address"),
    Payment(PaymentMethod.BankTransfer, Some("Name")),
    Some("PAYMENT_PROOF"),
    Some(OrderShipment("JNE")),
    Some("THIS_IS_RANDOM_RESULT")
  )



}
