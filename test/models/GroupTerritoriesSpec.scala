package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class GroupTerritoriesnSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "GroupTerritories" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(GroupTerritories.values.toSeq)

      forAll(gen) { groupTerritories =>
        JsString(groupTerritories.toString).validate[GroupTerritories].asOpt.value mustEqual groupTerritories
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!GroupTerritories.values.map(_.toString).contains(_))

      forAll(gen) { invalidValue =>
        JsString(invalidValue).validate[GroupTerritories] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(GroupTerritories.values.toSeq)

      forAll(gen) { groupTerritories =>
        Json.toJson(groupTerritories) mustEqual JsString(groupTerritories.toString)
      }
    }
  }
}
