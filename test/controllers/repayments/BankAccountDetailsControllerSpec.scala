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
import forms.BankAccountDetailsFormProvider
import models.NormalMode
import models.repayments.BankAccountDetails
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{BankAccountDetailsPage, BarsAccountNamePartialPage, RepaymentAccountNameConfirmationPage}
import play.api.inject
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.BarsService
import services.BarsServiceSpec.bankAccountDetails
import views.html.repayments.BankAccountDetailsView

import scala.concurrent.Future

class BankAccountDetailsControllerSpec extends SpecBase {

  val formProvider = new BankAccountDetailsFormProvider()

  "BankAccountDetails Controller" when {

    "must redirect to error page if the feature flag is false" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), additionalData = Map("features.repaymentsAccessEnabled" -> false))
        .build()
      running(application) {
        val request = FakeRequest(GET, controllers.repayments.routes.BankAccountDetailsController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/error/page-not-found")
      }
    }

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.repayments.routes.BankAccountDetailsController.onPageLoad(NormalMode).url)
        val view    = application.injector.instanceOf[BankAccountDetailsView]
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val testBankAccountDetails = BankAccountDetails("TestBankName", "TestAccountName", "112233", "12345611")
      val ua                     = emptyUserAnswers.setOrException(BankAccountDetailsPage, testBankAccountDetails)
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      running(application) {
        val request = FakeRequest(GET, controllers.repayments.routes.BankAccountDetailsController.onPageLoad(NormalMode).url)
        val view    = application.injector.instanceOf[BankAccountDetailsView]
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(formProvider().fill(testBankAccountDetails), NormalMode)(
            request,
            appConfig(application),
            messages(application)
          ).toString
      }
    }

    "must redirect to Repayments Contact Name page when valid data is submitted" in {
      val application = applicationBuilder(userAnswers =
        Some(
          emptyUserAnswers
            .setOrException(BarsAccountNamePartialPage, "James")
            .setOrException(RepaymentAccountNameConfirmationPage, false)
            .setOrException(BankAccountDetailsPage, bankAccountDetails)
        )
      ).overrides(
        bind[BarsService].toInstance(mockBarsService)
      ).build()

      running(application) {

        when(mockBarsService.verifyBusinessAccount(any(), any(), any(), any())(any(), any(), any()))
          .thenReturn(Future successful Redirect(controllers.repayments.routes.RepaymentsContactNameController.onPageLoad(NormalMode)))

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val request =
          FakeRequest(POST, controllers.repayments.routes.BankAccountDetailsController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "bankName"          -> "TestBankName",
              "accountHolderName" -> "TestAccountHolderBankName",
              "sortCode"          -> "123456",
              "accountNumber"     -> "12345678"
            )
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.repayments.routes.RepaymentsContactNameController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.repayments.routes.BankAccountDetailsController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", "invalid value"))
        val boundForm = formProvider().bind(Map("value" -> "invalid value"))
        val view      = application.injector.instanceOf[BankAccountDetailsView]
        val result    = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }
  }
}
