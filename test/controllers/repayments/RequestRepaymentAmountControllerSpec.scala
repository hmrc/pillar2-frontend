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
import forms.RequestRefundAmountFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.RepaymentsRefundAmountPage
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.repayments.RequestRefundAmountView

import scala.concurrent.Future

class RequestRepaymentAmountControllerSpec extends SpecBase {

  val formProvider = new RequestRefundAmountFormProvider()

  "RequestRefundAmount Controller" when {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.repayments.routes.RequestRepaymentAmountController.onPageLoad(NormalMode).url)
        val view    = application.injector.instanceOf[RequestRefundAmountView]
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val amount = BigDecimal(9.99)
      val ua     = emptyUserAnswers.setOrException(RepaymentsRefundAmountPage, amount)
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      running(application) {
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(ua)))
        val request = FakeRequest(GET, controllers.repayments.routes.RequestRepaymentAmountController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[RequestRefundAmountView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(amount), NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must redirect to reason for requesting when valid data is submitted" in {
      val application = applicationBuilder(None).build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.repayments.routes.RequestRepaymentAmountController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "value" -> "99.9"
            )
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.repayments.routes.ReasonForRequestingRepaymentController
          .onPageLoad(NormalMode)
          .url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.repayments.routes.RequestRepaymentAmountController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", "invalid value"))
        val boundForm = formProvider().bind(Map("value" -> "invalid value"))
        val view      = application.injector.instanceOf[RequestRefundAmountView]
        val result    = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }
    "must return a Bad Request and errors when invalid data is submitted less than 0.0" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.repayments.routes.RequestRepaymentAmountController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", "-9"))
        val boundForm = formProvider().bind(Map("value" -> "-9"))
        val view      = application.injector.instanceOf[RequestRefundAmountView]
        val result    = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted greater than 99,999,999,999.99" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.repayments.routes.RequestRepaymentAmountController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", "99,999,9999,999.99.99"))
        val boundForm = formProvider().bind(Map("value" -> "99,999,9999,999.99.99"))
        val view      = application.injector.instanceOf[RequestRefundAmountView]
        val result    = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }
  }
}
