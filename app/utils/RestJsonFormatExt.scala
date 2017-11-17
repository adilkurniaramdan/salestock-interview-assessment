package utils
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone, LocalDate, LocalTime}
import play.api.libs.json.{JsString, Reads, Writes}

/**
  * Created by adildramdan on 11/17/17.
  */
trait RestJsonFormatExt {
  implicit val readsJodaLocalDateTime = Reads[DateTime](js =>
    js.validate[String].map[DateTime](dtString =>
      DateTime.parse(dtString, DateTimeFormat.forPattern("dd/MM/yyyy HH:mm"))))

  implicit val writesJodaLocalDateTime = Writes[DateTime](dt =>
    JsString(DateTimeFormat.forPattern("dd/MM/yyyy HH:mm").print(dt.toDateTime(DateTimeZone.forID("Asia/Jakarta"))))
  )

  implicit val readJodaLocalDate = Reads[LocalDate] { js =>
    js.validate[String].map[LocalDate](dtString => LocalDate.parse(dtString, DateTimeFormat.forPattern("dd/MM/yyyy")))
  }
  implicit val writesJodaLocalDate = Writes[LocalDate] { dt =>
    JsString(DateTimeFormat.forPattern("dd/MM/yyyy").print(dt.toDateTimeAtStartOfDay(DateTimeZone.forID("Asia/Jakarta"))))
  }

  implicit val readJodaLocalTime = Reads[LocalTime](js =>
    js.validate[String].map[LocalTime](t =>
      LocalTime.parse(t, DateTimeFormat.forPattern("HH:mm"))
    )
  )
  implicit val writeJodaLocalTime = Writes[LocalTime](t =>
    JsString(DateTimeFormat.forPattern("HH:mm").print(t))
  )
}

object RestJsonFormatExt extends RestJsonFormatExt
