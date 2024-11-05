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
import forms.NonUKBankFormProvider
import models.NormalMode
import models.repayments.NonUKBank
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.NonUKBankPage
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.repayments.NonUKBankView

import scala.concurrent.Future

class NonUKBankControllerSpec extends SpecBase {

  val formProvider = new NonUKBankFormProvider()

  "NonUKBank Controller" when {

    "must redirect to error page if the feature flag is false" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), additionalData = Map("features.repaymentsAccessEnabled" -> false))
        .build()
      running(application) {
        val request = FakeRequest(GET, controllers.repayments.routes.NonUKBankController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/error/page-not-found")
      }
    }

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.repayments.routes.NonUKBankController.onPageLoad(NormalMode).url)
        val view    = application.injector.instanceOf[NonUKBankView]
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val ua = emptyUserAnswers.setOrException(NonUKBankPage, NonUKBank("BankName", "Name", "HBUKGB4B", "GB29NWBK60161331926819"))
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))
        val request = FakeRequest(GET, controllers.repayments.routes.NonUKBankController.onPageLoad(NormalMode).url)
        val view    = application.injector.instanceOf[NonUKBankView]
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(formProvider().fill(NonUKBank("BankName", "Name", "HBUKGB4B", "GB29NWBK60161331926819")), NormalMode)(
            request,
            appConfig(),
            messages(application)
          ).toString
      }
    }

    "must redirect to Repayments Contact Name page when valid data is submitted" in {
      val application = applicationBuilder(None)
        .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      running(application) {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val request =
          FakeRequest(POST, controllers.repayments.routes.NonUKBankController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "bankName"          -> "BankName",
              "nameOnBankAccount" -> "Name",
              "bic"               -> "HBUKGB4B",
              "iban"              -> "GB29NWBK60161331926819"
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
          FakeRequest(POST, controllers.repayments.routes.NonUKBankController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", "invalid value"))
        val boundForm = formProvider().bind(Map("value" -> "invalid value"))
        val view      = application.injector.instanceOf[NonUKBankView]
        val result    = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(), messages(application)).toString
      }
    }

  }
}
