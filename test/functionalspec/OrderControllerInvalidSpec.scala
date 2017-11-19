package functionalspec
import actors.entities.order.OrderDetail
import models.dto.DtoMapperFormats._
import models.dto.reference.{CouponDto, ProductDto}
import models.entities.reference.{Coupon, Product}
import models.forms.cart.CartForm.Add
import models.forms.order.OrderForm._
import models.forms.reference.{CouponForm, ProductForm}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import com.mohiva.play.silhouette.test._
import play.api.libs.json.Json._
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, contentType, _}
import services.application.RandomService
import testsupport.{BaseData, Context, FakeRandomServiceImpl}
import utils.Constants.{ErrorCode, OrderStatus}
import utils.Mapper
import utils.auth.DefaultEnv
/**
  * Created by adildramdan on 11/19/17.
  */
class OrderControllerInvalidSpec extends PlaySpec with Context with BaseData with GuiceOneAppPerSuite {

  "OrderControllerInvalidSpec" should {
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

    "return BadRequest for Submit with invalid coupon code" in {
      val request     = Submit(Some("INVALID_COUPON_CODE"), order.payment, order.info)
      val submit         = route(
        application,
        FakeRequest(POST, "/api/v1/order/submit")
          .withBody(toJson(request))
          .withAuthenticator[DefaultEnv](user.loginInfo)
      ).get
      status(submit) mustBe Status.BAD_REQUEST
      contentType(submit) mustBe Some("application/json")
      contentAsJson(submit) mustBe obj("code" -> ErrorCode.InvalidData, "message" -> "Coupon is not valid")
    }

    "return Created populate again data cart" in {
      val request     = Add(1000)
      val add         = route(
        application,
        FakeRequest(POST, "/api/v1/cart/"+ product.id.get+"/add")
          .withBody(toJson(request))
          .withAuthenticator[DefaultEnv](user.loginInfo)
      ).get
      status(add) mustBe Status.CREATED
      contentType(add) mustBe None
    }

    "return BadRequest for Submit with qty not enough" in {
      val request       = Submit(order.coupon.flatMap(_.code), order.payment, order.info)
      val submit         = route(
        application,
        FakeRequest(POST, "/api/v1/order/submit").withBody(toJson(request))
          .withAuthenticator[DefaultEnv](user.loginInfo)
      ).get
      status(submit) mustBe Status.BAD_REQUEST
      contentType(submit) mustBe Some("application/json")
      contentAsJson(submit) mustBe obj("code" -> ErrorCode.InvalidData, "message" -> "Not all item available")
    }
  }
}
