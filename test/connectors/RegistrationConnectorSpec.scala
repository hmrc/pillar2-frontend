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
import models.SafeId
import org.scalacheck.Gen
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import scala.collection.Seq

class RegistrationConnectorSpec extends SpecBase {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.pillar2.port" -> server.port()
    )
    .build()

  lazy val connector: RegistrationConnector = app.injector.instanceOf[RegistrationConnector]

  val apiUrl = "/report-pillar2-top-up-taxes"
  private val errorCodes: Gen[Int] = Gen.oneOf(Seq(400, 403, 500, 501, 502, 503, 504))

  "RegistrationConnector" when {
    "return safeId for Upe Registerwithout Id is successful" in {

      stubResponse(s"$apiUrl/upe/registration/id", OK, businessWithoutIdJsonResponse)
      val result = connector.upeRegisterationWithoutID("id", userAnswersData("id", Json.obj("Registration" -> validNoIdRegData())))
      result.futureValue mustBe Right(Some(SafeId("XE1111123456789")))
    }

    "return InternalServerError for Upe Register without Id is successful" in {

      stubResponse(s"$apiUrl/upe/registration/id", OK, businessWithoutIdMissingSafeIdJson)
      val result = connector.upeRegisterationWithoutID("id", userAnswersData("id", Json.obj("Registration" -> validNoIdRegData())))
      result.futureValue mustBe Right(None)
    }
    "return InternalServerError for EIS returns Error status" in {
      val errorStatus: Int = errorCodes.sample.value
      stubResponse(s"$apiUrl/upe/registration/id", errorStatus, businessWithoutIdJsonResponse)

      val result = connector.upeRegisterationWithoutID("id", userAnswersData("id", Json.obj("Registration" -> validNoIdRegData())))
      result.futureValue mustBe Left(models.InternalServerError)
    }
    "return safeId for FM Registerwithout Id is successful" in {

      stubResponse(s"$apiUrl/fm/registration/id", OK, businessWithoutIdJsonResponse)
      val result = connector.fmRegisterationWithoutID(
        "id",
        userAnswersData("id", Json.obj("FilingMember" -> validNoIdFmData(isNfmRegisteredInUK = Some(false))))
      )
      result.futureValue mustBe Right(Some(SafeId("XE1111123456789")))
    }

    "return InternalServerError for FM Register without Id is successful" in {

      stubResponse(s"$apiUrl/fm/registration/id", OK, businessWithoutIdMissingSafeIdJson)
      val result = connector.fmRegisterationWithoutID(
        "id",
        userAnswersData("id", Json.obj("FilingMember" -> validNoIdFmData(isNfmRegisteredInUK = Some(false))))
      )
      result.futureValue mustBe Right(None)
    }
    "return InternalServerError for EIS returns Error status for FM register withoutId" in {
      val errorStatus: Int = errorCodes.sample.value
      stubResponse(s"$apiUrl/fm/registration/id", errorStatus, businessWithoutIdJsonResponse)

      val result = connector.fmRegisterationWithoutID(
        "id",
        userAnswersData("id", Json.obj("FilingMember" -> validNoIdFmData(isNfmRegisteredInUK = Some(false))))
      )
      result.futureValue mustBe Left(models.InternalServerError)
    }
  }

}
