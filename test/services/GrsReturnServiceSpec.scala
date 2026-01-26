/*
 * Copyright 2026 HM Revenue & Customs
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

package services

import base.SpecBase
import models.grs.EntityType
import models.registration.IncorporatedEntityRegistrationData
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class GrsReturnServiceSpec extends SpecBase {

  private val service =
    new GrsReturnService(
      userAnswersConnectors = mockUserAnswersConnectors,
      incorporatedEntityIdentificationFrontendConnector = mockIncorporatedEntityIdentificationFrontendConnector,
      partnershipIdentificationFrontendConnector = mockPartnershipIdentificationFrontendConnector,
      auditService = mockAuditService
    )

  private given HeaderCarrier = HeaderCarrier()

  "GrsReturnService.continueUpe" should {
    "redirect to the task list when the limited company registration is REGISTERED with a safeId" in {
      val journeyId = "journey-1"
      val data      = Json.parse(validRegistrationWithIdResponse).as[IncorporatedEntityRegistrationData]

      when(mockIncorporatedEntityIdentificationFrontendConnector.getJourneyData(journeyId))
        .thenReturn(Future.successful(data))
      when(mockUserAnswersConnectors.save(any(), any[JsValue])(using any[HeaderCarrier]))
        .thenReturn(Future.successful(Json.obj()))

      val result = service.continueUpe(journeyId, EntityType.UkLimitedCompany, emptyUserAnswers).futureValue

      result.header.status mustBe SEE_OTHER
      result.header.headers(LOCATION) mustBe controllers.routes.TaskListController.onPageLoad.url
    }

    "redirect to the GRS registration not called page when identifiersMatch is false" in {
      val journeyId = "journey-2"
      val data      = Json.parse(registrationNotCalledLimited).as[IncorporatedEntityRegistrationData]

      when(mockIncorporatedEntityIdentificationFrontendConnector.getJourneyData(journeyId))
        .thenReturn(Future.successful(data))

      val result = service.continueUpe(journeyId, EntityType.UkLimitedCompany, emptyUserAnswers).futureValue

      result.header.status mustBe SEE_OTHER
      result.header.headers(LOCATION) mustBe controllers.routes.GrsRegistrationNotCalledController.onPageLoadUpe.url
    }
  }
}

