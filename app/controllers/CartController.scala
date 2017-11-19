package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.entities.cart._
import actors.entities.reference.ProductActor
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import exceptions.ObjectNotFoundException
import models.entities.reference.Product
import models.forms.order.CartForm
import play.api.Configuration
import play.api.libs.json.Json._
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.ResponseUtil

import scala.concurrent.ExecutionContext
import scala.concurrent.Future.{successful => future}
import scala.concurrent.duration._

/**
  * Created by adildramdan on 11/19/17.
  */
@Singleton
class CartController @Inject()(cc                        : ControllerComponents,
                                 configuration           : Configuration,
                                 responseUtil            : ResponseUtil,
                                 @Named(CartManager.Name)
                                 cartManager             : ActorRef,
                                 @Named(ProductActor.Name)
                                 productActor            : ActorRef)(implicit ec: ExecutionContext, m: Materializer) extends AbstractController(cc) {
  implicit val timeout  = Timeout(100 seconds)


  def add(productId: Long) = Action.async(parse.json)(implicit r =>
    r.body.validate[CartForm.Add].fold(responseUtil.error(classOf[CartForm.Add]), data =>
      for{
        _       <- checkCartAvailability("userId")
        product <- lookUpProduct(productId)
        _       <- addProductToCart("userId", product, data.qty)
      } yield {
        Created
      }
    )
  )

  private def checkCartAvailability(userId: String) = {
    (cartManager ? CartManager.Check(userId))
      .flatMap {
        case CartManager.Exists         =>
          future()
        case CartManager.DoesNotExists  =>
          (cartManager ? CartManager.Create(userId))
            .mapTo[CartManager.Created]
            .map(_ => ())
      }
  }

  private def lookUpProduct(productId: Long) = {
    (productActor ? ProductActor.Get(productId))
      .mapTo[ProductActor.GetResponse]
      .map(_.product.getOrElse(throw ObjectNotFoundException("Product not found")))
  }

  private def addProductToCart(userId: String, product: Product, qty: Int) = {
    (cartManager ? CartManager.Execute(userId, AddProductToCart(product, qty)))
      .mapTo[ProductAddedToCart]
  }

  def remove(productId: Long) = Action.async(parse.json)(implicit r =>
    r.body.validate[CartForm.Remove].fold(responseUtil.error(classOf[CartForm.Remove]), data =>
        for{
          product <- lookUpProduct(productId)
          cart    <- removeItemFromCart(product, data.qty)
        } yield {
          Ok
        }
    )
  )

  private def removeItemFromCart(product: Product, qty: Int) = {
    (cartManager ? CartManager.Execute("userId", RemoveProductFromCart(product, qty)))
      .mapTo[ProductRemovedFromCart]
  }

  def clear() = Action.async(parse.empty) (implicit r =>
    (cartManager ? CartManager.Execute("userId", ClearProduct))
      .map(_ => Ok)
  )

  def get() = Action.async(parse.empty) (implicit r =>
    (cartManager ? CartManager.Execute("userId", GetProduct))
      .mapTo[ResponseProduct]
      .map(_.products)
      .map(toJson(_))
      .map(Ok(_))
  )
}
