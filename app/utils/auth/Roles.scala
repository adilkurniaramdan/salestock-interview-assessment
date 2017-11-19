package utils.auth

import play.api.libs.functional.syntax._
import play.api.libs.json._


/**
  * Created by adildramdan on 5/26/17.
  */
object Roles {

  sealed abstract class Role(val name: String)

  case object AdminRole extends Role("admin")

  case object UserRole extends Role("user")

  object Role {
    def fromString(s: String): Role = s match {
      case "admin"  => AdminRole
      case "user"   => UserRole
    }
    def values = Seq(AdminRole, UserRole)
  }


  val roles = Seq[Role](AdminRole, UserRole)

  /* JSON implicits */
  val roleReads: Reads[Role] = __.read[String].map { s =>
    roles.find { r => r.name.equals(s) }.getOrElse(UserRole)
  }
  val roleWrites: Writes[Role] = __.write[String].contramap { (role: Role) =>
    role.name
  }
  implicit val jsonFormat = Format(roleReads, roleWrites)

}

