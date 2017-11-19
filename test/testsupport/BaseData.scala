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
    Some("CODECOUPON"),
    "Name",
    "Description",
    1000,
    Rate.Nominal,
    1,
    JodaHelper.localDateParse("01/01/2017"),
    JodaHelper.localDateParse("31/12/2017")
  )

  def dataOrder() = Order(
    idLong.toString,
    idLong.toString,
    List(Item(dataProduct(), 1)),
    Some(OrderCoupon(Some(idLong), Some("CODECOUPON"), "Name", "Description", 1000, Rate.Nominal, JodaHelper.localDateParse("01/01/2017"), JodaHelper.localDateParse("31/12/2017"))),
    OrderInformation("Name", "Phone", "Email", "Address"),
    Payment(PaymentMethod.BankTransfer, Some("Name")),
    Some("PAYMENT_PROOF"),
    Some(OrderShipment("JNE")),
    Some("SHIPPING-ID")
  )



}
