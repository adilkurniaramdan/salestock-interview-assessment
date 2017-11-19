package functionalspec
import models.dto.DtoMapperFormats._
import models.dto.Page
import models.dto.reference.CouponDto
import models.entities.reference.Coupon
import models.forms.reference.CouponForm.{Create, Update}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.application.RandomService
import testsupport.{BaseData, FakeRandomServiceImpl}
import utils.Mapper
/**
  * Created by adildramdan on 11/19/17.
  */
class CouponControllerSpec extends PlaySpec with BaseData with GuiceOneAppPerSuite {

  val application = new GuiceApplicationBuilder()
    .loadConfig(env => Configuration.load(env))
    .overrides(bind[RandomService].to[FakeRandomServiceImpl])
    .build()

  "CouponControllerSpec" should {
    "return Created for Create with valid data" in {
      val coupon      = dataCoupon()
      val request     = Create(coupon.name, coupon.description, coupon.amount, coupon.rate, coupon.qty, coupon.start, coupon.end)
      val response    = Mapper.map[Coupon, CouponDto](coupon)
      val create      = route(application, FakeRequest(POST, "/api/v1/coupons").withBody(toJson(request))).get

      status(create) mustBe Status.CREATED
      contentType(create) mustBe Some("application/json")
      contentAsJson(create) mustBe toJson(response)
    }
    "return Ok for Page with valid data" in {
      val coupon     = dataCoupon()
      val response    = Page(
        data  = List(Mapper.map[Coupon, CouponDto](coupon)),
        page  = 1,
        size  = 10,
        sort  = "asc",
        sortBy= "id" ,
        total = 1,
        filter= ""
      )
      val page      = route(application, FakeRequest(GET, "/api/v1/coupons?page=1&size=10&sort=asc&sortBy=id&filter")).get

      status(page) mustBe Status.OK
      contentType(page) mustBe Some("application/json")
      contentAsJson(page) mustBe toJson(response)
    }

    "return Ok for Update with valid data" in {
      val coupon     = dataCoupon()
      val request     = Update(coupon.name, coupon.description, coupon.amount, coupon.rate, coupon.qty, coupon.start, coupon.end)
      val response    = Mapper.map[Coupon, CouponDto](coupon)
      val update      = route(application, FakeRequest(PUT, "/api/v1/coupons/"+ coupon.id.get).withBody(toJson(request))).get

      status(update) mustBe Status.OK
      contentType(update) mustBe Some("application/json")
      contentAsJson(update) mustBe toJson(response)
    }

    "return Ok for Get with valid data" in {
      val coupon     = dataCoupon()
      val response    = Mapper.map[Coupon, CouponDto](coupon)
      val get         = route(application, FakeRequest(GET, "/api/v1/coupons/"+ coupon.id.get)).get

      status(get) mustBe Status.OK
      contentType(get) mustBe Some("application/json")
      contentAsJson(get) mustBe toJson(response)
    }

    "return Ok for Delete with valid data" in {
      val coupon     = dataCoupon()
      val delete         = route(application, FakeRequest(DELETE, "/api/v1/coupons/"+ coupon.id.get)).get

      status(delete) mustBe Status.OK
      contentType(delete) mustBe None
    }
  }

}
