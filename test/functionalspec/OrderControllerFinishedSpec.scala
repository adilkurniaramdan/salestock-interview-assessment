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
import play.api.libs.json.Json._
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, contentType, _}
import services.application.RandomService
import testsupport.{BaseData, FakeRandomServiceImpl}
import utils.Constants.OrderStatus
import utils.Mapper
/**
  * Created by adildramdan on 11/19/17.
  */
class OrderControllerFinishedSpec extends PlaySpec with BaseData with GuiceOneAppPerSuite {

  val application = new GuiceApplicationBuilder()
    .loadConfig(env => Configuration.load(env))
    .overrides(bind[RandomService].to(new FakeRandomServiceImpl("THIS_IS_RANDOM_RESULT")))
    .build()

  "OrderControllerFinishedSpec" should {
    val product       = dataProduct().copy(qty = 100)
    val coupon        = dataCoupon().copy(qty = 100)
    val order         = dataOrder()

    "return Created populate data product" in {
      val request     = ProductForm.Create(product.name, product.description, product.qty, product.unitPrice)
      val response    = Mapper.map[Product, ProductDto](product)
      val create      = route(application, FakeRequest(POST, "/api/v1/products").withBody(toJson(request))).get

      status(create) mustBe Status.CREATED
      contentType(create) mustBe Some("application/json")
      contentAsJson(create) mustBe toJson(response)
    }

    "return Created populate data coupon" in {
      val request     = CouponForm.Create(coupon.name, coupon.description, coupon.amount, coupon.rate, coupon.qty, coupon.start, coupon.end)
      val response    = Mapper.map[Coupon, CouponDto](coupon)
      val create      = route(application, FakeRequest(POST, "/api/v1/coupons").withBody(toJson(request))).get

      status(create) mustBe Status.CREATED
      contentType(create) mustBe Some("application/json")
      contentAsJson(create) mustBe toJson(response)
    }

    "return Created populate data cart" in {
      val request     = Add(10)
      val add         = route(application, FakeRequest(POST, "/api/v1/cart/"+ product.id.get+"/add").withBody(toJson(request))).get
      status(add) mustBe Status.CREATED
      contentType(add) mustBe None
    }

    "return Ok for Submit with valid data" in {
      val request     = Submit(order.coupon.flatMap(_.code), order.payment, order.info)
      val submit         = route(application, FakeRequest(POST, "/api/v1/order/submit").withBody(toJson(request))).get
      status(submit) mustBe Status.OK
      contentType(submit) mustBe None
    }
    "return Ok for RequestVerification with valid data" in {
      val request     = RequestVerification(order.id, order.paymentProof.get)
      val requestVerification = route(application, FakeRequest(POST, "/api/v1/order/request-verification").withBody(toJson(request))).get
      status(requestVerification) mustBe Status.OK
      contentType(requestVerification) mustBe None
    }
    "return Ok for Verify with valid data" in {
      val request     = Verify(order.id)
      val verify      = route(application, FakeRequest(POST, "/api/v1/order/verify").withBody(toJson(request))).get
      status(verify) mustBe Status.OK
      contentType(verify) mustBe None
    }
    "return Ok for RequestShipment with valid data" in {
      val request               = RequestShipment(order.id, order.shipment.get)
      val requestShipment       = route(application, FakeRequest(POST, "/api/v1/order/request-shipment").withBody(toJson(request))).get
      status(requestShipment) mustBe Status.OK
      contentType(requestShipment) mustBe None
    }
    "return Ok for Finish with valid data" in {
      val request               = Finish(order.id)
      val finish                = route(application, FakeRequest(POST, "/api/v1/order/finish").withBody(toJson(request))).get
      status(finish) mustBe Status.OK
      contentType(finish) mustBe None
    }
    "return Ok for GetOrder with valid data" in {
      val response              = List(OrderDetail(order, OrderStatus.OrderFinish, 9000))
      val get                   = route(application, FakeRequest(GET, "/api/v1/order")).get
      status(get) mustBe Status.OK
      contentType(get) mustBe Some("application/json")
      contentAsJson(get) mustBe toJson(response)
    }
  }
}
