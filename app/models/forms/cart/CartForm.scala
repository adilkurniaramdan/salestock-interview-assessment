package models.forms.cart

import play.api.libs.json.Json
import utils.RestJsonFormatExt

/**
  * Created by adildramdan on 11/19/17.
  */
object CartForm {
  case class Add(qty      : Int)

  object Add extends RestJsonFormatExt{
    implicit val addJsonFormat   = Json.format[Add]
  }

  case class Remove(qty      : Int)

  object Remove extends RestJsonFormatExt{
    implicit val removeJsonFormat   = Json.format[Remove]
  }
}
