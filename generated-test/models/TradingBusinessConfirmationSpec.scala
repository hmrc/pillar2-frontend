package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class TradingBusinessConfirmationSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "TradingBusinessConfirmation" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(TradingBusinessConfirmation.values.toSeq)

      forAll(gen) {
        tradingBusinessConfirmation =>

          JsString(tradingBusinessConfirmation.toString).validate[TradingBusinessConfirmation].asOpt.value mustEqual tradingBusinessConfirmation
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!TradingBusinessConfirmation.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[TradingBusinessConfirmation] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(TradingBusinessConfirmation.values.toSeq)

      forAll(gen) {
        tradingBusinessConfirmation =>

          Json.toJson(tradingBusinessConfirmation) mustEqual JsString(tradingBusinessConfirmation.toString)
      }
    }
  }
}
