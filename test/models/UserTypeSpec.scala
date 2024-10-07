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
import models.UserType
import models.UserType.{Fm, Rfm, Upe}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._
import play.api.mvc.JavascriptLiteral

class UserTypeSpec extends AnyWordSpec with Matchers {

  "UserType" should {

    "UserType" should {

      "serialize to JSON correctly" in {
        Json.toJson[UserType](Upe) mustEqual JsString("Upe")
        Json.toJson[UserType](Fm) mustEqual JsString("Fm")
        Json.toJson[UserType](Rfm) mustEqual JsString("Rfm")
      }

      "deserialize from JSON correctly" in {
        JsString("Upe").as[UserType] mustEqual Upe
        JsString("Fm").as[UserType] mustEqual Fm
        JsString("Rfm").as[UserType] mustEqual Rfm
      }

      "fail to deserialize invalid JSON" in {
        val invalidJson = JsString("Invalid")

        intercept[JsResultException] {
          invalidJson.as[UserType]
        }.errors.head._2.head.message mustEqual "Invalid Source System: Invalid"
      }
    }

    "JavascriptLiteral" should {

      "convert UserType to its string representation" in {
        val jsLiteral = implicitly[JavascriptLiteral[UserType]]

        jsLiteral.to(Upe) mustEqual "Upe"
        jsLiteral.to(Fm) mustEqual "Fm"
        jsLiteral.to(Rfm) mustEqual "Rfm"
      }
    }

    "fail to deserialize invalid JSON" in {
      val invalidJson = JsString("Invalid")

      intercept[JsResultException] {
        invalidJson.as[UserType]
      }.errors.head._2.head.message mustEqual "Invalid Source System: Invalid"
    }
  }

  "JavascriptLiteral" should {

    "convert UserType to its string representation" in {
      val jsLiteral = implicitly[JavascriptLiteral[UserType]]

      jsLiteral.to(Upe) mustEqual "Upe"
      jsLiteral.to(Fm) mustEqual "Fm"
      jsLiteral.to(Rfm) mustEqual "Rfm"
    }
  }
}
