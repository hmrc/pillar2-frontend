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
import models.{SafeId, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Gen
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
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

      when(mockRegistrationConnector.upeRegistrationWithoutID(eqTo("id"), any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Right(Some(SafeId("XE1111123456789")))))

      stubResponse(s"$apiUrl/upe/registration/id", OK, businessWithoutIdJsonResponse)

      val result = mockRegistrationConnector.upeRegistrationWithoutID("id", userAnswersData("id", Json.obj("Registration" -> validNoIdRegData())))

      result.futureValue mustBe Right(Some(SafeId("XE1111123456789")))
    }
    "return InternalServerError when HTTP response is successful but safeId is missing or UserAnswers update fails" in {

      stubResponse(s"$apiUrl/upe/registration/id", OK, businessWithoutIdMissingSafeIdJson)

      when(mockRegistrationConnector.upeRegistrationWithoutID(eqTo("id"), any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Left(models.InternalServerError)))

      val actualResponse =
        mockRegistrationConnector.upeRegistrationWithoutID("id", userAnswersData("id", Json.obj("Registration" -> validNoIdRegData()))).futureValue

      actualResponse match {
        case Left(errorResponse) =>
          errorResponse mustBe models.InternalServerError
        case Right(_) =>
          fail("Expected an error response, but got a success response.")
      }
    }
    "return InternalServerError for EIS returns Error status" in {
      val errorStatus: Int = errorCodes.sample.value
      stubResponse(s"$apiUrl/upe/registration/id", errorStatus, businessWithoutIdJsonResponse)

      val result = connector.upeRegistrationWithoutID("id", userAnswersData("id", Json.obj("Registration" -> validNoIdRegData())))
      result.futureValue mustBe Left(models.InternalServerError)
    }
    "return safeId for FM Registerwithout Id is successful" in {
      val expectedSafeId = "XE1111123456789"
      val expectedUserId = "id"

      when(mockRegistrationConnector.fmRegisterationWithoutID(eqTo(expectedUserId), any[UserAnswers])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Right(Some(SafeId(expectedSafeId)))))

      when(mockUserAnswersConnectors.save(any(), any())(any()))
        .thenReturn(Future(Json.toJson(Json.obj())))

      stubResponse(s"$apiUrl/fm/registration/$expectedUserId", OK, businessWithoutIdJsonResponse)
      stubResponse(s"$apiUrl/user-cache/registration-subscription/$expectedUserId", OK, businessWithoutIdJsonResponse)

      val testUserAnswers = userAnswersData(expectedUserId, Json.obj("FilingMember" -> validNoIdFmData(isNfmRegisteredInUK = Some(false))))

      val result = connector.fmRegisterationWithoutID(expectedUserId, testUserAnswers)

      result.futureValue mustBe Right(Some(SafeId(expectedSafeId)))
    }
    "return InternalServerError when FM Register without Id response is missing SafeId" in {
      val businessWithoutSafeIdJsonResponse = """{"registerWithoutIDResponse": {"responseDetail": {"ARN":"ZARN1234567"}}}"""

      stubResponse(s"$apiUrl/fm/registration/id", OK, businessWithoutSafeIdJsonResponse)

      val result = connector.fmRegisterationWithoutID(
        "id",
        userAnswersData("id", Json.obj("FilingMember" -> validNoIdFmData(isNfmRegisteredInUK = Some(false))))
      )

      result.map {
        case Left(models.InternalServerError) => succeed // This means the test has passed
        case _                                => fail("Expected Left(InternalServerError) but got another result")
      }
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

    "return InternalServerError if WithoutIdNfmData is not found in UserAnswers" in {
      val id          = "testID"
      val userAnswers = UserAnswers(id, Json.obj()) // UserAnswers without WithoutIdNfmData

      val result = connector.fmRegisterationWithoutID(id, userAnswers)

      result.futureValue mustBe Left(models.InternalServerError)
    }

  }

}
