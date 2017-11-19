package functionalspec
import actors.entities.order.OrderDetail
import com.mohiva.play.silhouette.test._
import models.dto.DtoMapperFormats._
import models.dto.reference.{CouponDto, ProductDto}
import models.entities.reference.{Coupon, Product}
import models.forms.cart.CartForm.Add
import models.forms.order.OrderForm._
import models.forms.reference.{CouponForm, ProductForm}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.libs.json.Json._
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, contentType, _}
import testsupport.{BaseData, Context}
import utils.Constants.OrderStatus
import utils.Mapper
import utils.auth.DefaultEnv
/**
  * Created by adildramdan on 11/19/17.
  */
class OrderControllerFinishedSpec extends PlaySpec with Context with BaseData with GuiceOneAppPerSuite {

  "OrderControllerFinishedSpec" should {
    val product       = dataProduct().copy(qty = 100)
    val coupon        = dataCoupon().copy(qty = 100)
    val order         = dataOrder().copy(userId = user.email)

    "return Created populate data product" in {
      val request     = ProductForm.Create(product.name, product.description, product.qty, product.unitPrice)
      val response    = Mapper.map[Product, ProductDto](product)
      val create      = route(
        application,
        FakeRequest(POST, "/api/v1/products")
          .withBody(toJson(request))
          .withAuthenticator[DefaultEnv](admin.loginInfo)
      ).get

      status(create) mustBe Status.CREATED
      contentType(create) mustBe Some("application/json")
      contentAsJson(create) mustBe toJson(response)
    }

    "return Created populate data coupon" in {
      val request     = CouponForm.Create(coupon.name, coupon.description, coupon.amount, coupon.rate, coupon.qty, coupon.start, coupon.end)
      val response    = Mapper.map[Coupon, CouponDto](coupon)
      val create      = route(
        application,
        FakeRequest(POST, "/api/v1/coupons")
          .withBody(toJson(request))
          .withAuthenticator[DefaultEnv](admin.loginInfo)
      ).get

      status(create) mustBe Status.CREATED
      contentType(create) mustBe Some("application/json")
      contentAsJson(create) mustBe toJson(response)
    }

    "return Created populate data cart" in {
      val request     = Add(10)
      val add         = route(
        application,
        FakeRequest(POST, "/api/v1/cart/"+ product.id.get+"/add")
          .withBody(toJson(request))
          .withAuthenticator[DefaultEnv](user.loginInfo)
      ).get
      status(add) mustBe Status.CREATED
      contentType(add) mustBe None
    }

    //    1.  Customer can add product to an order
    //    2.  Customer can apply one coupon to order, only one coupon can be applied to order
    //    3.  Customer can submit an order and the order is finalized
    //    4.  Customer can only pay via bank transfer
    //    5.  When placing order the following data is required: name, phone number, email, address
    //    6.  When an order is submitted, the quantity for ordered product will be reduced based on the quantity.
    //    7.  When an order is submitted, the quantity of the coupon will be reduced based on the applied coupon
    //    8.  An order is successfully submitted if fulfills all of the following condition:
    //          - Applied coupon is valid
    //          - All ordered products is available
    "return Ok for Submit with valid data" in {
      val request     = Submit(order.coupon.flatMap(_.code), order.payment, order.info)
      val submit         = route(
        application,
        FakeRequest(POST, "/api/v1/order/submit")
          .withBody(toJson(request))
          .withAuthenticator[DefaultEnv](user.loginInfo)
      ).get
      status(submit) mustBe Status.OK
      contentType(submit) mustBe None
    }

    //9.  After an order is submitted, customer will be required to submit payment proof
    "return Ok for RequestVerification with valid data" in {
      val request     = RequestVerification(order.id, order.paymentProof.get)
      val requestVerification = route(
        application,
        FakeRequest(POST, "/api/v1/order/request-verification")
          .withBody(toJson(request))
          .withAuthenticator[DefaultEnv](user.loginInfo)
      ).get
      status(requestVerification) mustBe Status.OK
      contentType(requestVerification) mustBe None
    }
    //10. After an order is submitted, the order is accessible to admin and ready to be processed
    //12. Admin can verify the validity of order data: customer name, phone, email, address, payment proof
    //      - Given an order is valid, then Admin will prepare the ordered items for shipment
    //      - (OrderControllerCanceledSpec) Given and order is invalid, then Admin can cancel the order

    "return Ok for Verify with valid data" in {
      val request     = Verify(order.id)
      val verify      = route(
        application,
        FakeRequest(POST, "/api/v1/order/verify")
          .withBody(toJson(request))
          .withAuthenticator[DefaultEnv](admin.loginInfo)
      ).get
      status(verify) mustBe Status.OK
      contentType(verify) mustBe None
    }
    //13. After an order ready for shipment, Admin ship process ordered items via logistic partner
    //14. After shipping the ordered items via logistic partner, Admin will mark the order as shipped and update the order with Shipping ID
    "return Ok for RequestShipment with valid data" in {
      val request               = RequestShipment(order.id, order.shipment.get)
      val requestShipment       = route(
        application,
        FakeRequest(POST, "/api/v1/order/request-shipment")
          .withBody(toJson(request))
          .withAuthenticator[DefaultEnv](admin.loginInfo)
      ).get
      status(requestShipment) mustBe Status.OK
      contentType(requestShipment) mustBe None
    }
    "return Ok for Finish with valid data" in {
      val request               = Finish(order.id)
      val finish                = route(
        application,
        FakeRequest(POST, "/api/v1/order/finish").withBody(toJson(request))
          .withAuthenticator[DefaultEnv](admin.loginInfo)
      ).get
      status(finish) mustBe Status.OK
      contentType(finish) mustBe None
    }

    //11.  Admin can view order detail
    "return Ok for GetOrder with valid data" in {
      val response              = List(OrderDetail(order, OrderStatus.OrderFinish, 9000))
      val get                   = route(
        application,
        FakeRequest(GET, "/api/v1/order")
          .withAuthenticator[DefaultEnv](admin.loginInfo)
      ).get
      status(get) mustBe Status.OK
      contentType(get) mustBe Some("application/json")
      contentAsJson(get) mustBe toJson(response)
    }
    //15. Customer can check the order status for the submitted order
    "return Ok for GetOrderByUserLoggedIn with valid data" in {
      val response              = List(OrderDetail(order, OrderStatus.OrderFinish, 9000))
      val get                   = route(
        application,
        FakeRequest(GET, "/api/v1/order-by-user")
          .withAuthenticator[DefaultEnv](user.loginInfo)
      ).get
      status(get) mustBe Status.OK
      contentType(get) mustBe Some("application/json")
      contentAsJson(get) mustBe toJson(response)
    }
    //15. Customer can check the order status for the submitted order
    "return Ok for GetByID with valid data" in {
      val response              = OrderDetail(order, OrderStatus.OrderFinish, 9000)
      val get                   = route(
        application,
        FakeRequest(GET, "/api/v1/order-by-id/"+order.id)
          .withAuthenticator[DefaultEnv](user.loginInfo)
      ).get
      status(get) mustBe Status.OK
      contentType(get) mustBe Some("application/json")
      contentAsJson(get) mustBe toJson(response)
    }
    //16. Customer can check the shipment status for the submitted order using Shipping ID
    "return Ok for GetByShippingID with valid data" in {
      val response              = OrderDetail(order, OrderStatus.OrderFinish, 9000)
      val get                   = route(
        application,
        FakeRequest(GET, "/api/v1/order-by-shipping-id/"+order.shipmentId.get)
          .withAuthenticator[DefaultEnv](user.loginInfo)
      ).get
      status(get) mustBe Status.OK
      contentType(get) mustBe Some("application/json")
      contentAsJson(get) mustBe toJson(response)
    }
  }
}
