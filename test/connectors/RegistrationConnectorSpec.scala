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

import base.{SpecBase, WireMockServerHandler}
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.SafeId
import org.scalacheck.Gen
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import scala.collection.Seq

class RegistrationConnectorSpec extends SpecBase with WireMockServerHandler {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.pillar2.port" -> server.port()
    )
    .build()

  lazy val connector: RegistrationConnector = app.injector.instanceOf[RegistrationConnector]

  val apiUrl = "/report-pillar2-top-up-taxes/registration"
  private val errorCodes: Gen[Int] = Gen.oneOf(Seq(400, 403, 500, 501, 502, 503, 504))

  "RegistrationConnector" when {
    "must return safeId when Upe Registerwithout Id is successful" in {

      stubResponse(s"$apiUrl/id", OK, businessWithoutIdJsonResponse)
      val result = connector.upeRegisterationWithoutID("id", userAnswersData("id", Json.obj("Registration" -> validNoIdRegData())))
      result.futureValue mustBe Right(Some(SafeId("XE1111123456789")))
    }

    "must return InternalServerError when Upe Registerwithout Id is successful" in {

      stubResponse(s"$apiUrl/id", OK, businessWithoutIdMissingSafeIdJson)
      val result = connector.upeRegisterationWithoutID("id", userAnswersData("id", Json.obj("Registration" -> validNoIdRegData())))
      result.futureValue mustBe Right(None)
    }
    "must return InternalServerError when EIS returns Error status" in {
      val errorStatus: Int = errorCodes.sample.value
      stubResponse(s"$apiUrl/id", errorStatus, businessWithoutIdJsonResponse)

      val result = connector.upeRegisterationWithoutID("id", userAnswersData("id", Json.obj("Registration" -> validNoIdRegData())))
      result.futureValue mustBe Left(models.InternalServerError)
    }
  }

  private def stubResponse(expectedEndpoint: String, expectedStatus: Int, expectedBody: String): StubMapping =
    server.stubFor(
      post(urlEqualTo(s"$expectedEndpoint"))
        .willReturn(
          aResponse()
            .withStatus(expectedStatus)
            .withBody(expectedBody)
        )
    )

}
