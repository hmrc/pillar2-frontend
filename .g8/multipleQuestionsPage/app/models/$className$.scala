package models

import play.api.libs.json._

case class $className$ ($field1Name$: String, $field2Name$: String)

object $className$ {
  given format = Json.format[$className$]
}
