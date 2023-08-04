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
import models.{NormalMode, UserType}
import models.grs.{GrsCreateRegistrationResponse, OptServiceName, ServiceName}
import models.registration.IncorporatedEntityCreateRegistrationRequest
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import scala.concurrent.Future

class IncorporatedEntityIdentificationFrontendConnectorSpec extends SpecBase {

  val apiUrl    = s"${appConfig.incorporatedEntityIdentificationFrontendBaseUrl}/incorporated-entity-identification/api"
  val connector = new IncorporatedEntityIdentificationFrontendConnectorImpl(appConfig, mockHttpClient)
  "IncorporatedEntityIdentificationFrontendConnector" when {

    "must return OK status for createLimitedCompanyJourney" in {
      val expectedUrl = s"$apiUrl/limited-company-journey"
      val expectedIncorporatedEntityCreateRegistrationRequest: IncorporatedEntityCreateRegistrationRequest = {
        val serviceName = ServiceName(
          OptServiceName("Report Pillar 2 top-up taxes"),
          OptServiceName("Report Pillar 2 top-up taxes")
        )

        IncorporatedEntityCreateRegistrationRequest(
          continueUrl =
            s"http://localhost:10050/report-pillar2-top-up-taxes/grs-return/${NormalMode.toString.toLowerCase}/${UserType.Upe.value.toLowerCase}",
          businessVerificationCheck = false,
          optServiceName = Some(serviceName.en.optServiceName),
          deskProServiceId = "pillar2-frontend",
          signOutUrl = "http://localhost:9553/bas-gateway/sign-out-without-state",
          accessibilityUrl = "/accessibility-statement/pillar2-frontend",
          labels = serviceName
        )
      }

      when(
        mockHttpClient.POST[IncorporatedEntityCreateRegistrationRequest, GrsCreateRegistrationResponse](
          any(),
          any(),
          any()
        )(any(), any(), any(), any())
      )
        .thenReturn(Future.successful(validGrsCreateRegistrationResponse))

      val result = connector.createLimitedCompanyJourney(UserType.Upe, NormalMode).futureValue
      result shouldBe validGrsCreateRegistrationResponse
      verify(mockHttpClient, times(1))
        .POST[IncorporatedEntityCreateRegistrationRequest, GrsCreateRegistrationResponse](
          ArgumentMatchers.eq(expectedUrl),
          ArgumentMatchers.eq(expectedIncorporatedEntityCreateRegistrationRequest),
          any()
        )(any(), any(), any(), any())

    }

  }

}
