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

package models

import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue}
import play.api.mvc.JavascriptLiteral

sealed trait UserType extends Product with Serializable

object UserType {
  case object Upe extends UserType {
    val value: String = this.toString
  }
  case object Fm extends UserType {
    val value: String = this.toString
  }

  case object Rfm extends UserType {
    val value: String = this.toString
  }

  implicit val format: Format[UserType] = new Format[UserType] {
    override def reads(json: JsValue): JsResult[UserType] =
      json.as[String] match {
        case "Upe" => JsSuccess[UserType](Upe)
        case "Fm"  => JsSuccess[UserType](Fm)
        case "Rfm" => JsSuccess[UserType](Rfm)

        case other => JsError(s"Invalid Source System: $other")
      }

    override def writes(sourceSystem: UserType): JsValue =
      sourceSystem match {
        case Upe => JsString("Upe")
        case Fm  => JsString("Fm")
        case Rfm => JsString("Rfm")
      }
  }

  implicit val jsLiteral: JavascriptLiteral[UserType] = new JavascriptLiteral[UserType] {
    override def to(value: UserType): String =
      value match {
        case Upe => "Upe"
        case Fm  => "Fm"
        case Fm  => "Rfm"
      }
  }

}
