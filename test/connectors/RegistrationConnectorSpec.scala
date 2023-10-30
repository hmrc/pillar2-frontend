/*
 * Copyright 2023 HM Revenue & Customs
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

package connectors

import base.SpecBase
import models.{NonUKAddress, SafeId, UKAddress, UserAnswers}
import org.scalacheck.Gen
import pages._
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}

import java.time.Instant

class RegistrationConnectorSpec extends SpecBase {

  val businessWithoutIdJsonResponse: String =
    """
| {
  |
  "registerWithoutIDResponse": {
    | "responseCommon": {
    | "status": "OK",
    | "processingDate": "2010-12-19T09:30:47Z",
    | "returnParameters": [
    | {
    | "paramName": "SAP_NUMBER", "paramValue": "9876543210"
    |}]
    |},
    | "responseDetail": {
    | "SAFEID": "XE1111123456789",
    | "ARN": "ZARN7654321"
    |}}
}
""".stripMargin

  val businessWithoutIdMissingSafeIdJson: String =
    """
| {
  |
  "registerWithoutIDResponse": {
    | "responseCommon": {
    | "status": "OK",
    | "processingDate": "2010-12-19T09:30:47Z",
    | "returnParameters": [
    | {
    | "paramName": "SAP_NUMBER", "paramValue": "0123456789"
    |}]
    |},
    | "responseDetail": {
    | "ARN": "ZARN1234567"
    |}}
}
""".stripMargin

  val businessSubscriptionSuccessJson: String =
    """
| {
  |
  "success": {
    | "plrReference": "XMPLR0012345678",
    | "formBundleNumber": "119000004320",
    | "processingDate": "2023-09-22"
    |}
  |
}
""".stripMargin

  val businessSubscriptionMissingPlrRefJson: String =
    """
    |{
    |"formBundleNumber":"119000004320",
    |"processingDate":"2023-09-22"
    |}""".stripMargin

  def userAnswersData(id: String, jsonObj: JsObject): UserAnswers = UserAnswers(id, jsonObj, Instant.ofEpochSecond(1))
  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.pillar2.port" -> server.port()
    )
    .build()

  lazy val connector: RegistrationConnector = app.injector.instanceOf[RegistrationConnector]

  val apiUrl = "/report-pillar2-top-up-taxes"
  private val errorCodes: Gen[Int] = Gen.oneOf(Seq(400, 403, 500, 501, 502, 503, 504))
  val ukAddress = UKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = "m19hgs",
    countryCode = "AB"
  )
  private val noIDUpeData =
    emptyUserAnswers
      .set(upeNameRegistrationPage, "name")
      .success
      .value
      .set(upeRegisteredInUKPage, false)
      .success
      .value
      .set(upeRegisteredAddressPage, ukAddress)
      .success
      .value
      .set(upeContactNamePage, "contactName")
      .success
      .value
      .set(upeContactEmailPage, "some@email.com")
      .success
      .value
      .set(upePhonePreferencePage, true)
      .success
      .value
      .set(upeCapturePhonePage, "12312321")
      .success
      .value
  val nonUkAddress = NonUKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = None,
    countryCode = "AB"
  )
  val nfmNoID = emptyUserAnswers
    .set(NominateFilingMemberPage, true)
    .success
    .value
    .set(fmRegisteredInUKPage, false)
    .success
    .value
    .set(fmNameRegistrationPage, "name")
    .success
    .value
    .set(fmRegisteredAddressPage, nonUkAddress)
    .success
    .value
    .set(fmContactNamePage, "contactName")
    .success
    .value
    .set(fmContactEmailPage, "some@email.com")
    .success
    .value
    .set(fmPhonePreferencePage, true)
    .success
    .value
    .set(fmCapturePhonePage, "12312321")
    .success
    .value
  "RegistrationConnector" when {
    "return safeId for Upe Register without Id is successful" in {

      stubResponse(s"$apiUrl/upe/registration/id", OK, businessWithoutIdJsonResponse)
      val result = connector.upeRegisterationWithoutID("id", userAnswersData("id", Json.obj("Registration" -> noIDUpeData)))
      result.futureValue mustBe Right(Some(SafeId("XE1111123456789")))
    }

    "return InternalServerError for Upe Register without Id is successful" in {

      stubResponse(s"$apiUrl/upe/registration/id", OK, businessWithoutIdMissingSafeIdJson)
      val result = connector.upeRegisterationWithoutID("id", userAnswersData("id", Json.obj("Registration" -> noIDUpeData)))
      result.futureValue mustBe Right(None)
    }
    "return InternalServerError for EIS returns Error status" in {
      val errorStatus: Int = errorCodes.sample.value
      stubResponse(s"$apiUrl/upe/registration/id", errorStatus, businessWithoutIdJsonResponse)

      val result = connector.upeRegisterationWithoutID("id", userAnswersData("id", Json.obj("Registration" -> noIDUpeData)))
      result.futureValue mustBe Left(models.InternalServerError)
    }
    "return safeId for FM Registerwithout Id is successful" in {

      stubResponse(s"$apiUrl/fm/registration/id", OK, businessWithoutIdJsonResponse)
      val result = connector.fmRegisterationWithoutID(
        "id",
        userAnswersData("id", Json.obj("FilingMember" -> nfmNoID))
      )
      result.futureValue mustBe Right(Some(SafeId("XE1111123456789")))
    }

    "return InternalServerError for FM Register without Id is successful" in {

      stubResponse(s"$apiUrl/fm/registration/id", OK, businessWithoutIdMissingSafeIdJson)
      val result = connector.fmRegisterationWithoutID(
        "id",
        userAnswersData("id", Json.obj("FilingMember" -> nfmNoID))
      )
      result.futureValue mustBe Right(None)
    }
    "return InternalServerError for EIS returns Error status for FM register withoutId" in {
      val errorStatus: Int = errorCodes.sample.value
      stubResponse(s"$apiUrl/fm/registration/id", errorStatus, businessWithoutIdJsonResponse)

      val result = connector.fmRegisterationWithoutID(
        "id",
        userAnswersData("id", Json.obj("FilingMember" -> nfmNoID))
      )
      result.futureValue mustBe Left(models.InternalServerError)
    }
  }

}
