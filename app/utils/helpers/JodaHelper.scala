package utils.helpers

import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.joda.time.{LocalDate, LocalDateTime}
import utils.Constants.DatePattern


/**
  * Created by adildramdan on 11/17/17.
  */
object JodaHelper {

  def localDateTimeParse(str : String)(implicit fmt: DateTimeFormatter = defaultLocalDateTimeFormatter()) =
    LocalDateTime.parse(str, fmt)

  private def defaultLocalDateTimeFormatter() =
    DateTimeFormat.forPattern(DatePattern.DATE_TIME_SLASH)

  def localDateParse(str: String)(implicit fmt: DateTimeFormatter = defaultLocalDateFormatter()) =
    LocalDate.parse(str, fmt)

  private def defaultLocalDateFormatter() =
    DateTimeFormat.forPattern(DatePattern.DATE_SLASH)

}


