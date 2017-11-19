package models.entities

import java.util.UUID

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import utils.auth.Roles.{Role, UserRole}

/**
  * Created by adildramdan on 11/19/17.
  */
case class User(userID      : UUID,
                loginInfo   : LoginInfo,
                email       : String,
                role        : Role                  = UserRole) extends Identity
