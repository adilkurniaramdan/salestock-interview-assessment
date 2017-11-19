package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.entities.cart._
import actors.entities.reference.ProductActor
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import com.mohiva.play.silhouette.api.Silhouette
import exceptions.ObjectNotFoundException
import models.entities.reference.Product
import models.forms.cart.CartForm
import play.api.Configuration
import play.api.libs.json.Json._
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.ResponseUtil
import utils.auth.Roles.UserRole
import utils.auth.{DefaultEnv, WithRole}

import scala.concurrent.ExecutionContext
import scala.concurrent.Future.{successful => future}
import scala.concurrent.duration._

/**
  * Created by adildramdan on 11/19/17.
  */
@Singleton
class CartController @Inject()(cc                      : ControllerComponents,
                               silhouette              : Silhouette[DefaultEnv],
                               configuration           : Configuration,
                               responseUtil            : ResponseUtil,
                               @Named(CartManager.Name)
                               cartManager             : ActorRef,
                               @Named(ProductActor.Name)
                               productActor            : ActorRef)(implicit ec: ExecutionContext, m: Materializer) extends AbstractController(cc) {
  implicit val timeout  = Timeout(100 seconds)


  def add(productId: Long) =
    silhouette.SecuredAction(WithRole(UserRole)).async(parse.json)(implicit r =>
      r.body.validate[CartForm.Add].fold(responseUtil.error(classOf[CartForm.Add]), data =>
        for{
          _       <- checkCartAvailability(r.identity.email)
          product <- lookUpProduct(productId)
          _       <- addProductToCart(r.identity.email, product, data.qty)
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

  def remove(productId: Long) =
    silhouette.SecuredAction(WithRole(UserRole)).async(parse.json)(implicit r =>
      r.body.validate[CartForm.Remove].fold(responseUtil.error(classOf[CartForm.Remove]), data =>
          for{
            product <- lookUpProduct(productId)
            cart    <- removeItemFromCart(product, data.qty, r.identity.email)
          } yield {
            Ok
          }
      )
    )

  private def removeItemFromCart(product: Product, qty: Int, userId: String) = {
    (cartManager ? CartManager.Execute(userId, RemoveProductFromCart(product, qty)))
      .mapTo[ProductRemovedFromCart]
  }

  def clear() =
    silhouette.SecuredAction(WithRole(UserRole)).async(parse.empty)(implicit r =>
      (cartManager ? CartManager.Execute(r.identity.email, ClearProduct))
        .map(_ => Ok)
    )

  def get() =
    silhouette.SecuredAction(WithRole(UserRole)).async(parse.empty)(implicit r =>
      (cartManager ? CartManager.Execute(r.identity.email, GetProduct))
        .mapTo[ResponseProduct]
        .map(_.products)
        .map(toJson(_))
        .map(Ok(_))
    )
}
