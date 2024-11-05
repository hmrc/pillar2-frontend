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

package controllers.repayments

import base.SpecBase
import models.UserAnswers
import models.repayments.RepaymentsStatus._
import pages.{RepaymentsStatusPage, RepaymentsWaitingRoomVisited}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.repayments.RepaymentsWaitingRoomView

class RepaymentsWaitingRoomControllerSpec extends SpecBase {

  "RepaymentsWaitingRoom Controller" when {

    "return OK and the correct view for a GET if RepaymentsStatusPage is empty" in {

      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, routes.RepaymentsWaitingRoomController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[RepaymentsWaitingRoomView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(None)(request, appConfig(), messages(application)).toString
      }
    }

    " redirect to registration confirmation page if database state is updated successfully after page refresh" in {
      val ua: UserAnswers = emptyUserAnswers
        .setOrException(RepaymentsStatusPage, SuccessfullyCompleted)
        .setOrException(RepaymentsWaitingRoomVisited, true)
      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, routes.RepaymentsWaitingRoomController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.RepaymentConfirmationController.onPageLoad().url
      }
    }

    " redirect to repayment error page in case of a unexpected error response after page refresh" in {
      val ua: UserAnswers = emptyUserAnswers
        .setOrException(RepaymentsStatusPage, UnexpectedResponseError)
        .setOrException(RepaymentsWaitingRoomVisited, true)
      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, routes.RepaymentsWaitingRoomController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.RepaymentErrorController.onPageLoadRepaymentSubmissionFailed.url
      }
    }

    " redirect to repayments incomplete data page in case of incomplete data error responses after page refresh" in {
      val ua: UserAnswers = emptyUserAnswers
        .setOrException(RepaymentsStatusPage, IncompleteDataError)
        .setOrException(RepaymentsWaitingRoomVisited, true)
      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, routes.RepaymentsWaitingRoomController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.RepaymentsIncompleteDataController.onPageLoad.url
      }
    }

  }
}
