package utils.helpers

import base.BaseSpec
import utils.Constants.DatePattern
import utils.helpers.JodaHelper._
/**
  * Created by adildramdan on 11/17/17.
  */
class JodaHelperSpec extends BaseSpec{

  "An JodaHelper " must {
    "localDateTimeParse with valid data" in {
      val dateStr = "01/01/2017 00:00:00"
      val date    = localDateTimeParse("01/01/2017 00:00:00")
      assert(dateStr == date.toString(DatePattern.DATE_TIME_SLASH))
    }

    "localDateParse with valid data" in {
      val dateStr = "01/01/2017"
      val date    = localDateParse("01/01/2017")
      println(date.toString(DatePattern.DATE_SLASH))
      assert(dateStr == date.toString(DatePattern.DATE_SLASH))
    }
  }
}

