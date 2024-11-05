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

package controllers.rfm

import base.SpecBase
import connectors.UserAnswersConnectors
import models.rfm.CorporatePosition
import models.rfm.RfmStatus.{FailException, FailedInternalIssueError, SuccessfullyCompleted}
import models.{NonUKAddress, UserAnswers}
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.rfm.RfmWaitingRoomView

import scala.concurrent.Future

class RfmWaitingRoomControllerSpec extends SpecBase {

  "RfmWaitingRoom Controller" when {

    "return OK and the correct view for a GET if RfmStatusPage is empty" in {
      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmWaitingRoomController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[RfmWaitingRoomView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(None)(request, appConfig(), messages(application)).toString
      }
    }

    " redirect to registration confirmation page if database state is updated successfully" in {
      val contactAddress = NonUKAddress("Address line first drive", Some("Address line 2"), "Home Town", Some("region"), Some("ne5 2dh"), "AT")
      val ua: UserAnswers = emptyUserAnswers
        .setOrException(RfmStatusPage, SuccessfullyCompleted)
        .setOrException(RfmPillar2ReferencePage, "plrReference")
        .setOrException(RfmCorporatePositionPage, CorporatePosition.Upe)
        .setOrException(RfmPrimaryContactNamePage, "name")
        .setOrException(RfmPrimaryContactEmailPage, "name@email.com")
        .setOrException(RfmContactAddressPage, contactAddress)

      val application = applicationBuilder(Some(ua))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmWaitingRoomController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmConfirmationController.onPageLoad.url
      }
    }

    " redirect to error page in case of any failed api responses" in {
      val ua: UserAnswers = emptyUserAnswers.setOrException(RfmStatusPage, FailedInternalIssueError)
      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmWaitingRoomController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.AmendApiFailureController.onPageLoad.url
      }
    }

    " redirect to journey recovery page if fetching data from mongo fails" in {
      val ua: UserAnswers = emptyUserAnswers.setOrException(RfmStatusPage, FailException)
      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmWaitingRoomController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url
      }
    }
  }
}
