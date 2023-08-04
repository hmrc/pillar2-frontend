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

package controllers.registration

import base.SpecBase
import connectors.{IncorporatedEntityIdentificationFrontendConnector, PartnershipIdentificationFrontendConnector, UserAnswersConnectors}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.inject.bind

import scala.concurrent.Future

class GrsReturnControllerSpec extends SpecBase {

  "GrsReturn Controller" when {

    "must return 303 redirect to the next page with UK Limited company for UPE" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithIdForLimitedComp))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[IncorporatedEntityIdentificationFrontendConnector].toInstance(mockIncorporatedEntityIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        when(mockIncorporatedEntityIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponse))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueUpe("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url
      }

    }

    "must return 303 redirect to the next page with Limited Liability Partnership for UPE" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithIdForLLP))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[PartnershipIdentificationFrontendConnector].toInstance(mockPartnershipIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        when(mockPartnershipIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponseForLLP))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueUpe("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url

      }

    }

    "must return 303 redirect to the next page with UK Limited company for Filing Member" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithIdForLimitedCompForFm))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[IncorporatedEntityIdentificationFrontendConnector].toInstance(mockIncorporatedEntityIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        when(mockIncorporatedEntityIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponse))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueFm("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url
      }

    }

    "must return 303 redirect to the next page with Limited Liability Partnership for Filing Member" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithIdForLLPForFm))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[PartnershipIdentificationFrontendConnector].toInstance(mockPartnershipIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        when(mockPartnershipIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponseForLLP))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueFm("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url

      }

    }
  }
}
