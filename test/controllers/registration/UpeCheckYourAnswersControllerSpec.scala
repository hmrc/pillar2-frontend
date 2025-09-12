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

package controllers.registration

import base.SpecBase
import connectors.UserAnswersConnectors
import helpers.SectionHash
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.UpeSectionConfirmationHashPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.Future

class UpeCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar {

  "UPE no ID Check Your Answers Controller" must {

    "redirect to bookmark prevention page if all required pages have not been answered" in {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.UpeCheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }

    }
    "return ok with correct view" in {
      val application = applicationBuilder(userAnswers = Some(upeCompletedNoPhoneNumber)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.UpeCheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("Check your answers for ultimate parent details")
      }

    }

    "store confirmation hash and redirect to task list on submit" in {
      val mockSessionRepository    = mock[SessionRepository]
      val mockUserAnswersConnector = mock[UserAnswersConnectors]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockUserAnswersConnector.save(any(), any())(any())) thenReturn Future.successful(Json.toJson(Json.obj()))

      val ua              = upeCompletedNoPhoneNumber
      val expectedHash    = SectionHash.computeUpeHash(ua)
      val expectedAnswers = ua.set(UpeSectionConfirmationHashPage, expectedHash).success.value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, controllers.registration.routes.UpeCheckYourAnswersController.onSubmit.url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
        verify(mockUserAnswersConnector, times(1)).save(eqTo(expectedAnswers.id), eqTo(Json.toJson(expectedAnswers.data)))(any())
      }
    }
  }
}
