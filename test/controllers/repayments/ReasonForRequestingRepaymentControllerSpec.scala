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
import forms.ReasonForRequestingRepaymentFormProvider
import models.{NormalMode, UserAnswers}
import navigation.RepaymentNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{verify, when}
import pages.ReasonForRequestingRefundPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.repayments.ReasonForRequestingRefundView

import scala.concurrent.Future

class ReasonForRequestingRepaymentControllerSpec extends SpecBase {

  val formProvider = new ReasonForRequestingRepaymentFormProvider()

  "ReasonForRequestingRefund Controller" when {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.repayments.routes.ReasonForRequestingRepaymentController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReasonForRequestingRefundView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(ReasonForRequestingRefundPage, "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.repayments.routes.ReasonForRequestingRepaymentController.onPageLoad(NormalMode).url)

        val view = application.injector.instanceOf[ReasonForRequestingRefundView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill("answer"), NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.repayments.routes.ReasonForRequestingRepaymentController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ReasonForRequestingRefundView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }
    "must update the user answers and redirect to the next page when the user answers has provided a valid answer" in {

      val expectedNextPage = Call(GET, "/")
      val mockNavigator    = mock[RepaymentNavigator]
      when(mockNavigator.nextPage(any(), any(), any())).thenReturn(expectedNextPage)
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val userAnswers = emptyUserAnswers
        .setOrException(ReasonForRequestingRefundPage, "valid reason")

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[RepaymentNavigator].toInstance(mockNavigator),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.repayments.routes.ReasonForRequestingRepaymentController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", "valid reason"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedNextPage.url
        verify(mockSessionRepository).set(eqTo(userAnswers))
        verify(mockNavigator).nextPage(ReasonForRequestingRefundPage, NormalMode, userAnswers)
      }
    }

    "must display character limit text when long text is entered" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {

        val request =
          FakeRequest(GET, controllers.repayments.routes.ReasonForRequestingRepaymentController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("250 characters")
      }
    }

    "must display pre-populated repayment reason field when previously answered" in {
      val longText =
        "A content designer works on the end-to-end journey of a service to help users complete their goal and government deliver a policy intent. Their work may involve the creation of, or change to, a transaction, product or single piece of content."
      val userAnswers = UserAnswers(userAnswersId).set(ReasonForRequestingRefundPage, longText).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.repayments.routes.ReasonForRequestingRepaymentController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include(longText)
      }
    }

  }
}
