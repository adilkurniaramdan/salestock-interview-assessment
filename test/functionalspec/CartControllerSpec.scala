package functionalspec
import actors.entities.cart.Item
import models.dto.DtoMapperFormats._
import models.dto.reference.ProductDto
import models.entities.reference.Product
import models.forms.cart.CartForm.{Add, Remove}
import models.forms.reference.ProductForm
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
import utils.Mapper
/**
  * Created by adildramdan on 11/19/17.
  */
class CartControllerSpec extends PlaySpec with BaseData with GuiceOneAppPerSuite {

  val application = new GuiceApplicationBuilder()
    .loadConfig(env => Configuration.load(env))
    .overrides(bind[RandomService].to(new FakeRandomServiceImpl("")))
    .build()

  "CartControllerSpec" should {
    val product     = dataProduct().copy(qty = 100)
    "return Created for Create Product with valid data" in {
      val request     = ProductForm.Create(product.name, product.description, product.qty, product.unitPrice)
      val response    = Mapper.map[Product, ProductDto](product)
      val create      = route(application, FakeRequest(POST, "/api/v1/products").withBody(toJson(request))).get

      status(create) mustBe Status.CREATED
      contentType(create) mustBe Some("application/json")
      contentAsJson(create) mustBe toJson(response)
    }
    "return Created for Add with valid data" in {
      val request     = Add(5)
      val add      = route(application, FakeRequest(POST, "/api/v1/cart/"+ product.id.get+"/add").withBody(toJson(request))).get
      status(add) mustBe Status.CREATED
      contentType(add) mustBe None
    }
    "return Ok for Update with valid data" in {
      val request     = Remove(1)
      val update      = route(application, FakeRequest(POST, "/api/v1/cart/"+ product.id.get+"/remove").withBody(toJson(request))).get
      status(update) mustBe Status.OK
      contentType(update) mustBe None
    }

    "return Ok for Get with valid data" in {
      val response    = List(Item(product, 4))
      val get         = route(application, FakeRequest(GET, "/api/v1/cart")).get
      status(get) mustBe Status.OK

      contentType(get) mustBe Some("application/json")
      contentAsJson(get) mustBe toJson(response)
    }

    "return Ok for Clear with valid data" in {
      val clear         = route(application, FakeRequest(DELETE, "/api/v1/cart")).get
      status(clear) mustBe Status.OK

      contentType(clear) mustBe None
    }

    "return Ok for Get after Clear with valid data" in {
      val response    = List[Item]()
      val get         = route(application, FakeRequest(GET, "/api/v1/cart")).get
      status(get) mustBe Status.OK

      contentType(get) mustBe Some("application/json")
      contentAsJson(get) mustBe toJson(response)
    }
  }

}
