package utils

/**
  * Created by adildramdan on 11/17/17.
  */
object Constants {


  object DatePattern {
    val DATE_SLASH            = "dd/MM/yyyy"
    val DATE_TIME_SLASH       = "dd/MM/yyy HH:mm:ss"
  }
  object Rate {
    val Nominal               = "nominal"
    val Percentage            = "percentage"
  }
  object PaymentMethod {
    val BankTransfer          = "bank-transfer"
  }

  object OrderStatus {
    val OrderSubmitted            = "order-submitted"
    val OrderRequestVerification  = "order-request-verification"
    val OrderVerified             = "order-verified"
    val OrderShipped              = "order-shipped"
    val OrderFinish               = "order-finish"
    val OrderCanceled             = "order-canceled"
  }

  object ErrorCode {
    val JsonError               = "JSON_ERROR"
    val ValidationError         = "VALIDATION_ERROR"
    val ObjectAlreadyExist      = "OBJECT_ALREADY_EXIST"
    val AuthenticationFailed    = "AUTHENTICATION_FAILED"
    val InvalidData             = "INVALID_DATA"
    val OutOfStock              = "OUT_OF_STOCK"
    val OtherError              = "OTHER_ERROR"
  }
}
