package models.dto

import play.api.libs.json.Json.toJson
import play.api.libs.json._
/**
  * Created by adildramdan on 11/17/17.
  */
case class Page[T](data: List[T], page: Int, size: Int, sort: String, sortBy: String, total: Int, filter: String)

object Page {
  implicit def pageWrites[T](implicit fmt: Writes[T]): Writes[Page[T]] = new Writes[Page[T]] {
    def writes(p: Page[T]) = JsObject(Seq(
      "data" -> JsArray(p.data.map(toJson(_))),
      "page" -> JsNumber(p.page),
      "size" -> JsNumber(p.size),
      "sort" -> JsString(p.sort),
      "sortBy" -> JsString(p.sortBy),
      "total" -> JsNumber(p.total),
      "filter" -> JsString(p.filter)
    ))
  }
}

