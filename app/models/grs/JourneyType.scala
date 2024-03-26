/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.grs

import play.api.libs.json._
import play.api.mvc.JavascriptLiteral

sealed trait JourneyType extends Product with Serializable

object JourneyType {
  case object Reg extends JourneyType {
    val value: String = this.toString
  }
  case object Rfm extends JourneyType {
    val value: String = this.toString
  }

  implicit val format: Format[JourneyType] = new Format[JourneyType] {
    override def reads(json: JsValue): JsResult[JourneyType] =
      json.as[String] match {
        case "Upe" => JsSuccess[JourneyType](Reg)
        case "Rfm" => JsSuccess[JourneyType](Rfm)

        case other => JsError(s"Invalid Source System: $other")
      }

    override def writes(sourceSystem: JourneyType): JsValue =
      sourceSystem match {
        case Reg => JsString("Reg")
        case Rfm => JsString("Rfm")

      }
  }

  implicit val jsLiteral: JavascriptLiteral[JourneyType] = new JavascriptLiteral[JourneyType] {
    override def to(value: JourneyType): String =
      value match {
        case Reg => "Reg"
        case Rfm => "Rfm"

      }
  }

}
