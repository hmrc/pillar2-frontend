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

package controllers.btn

import base.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.ObligationsAndSubmissionsService
import utils.DateTimeUtils.LocalDateOps
import views.html.btn.BTNConfirmationView

import java.time.LocalDate
import scala.concurrent.Future

class BTNConfirmationControllerSpec extends SpecBase {

  "BTNConfirmationController" when {

    "onPageLoad" should {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), subscriptionLocalData = Some(someSubscriptionLocalData))
          .configure("features.phase2ScreensEnabled" -> true)
          .build()

        val currentDate: String = LocalDate.now.toDateFormat
        val date:        String = someSubscriptionLocalData.subAccountingPeriod.startDate.toDateFormat

        running(application) {
          val request = FakeRequest(GET, controllers.btn.routes.BTNConfirmationController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[BTNConfirmationView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual view(Some("OrgName"), currentDate, date, isAgent = false, showUnderEnquiryWarning = false)(
            request,
            applicationConfig,
            messages(application)
          ).toString
        }
      }

      "must redirect to dashboard for onPageLoad when phase2ScreensEnabled is false" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .configure("features.phase2ScreensEnabled" -> false)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService)
          )
          .build()

        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

          val request = FakeRequest(GET, controllers.btn.routes.BTNConfirmationController.onPageLoad.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.DashboardController.onPageLoad.url
        }
      }
    }
  }
}
