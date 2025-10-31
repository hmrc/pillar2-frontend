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

package controllers

import base.SpecBase
import models.UserAnswers
import models.subscription.SubscriptionStatus._
import pages.SubscriptionStatusPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.registrationview.RegistrationWaitingRoomView

class RegistrationWaitingRoomControllerSpec extends SpecBase {

  "RegistrationWaitingRoom Controller" should {

    "return OK and the correct view for a GET if SubscriptionStatusPage is empty" in {

      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, routes.RegistrationWaitingRoomController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RegistrationWaitingRoomView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(None)(request, applicationConfig, messages(application)).toString
      }
    }
    "redirect to registration confirmation page if database state is updated successfully" in {
      val ua: UserAnswers = emptyUserAnswers.setOrException(SubscriptionStatusPage, SuccessfullyCompletedSubscription)
      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, routes.RegistrationWaitingRoomController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.RegistrationConfirmationController.onPageLoad.url
      }
    }
    "redirect to error page in case of a duplicated submission response" in {
      val ua: UserAnswers = emptyUserAnswers.setOrException(SubscriptionStatusPage, FailedWithDuplicatedSubmission)
      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, routes.RegistrationWaitingRoomController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.SubscriptionFailureController.onPageLoad.url
      }
    }

    "redirect to error page in case of a unprocessable entity response" in {
      val ua: UserAnswers = emptyUserAnswers.setOrException(SubscriptionStatusPage, FailedWithUnprocessableEntity)
      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, routes.RegistrationWaitingRoomController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.SubscriptionFailureController.onPageLoad.url
      }
    }

    "redirect to registration failed error page in case of any failed api responses" in {
      val ua: UserAnswers = emptyUserAnswers.setOrException(SubscriptionStatusPage, FailedWithInternalIssueError)
      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, routes.RegistrationWaitingRoomController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.SubscriptionFailedController.onPageLoad.url
      }
    }
    "redirect to journey recovery page if fetching data from mongo fails" in {
      val ua: UserAnswers = emptyUserAnswers.setOrException(SubscriptionStatusPage, FailedWithNoMneOrDomesticValueFoundError)
      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, routes.RegistrationWaitingRoomController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
    "redirect to duplicate safe id page in case of failed with duplicate safe id responses" in {
      val ua: UserAnswers = emptyUserAnswers.setOrException(SubscriptionStatusPage, FailedWithDuplicatedSafeIdError)
      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, routes.RegistrationWaitingRoomController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.DuplicateSafeIdController.onPageLoad.url
      }
    }

  }
}
