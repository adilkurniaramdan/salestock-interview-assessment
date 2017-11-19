package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.entities.reference.ProductActor
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import models.dto.DtoMapperFormats._
import models.dto.Page
import models.dto.reference.ProductDto
import models.entities.reference.Product
import models.forms.reference.ProductForm
import play.api.Configuration
import play.api.libs.json.Json._
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.{Mapper, ResponseUtil}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * Created by adildramdan on 11/19/17.
  */
@Singleton
class ProductController @Inject()(cc                      : ControllerComponents,
                                  configuration           : Configuration,
                                  responseUtil            : ResponseUtil,
                                  @Named(ProductActor.Name)
                                  productActor            : ActorRef)(implicit ec: ExecutionContext, m: Materializer) extends AbstractController(cc) {
  implicit val timeout  = Timeout(100 seconds)

    def create() = Action.async(parse.json)(implicit r =>
      r.body.validate[ProductForm.Create].fold(responseUtil.error(classOf[ProductForm.Create]), data =>
        (productActor ? ProductActor.Create(data.name, data.description, data.qty, data.unitPrice))
          .mapTo[ProductActor.Created]
          .map(_.product)
          .map(Mapper.map[Product, ProductDto])
          .map(toJson(_))
          .map(Created(_))
      )
    )

  def update(id: Long) = Action.async(parse.json)(implicit r =>
    r.body.validate[ProductForm.Update].fold(responseUtil.error(classOf[ProductForm.Update]), data =>
      (productActor ? ProductActor.Update(id, data.name, data.description, data.qty, data.unitPrice))
        .mapTo[ProductActor.Updated]
        .map(_.product)
        .map(Mapper.map[Product, ProductDto])
        .map(toJson(_))
        .map(Ok(_))
    )
  )

  def get(id: Long) = Action.async(parse.empty) (implicit r =>
    (productActor ? ProductActor.Get(id))
      .mapTo[ProductActor.GetResponse]
      .map(_.product)
      .map(_.map(Mapper.map[Product, ProductDto]))
      .map(responseUtil.option)
  )

  def delete(id: Long) = Action.async(parse.empty) (implicit r =>
    (productActor ? ProductActor.Delete(id))
      .mapTo[ProductActor.Deleted]
      .map(_.success)
      .map(responseUtil.boolean)
  )

  def page(page: Int, size: Int, sort: String, sortBy: String, filter: String) =
    Action.async(parse.empty) (implicit r =>
      (productActor ? ProductActor.RequestPage(page, size, sort, sortBy, filter))
        .mapTo[ProductActor.ResponsePage]
        .map(response =>
          Page(response.data.map(Mapper.map[Product, ProductDto]), page, size, sort, sortBy, response.total, filter)
        )
        .map(toJson(_))
        .map(Ok(_))
    )
}
