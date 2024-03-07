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

import base.SpecBase
import play.api.libs.json.Json

class EnrolmentInfoSpec extends SpecBase {

  "EnrolmentInfo" must {
    "create correct EnrolmentRequest for No-ID journey" in {
      val json = Json.parse("""{
          | "identifiers":[{"key":"PLRID","value":"XYPLR0022000302"}],
          | "verifiers":[{"key":"NonUKPostalCode","value":"123456789"}, {"key":"CountryCode","value":"US"}]}
        | """.stripMargin)

      val enrolmentInfo =
        EnrolmentInfo(ctUtr = None, crn = None, nonUkPostcode = Some("123456789"), countryCode = Some("US"), plrId = "XYPLR0022000302")

      val enrolmentRequest = enrolmentInfo.convertToEnrolmentRequest
      Json.toJson(enrolmentRequest) mustBe json
    }
    "create correct EnrolmentRequest for ID journey" in {
      val json = Json.parse("""{
                              | "identifiers":[{"key":"PLRID","value":"XYPLR0022000301"}],
                              | "verifiers":[{"key":"CTUTR","value":"1234567890"}, {"key":"CRN","value":"12345678"}]}
                              | """.stripMargin)
      val enrolmentInfo =
        EnrolmentInfo(ctUtr = Some("1234567890"), crn = Some("12345678"), nonUkPostcode = None, countryCode = None, plrId = "XYPLR0022000301")

      val enrolmentRequest = enrolmentInfo.convertToEnrolmentRequest
      Json.toJson(enrolmentRequest) mustBe json
    }
  }

}
