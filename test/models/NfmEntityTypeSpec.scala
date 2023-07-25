package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class NfmEntityTypeSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "NfmEntityType" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(NfmEntityType.values.toSeq)

      forAll(gen) { nfmEntityType =>
        JsString(nfmEntityType.toString).validate[NfmEntityType].asOpt.value mustEqual nfmEntityType
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!NfmEntityType.values.map(_.toString).contains(_))

      forAll(gen) { invalidValue =>
        JsString(invalidValue).validate[NfmEntityType] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(NfmEntityType.values.toSeq)

      forAll(gen) { nfmEntityType =>
        Json.toJson(nfmEntityType) mustEqual JsString(nfmEntityType.toString)
      }
    }
  }
}
