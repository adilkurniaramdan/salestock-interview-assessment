package functionalspec
import com.mohiva.play.silhouette.test._
import models.dto.DtoMapperFormats._
import models.dto.Page
import models.dto.reference.ProductDto
import models.entities.reference.Product
import models.forms.reference.ProductForm.{Create, Update}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.libs.json.Json._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testsupport.{BaseData, Context}
import utils.Mapper
import utils.auth.DefaultEnv
/**
  * Created by adildramdan on 11/19/17.
  */
class ProductControllerSpec extends PlaySpec with BaseData with Context with GuiceOneAppPerSuite {

  "ProductControllerSpec" should {
    "return Created for Create with valid data" in {
      val product     = dataProduct()
      val request     = Create(product.name, product.description, product.qty, product.unitPrice)
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
    "return Ok for Page with valid data" in {
      val product     = dataProduct()
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

    "return Ok for Update with valid data" in {
      val product     = dataProduct()
      val request     = Update(product.name, product.description, product.qty, product.unitPrice)
      val response    = Mapper.map[Product, ProductDto](product)
      val update      = route(
        application,
        FakeRequest(PUT, "/api/v1/products/"+ product.id.get).withBody(toJson(request))
          .withAuthenticator[DefaultEnv](admin.loginInfo)
      ).get

      status(update) mustBe Status.OK
      contentType(update) mustBe Some("application/json")
      contentAsJson(update) mustBe toJson(response)
    }

    "return Ok for Get with valid data" in {
      val product     = dataProduct()
      val response    = Mapper.map[Product, ProductDto](product)
      val get         = route(
        application,
        FakeRequest(GET, "/api/v1/products/"+ product.id.get)
          .withAuthenticator[DefaultEnv](admin.loginInfo)
      ).get

      status(get) mustBe Status.OK
      contentType(get) mustBe Some("application/json")
      contentAsJson(get) mustBe toJson(response)
    }

    "return Ok for Delete with valid data" in {
      val product     = dataProduct()
      val delete         = route(
        application,
        FakeRequest(DELETE, "/api/v1/products/"+ product.id.get)
          .withAuthenticator[DefaultEnv](admin.loginInfo)
      ).get

      status(delete) mustBe Status.OK
      contentType(delete) mustBe None
    }
  }

}
