package functionalspec
import actors.entities.order.OrderDetail
import com.mohiva.play.silhouette.test._
import models.dto.DtoMapperFormats._
import models.dto.Page
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
class OrderControllerCanceledSpec extends PlaySpec with Context with BaseData with GuiceOneAppPerSuite {


  "OrderControllerCanceledSpec" should {
    val product       = dataProduct().copy(qty = 100)
    val coupon        = dataCoupon().copy(qty = 100)
    val order         = dataOrder().copy(shipment = None, shipmentId = None, userId = user.email)

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
    "return Ok for Cancel with valid data" in {
      val request     = Cancel(order.id)
      val cancel      = route(
        application,
        FakeRequest(POST, "/api/v1/order/cancel")
          .withBody(toJson(request))
          .withAuthenticator[DefaultEnv](admin.loginInfo)
      ).get
      status(cancel) mustBe Status.OK
      contentType(cancel) mustBe None
    }
    "return Ok for GetOrder with valid data" in {
      val response              = List(OrderDetail(order, OrderStatus.OrderCanceled, 9000))
      val get                   = route(
        application,
        FakeRequest(GET, "/api/v1/order")
          .withAuthenticator[DefaultEnv](admin.loginInfo)
      ).get
      status(get) mustBe Status.OK
      contentType(get) mustBe Some("application/json")
      contentAsJson(get) mustBe toJson(response)
    }

    "check qty of product not decreased" in {
      val response    = Page(
        data  = List(Mapper.map[Product, ProductDto](product)),
        page  = 1,
        size  = 10,
        sort  = "asc",
        sortBy= "id" ,
        total = 1,
        filter= ""
      )
      val page      = route(
        application,
        FakeRequest(GET, "/api/v1/products?page=1&size=10&sort=asc&sortBy=id&filter")
          .withAuthenticator[DefaultEnv](admin.loginInfo)
      ).get

      status(page) mustBe Status.OK
      contentType(page) mustBe Some("application/json")
      contentAsJson(page) mustBe toJson(response)
    }

    "check qty of coupon not decreased" in {
      val response    = Page(
        data  = List(Mapper.map[Coupon, CouponDto](coupon)),
        page  = 1,
        size  = 10,
        sort  = "asc",
        sortBy= "id" ,
        total = 1,
        filter= ""
      )
      val page      = route(
        application,
        FakeRequest(GET, "/api/v1/coupons?page=1&size=10&sort=asc&sortBy=id&filter")
          .withAuthenticator[DefaultEnv](admin.loginInfo)
      ).get

      status(page) mustBe Status.OK
      contentType(page) mustBe Some("application/json")
      contentAsJson(page) mustBe toJson(response)
    }
  }
}
