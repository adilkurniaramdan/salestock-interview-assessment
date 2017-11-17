package models.dto

import play.api.libs.json.Json

/**
  * Created by adildramdan on 10/25/17.
  */
case class AppInfo(name: String, version: String, uptime: Long)

object AppInfo {
  implicit val appinfoJsonFormat   = Json.format[AppInfo]
}