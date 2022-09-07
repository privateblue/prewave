package prewave

import java.time.ZonedDateTime

case class Term(
    id: Int,
    target: Int,
    text: String,
    language: String,
    keepOrder: Boolean
)

case class Alert(
    id: String,
    contents: List[Content],
    date: ZonedDateTime,
    inputType: String
)

case class Content(
    text: String,
    contentType: Option[String],
    language: String
)
