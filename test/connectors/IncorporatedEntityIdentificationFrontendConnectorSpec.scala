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

package connectors

import base.MockitoStubUtils
import helpers.ViewInstances
import models.grs.{GrsCreateRegistrationResponse, OptServiceName, ServiceName}
import models.registration.{IncorporatedEntityCreateRegistrationRequest, IncorporatedEntityRegistrationData}
import models.{NormalMode, UserType}
import org.mockito.ArgumentMatchers.{any, eq => Meq}
import org.mockito.Mockito.{verify, when}
import org.scalatest.matchers.should.Matchers._
import play.api.libs.json.Json
import uk.gov.hmrc.http.StringContextOps

import scala.concurrent.Future

class IncorporatedEntityIdentificationFrontendConnectorSpec extends MockitoStubUtils with ViewInstances {
  private val validGrsCreateRegistrationResponse = new GrsCreateRegistrationResponse("http://journey-start")
  val apiUrl: String = s"${applicationConfig.incorporatedEntityIdentificationFrontendBaseUrl}/incorporated-entity-identification/api"
  val connector                           = new IncorporatedEntityIdentificationFrontendConnectorImpl(applicationConfig, mockHttpClient)
  private val validRegisterWithIdResponse = Json.parse(validRegistrationWithIdResponse).as[IncorporatedEntityRegistrationData]
  val serviceName: ServiceName = ServiceName(OptServiceName("Report Pillar 2 Top-up Taxes"))

  "IncorporatedEntityIdentificationFrontendConnector" should {
    "return OK status for createLimitedCompanyJourney" in {
      val expectedUrl = s"$apiUrl/limited-company-journey"
      val expectedIncorporatedEntityCreateRegistrationRequest: IncorporatedEntityCreateRegistrationRequest =
        IncorporatedEntityCreateRegistrationRequest(
          continueUrl =
            s"http://localhost:10050/report-pillar2-top-up-taxes/grs-return/${NormalMode.toString.toLowerCase}/${UserType.Upe.value.toLowerCase}",
          businessVerificationCheck = false,
          optServiceName = Some(serviceName.en.optServiceName),
          deskProServiceId = "pillar2-frontend",
          signOutUrl = "http://localhost:9553/bas-gateway/sign-out-without-state",
          accessibilityUrl = "/accessibility-statement/pillar2",
          labels = serviceName
        )

      when(executePost[GrsCreateRegistrationResponse](Json.toJson(expectedIncorporatedEntityCreateRegistrationRequest)))
        .thenReturn(Future.successful(validGrsCreateRegistrationResponse))

      val result = connector.createLimitedCompanyJourney(UserType.Upe, NormalMode).futureValue
      result shouldBe validGrsCreateRegistrationResponse
      verify(mockHttpClient).post(Meq(url"$expectedUrl"))(any())
    }

    "return OK status for createLimitedCompanyJourney for RFM" in {
      val expectedUrl = s"$apiUrl/limited-company-journey"
      val expectedIncorporatedEntityCreateRegistrationRequest: IncorporatedEntityCreateRegistrationRequest =
        IncorporatedEntityCreateRegistrationRequest(
          continueUrl =
            s"http://localhost:10050/report-pillar2-top-up-taxes/grs-return/${NormalMode.toString.toLowerCase}/${UserType.Rfm.value.toLowerCase}",
          businessVerificationCheck = false,
          optServiceName = Some(serviceName.en.optServiceName),
          deskProServiceId = "pillar2-frontend",
          signOutUrl = "http://localhost:9553/bas-gateway/sign-out-without-state",
          accessibilityUrl = "/accessibility-statement/pillar2",
          labels = serviceName
        )

      when(executePost[GrsCreateRegistrationResponse](Json.toJson(expectedIncorporatedEntityCreateRegistrationRequest)))
        .thenReturn(Future.successful(validGrsCreateRegistrationResponse))

      val result = connector.createLimitedCompanyJourney(UserType.Rfm, NormalMode).futureValue
      result shouldBe validGrsCreateRegistrationResponse
      verify(mockHttpClient).post(Meq(url"$expectedUrl"))(any())
    }

    "getJourneyData should be successful" in {
      when(executeGet[IncorporatedEntityRegistrationData]).thenReturn(Future.successful(validRegisterWithIdResponse))
      val result = connector.getJourneyData("1234")
      whenReady(result)(response => response shouldBe validRegisterWithIdResponse)
    }
  }

}
