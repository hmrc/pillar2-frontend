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
import pages.RepaymentCompletionStatus
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import utils.ViewHelpers
import views.html.repayments.RepaymentsConfirmationView

class RepaymentConfirmationControllerSpec extends SpecBase {

  val dateHelper = new ViewHelpers()

  "Repayment confirmation controller" when {

    "must return OK and the correct view for a GET" in {
      val testUserAnswers = emptyUserAnswers.setOrException(RepaymentCompletionStatus, true)
      val application     = applicationBuilder(userAnswers = Some(testUserAnswers)).build()
      val currentDate     = HtmlFormat.escape(dateHelper.getDateTimeGMT)

      running(application) {
        val request = FakeRequest(GET, controllers.repayments.routes.RepaymentConfirmationController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RepaymentsConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(currentDate.toString())(request, applicationConfig, messages(application)).toString
      }
    }

    "must redirect to recovery page when the user attempts to access the page before completing journey" in {
      val testUserAnswers = emptyUserAnswers.setOrException(RepaymentCompletionStatus, false)
      val application     = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.repayments.routes.RepaymentConfirmationController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }
  }
}
