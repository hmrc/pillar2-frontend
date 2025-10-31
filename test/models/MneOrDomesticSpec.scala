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

import models.EntityLocationChangeResult.{EntityLocationChangeAllowed, EntityLocationChangeBlocked}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class MneOrDomesticSpec extends AnyWordSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "MneOrDomestic" must {

    "deserialise valid values" in {
      val gen: Gen[MneOrDomestic] = Gen.oneOf(MneOrDomestic.values.toSeq)

      forAll(gen) { mneOrDomestic =>
        JsString(mneOrDomestic.toString).validate[MneOrDomestic].asOpt.value mustEqual mneOrDomestic
      }
    }

    "fail to deserialise invalid values" in {
      val gen: Gen[String] = arbitrary[String] suchThat (!MneOrDomestic.values.map(_.toString).contains(_))

      forAll(gen) { invalidValue =>
        JsString(invalidValue).validate[MneOrDomestic] mustEqual JsError("error.invalid")
      }
    }

    "serialise" in {
      val gen: Gen[MneOrDomestic] = Gen.oneOf(MneOrDomestic.values.toSeq)

      forAll(gen) { mneOrDomestic =>
        Json.toJson(mneOrDomestic) mustEqual JsString(mneOrDomestic.toString)
      }
    }

    "return EntityLocationChangeBlocked when changing from UkAndOther to Uk (MultiNational Entity to Domestic)" in {
      val result: EntityLocationChangeResult =
        MneOrDomestic.handleEntityLocationChange(from = MneOrDomestic.UkAndOther, to = MneOrDomestic.Uk)

      result mustEqual EntityLocationChangeBlocked
    }

    "return EntityLocationChangeAllowed for any other transition" in {
      val allowedLocationChanges: Seq[(MneOrDomestic, MneOrDomestic)] = Seq(
        (MneOrDomestic.Uk, MneOrDomestic.UkAndOther),
        (MneOrDomestic.Uk, MneOrDomestic.Uk),
        (MneOrDomestic.UkAndOther, MneOrDomestic.UkAndOther)
      )

      forAll(Gen.oneOf(allowedLocationChanges)) { case (from, to) =>
        MneOrDomestic.handleEntityLocationChange(from, to) mustEqual EntityLocationChangeAllowed
      }
    }

  }
}
