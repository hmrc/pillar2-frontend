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

package utils

import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue}
import play.api.mvc.JavascriptLiteral

sealed trait RegistrationType extends Product with Serializable

object RegistrationType {
  case object WithId extends RegistrationType {
    val value: String = this.toString
  }
  case object NoId extends RegistrationType {
    val value: String = this.toString
  }

  implicit val format: Format[RegistrationType] = new Format[RegistrationType] {
    override def reads(json: JsValue): JsResult[RegistrationType] =
      json.as[String] match {
        case "WithId" => JsSuccess[RegistrationType](WithId)
        case "NoId"   => JsSuccess[RegistrationType](NoId)
        case other    => JsError(s"Invalid Source System: $other")
      }

    override def writes(sourceSystem: RegistrationType): JsValue =
      sourceSystem match {
        case WithId => JsString("WithId")
        case NoId   => JsString("NoId")

      }
  }

  implicit val jsLiteral: JavascriptLiteral[RegistrationType] = new JavascriptLiteral[RegistrationType] {
    override def to(value: RegistrationType): String =
      value match {
        case WithId => "WithId"
        case NoId   => "NoId"
      }
  }

}
