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
import forms.RepaymentAccountNameConfirmationForm
import models.NormalMode
import pages.{BarsAccountNamePartialPage, RepaymentAccountNameConfirmationPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.repayments.*

class RepaymentErrorControllerSpec extends SpecBase {

  val formProvider = new RepaymentAccountNameConfirmationForm

  "Not Confirmed Bank Details" must {

    "must return the correct view" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.RepaymentErrorController.onPageLoadNotConfirmedDetails.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CouldNotConfirmDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

    "must return the correct error view for a submission error" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.repayments.routes.RepaymentErrorController.onPageLoadRepaymentSubmissionFailed.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RepaymentSubmissionErrorView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, applicationConfig, messages(application)).toString
      }
    }
  }

  "Repayment Error" must {

    "must return the correct view" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.RepaymentErrorController.onPageLoadError.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RepaymentErrorView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, applicationConfig, messages(application)).toString
      }
    }
  }

  "Bank Details Error" must {

    "must return the correct view" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.RepaymentErrorController.onPageLoadBankDetailsError.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BankDetailsErrorView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }
  }

  "Partial Account Name Match" must {

    "must return the correct view" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.setOrException(BarsAccountNamePartialPage, "James")))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.RepaymentErrorController.onPageLoadPartialNameError(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AccountNameConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), "James", NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must redirect to error page if partial account name is not present" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.RepaymentErrorController.onPageLoadPartialNameError(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/repayment/error/view")
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.setOrException(BarsAccountNamePartialPage, "James")))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.RepaymentErrorController.onPageLoadPartialNameError(NormalMode).url)
            .withFormUrlEncodedBody(("", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AccountNameConfirmationView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, "James", NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

    "must redirect to input name page if valid selection is submitted" in {
      val application = applicationBuilder(userAnswers =
        Some(
          emptyUserAnswers
            .setOrException(BarsAccountNamePartialPage, "James")
        )
      )
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.RepaymentErrorController.onSubmitPartialNameError(NormalMode).url)
          .withFormUrlEncodedBody(("confirmRepaymentAccountName", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/repayment/contact-details/input-name")
      }
    }

    "must redirect to back to enter bank details page if valid selection is submitted" in {
      val application = applicationBuilder(userAnswers =
        Some(
          emptyUserAnswers
            .setOrException(BarsAccountNamePartialPage, "James")
            .setOrException(RepaymentAccountNameConfirmationPage, false)
        )
      )
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.RepaymentErrorController.onSubmitPartialNameError(NormalMode).url)
          .withFormUrlEncodedBody(("confirmRepaymentAccountName", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/repayment/uk-details")
      }
    }

    "must redirect to error page if partial account name is not present for submit" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.RepaymentErrorController.onSubmitPartialNameError(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/repayment/error/view")
      }
    }
  }
}
