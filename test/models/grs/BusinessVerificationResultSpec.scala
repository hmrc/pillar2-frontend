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

import org.scalatestplus.play.PlaySpec
import play.api.libs.json._

class BusinessVerificationResultSpec extends PlaySpec {

  "VerificationStatus" should {

    "deserialize 'PASS' as Pass" in {
      val json = JsString("PASS")
      json.validate[VerificationStatus].asOpt.value mustEqual VerificationStatus.Pass
    }

    "deserialize 'FAIL' as Fail" in {
      val json = JsString("FAIL")
      json.validate[VerificationStatus].asOpt.value mustEqual VerificationStatus.Fail
    }

    "deserialize 'UNCHALLENGED' as Unchallenged" in {
      val json = JsString("UNCHALLENGED")
      json.validate[VerificationStatus].asOpt.value mustEqual VerificationStatus.Unchallenged
    }

    "deserialize 'CT_ENROLLED' as CtEnrolled" in {
      val json = JsString("CT_ENROLLED")
      json.validate[VerificationStatus].asOpt.value mustEqual VerificationStatus.CtEnrolled
    }

    "deserialize 'SA_ENROLLED' as SaEnrolled" in {
      val json = JsString("SA_ENROLLED")
      json.validate[VerificationStatus].asOpt.value mustEqual VerificationStatus.SaEnrolled
    }

    "return an error for an invalid status" in {
      val json = JsString("INVALID_STATUS")
      json.validate[VerificationStatus] mustEqual JsError("Invalid VerificationStatus")
    }

    "serialize Pass as 'PASS'" in {
      val json = Json.toJson(VerificationStatus.Pass: VerificationStatus)
      json mustEqual JsString("PASS")
    }

    "serialize Fail as 'FAIL'" in {
      val json = Json.toJson(VerificationStatus.Fail: VerificationStatus) // Explicitly cast to VerificationStatus
      json mustEqual JsString("FAIL")
    }

    "serialize Unchallenged as 'UNCHALLENGED'" in {
      val json = Json.toJson(VerificationStatus.Unchallenged: VerificationStatus)
      json mustEqual JsString("UNCHALLENGED")
    }

    "serialize CtEnrolled as 'CT_ENROLLED'" in {
      val json = Json.toJson(VerificationStatus.CtEnrolled: VerificationStatus)
      json mustEqual JsString("CT_ENROLLED")
    }

    "serialize SaEnrolled as 'SA_ENROLLED'" in {
      val json = Json.toJson(VerificationStatus.SaEnrolled: VerificationStatus)
      json mustEqual JsString("SA_ENROLLED")
    }
  }

  "BusinessVerificationResult" should {

    "serialize and deserialize correctly" in {
      val result = BusinessVerificationResult(VerificationStatus.Pass)
      val json   = Json.toJson(result)
      json.as[BusinessVerificationResult] mustEqual result
    }
  }
}
