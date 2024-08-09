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
import connectors.UserAnswersConnectors
import models.repayments.RepaymentsStatus.{IncompleteDataError, SuccessfullyCompleted, UnexpectedResponseError}
import models.repayments.SendRepaymentDetails
import models.{UnexpectedResponse, UserAnswers}
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{verify, when}
import pages._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.RepaymentService
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.Future

class RepaymentsCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  val amount: BigDecimal = BigDecimal(9.99)
  private val subData = emptyUserAnswers
    .setOrException(RepaymentsRefundAmountPage, amount)
    .setOrException(ReasonForRequestingRefundPage, "The reason for refund")

  "Repayments Check Your Answers Controller" must {

    "on page load method " should {

      "redirect to the error return page when the repayments completion status flag is set to true" in {
        val userAnswer                = UserAnswers("id")
        val postCompletionUserAnswers = emptyUserAnswers.setOrException(RepaymentCompletionStatus, true)
        val application = applicationBuilder(userAnswers = Some(postCompletionUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))
        running(application) {
          val request = FakeRequest(GET, controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).get mustEqual controllers.repayments.routes.RepaymentErrorReturnController.onPageLoad().url
        }
      }

      "return OK and the correct view if an answer is provided to every contact detail question" in {
        val userAnswer = UserAnswers("id")
        val application = applicationBuilder(userAnswers = Some(subData))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))

        running(application) {
          val request = FakeRequest(GET, controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include(
            "Check your answers before submitting your refund request"
          )
          contentAsString(result) must include(
            "Request details"
          )
          contentAsString(result) must include(
            "Bank account details"
          )
          contentAsString(result) must include(
            "Contact details"
          )
        }
      }
    }

    "on submit method" should {

      "redirect to the confirmation page in case of a success response" in {
        val userAnswer = completeRepaymentDataUkBankAccount.setOrException(RepaymentsStatusPage, SuccessfullyCompleted)
        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[RepaymentService].toInstance(mockRepaymentService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
        when(mockRepaymentService.getRepaymentData(any())).thenReturn(Some(validRepaymentPayloadUkBank))
        when(mockRepaymentService.sendRepaymentDetails(any[SendRepaymentDetails])(any())).thenReturn(Future.successful(Done))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        running(application) {
          val request = FakeRequest(POST, controllers.repayments.routes.RepaymentsCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustEqual controllers.repayments.routes.RepaymentsWaitingRoomController.onPageLoad().url
          verify(mockSessionRepository).set(eqTo(userAnswer))
        }
      }

      "redirect to payment failed error page in case of an unsuccessful response" in {
        val userAnswer = completeRepaymentDataUkBankAccount.setOrException(RepaymentsStatusPage, UnexpectedResponseError)
        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[RepaymentService].toInstance(mockRepaymentService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
        when(mockRepaymentService.getRepaymentData(any())).thenReturn(Some(validRepaymentPayloadUkBank))
        when(mockRepaymentService.sendRepaymentDetails(any[SendRepaymentDetails])(any())).thenReturn(Future.failed(UnexpectedResponse))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        running(application) {
          val request = FakeRequest(POST, controllers.repayments.routes.RepaymentsCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustEqual controllers.repayments.routes.RepaymentsWaitingRoomController.onPageLoad().url
          verify(mockSessionRepository).set(eqTo(userAnswer))
        }
      }

      "redirect to Repayments Incomplete Data page if data is partially completed" in {
        val userAnswer = completeRepaymentDataUkBankAccount.setOrException(RepaymentsStatusPage, IncompleteDataError)
        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[RepaymentService].toInstance(mockRepaymentService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
        when(mockRepaymentService.getRepaymentData(any())).thenReturn(None)

        running(application) {
          val request = FakeRequest(POST, controllers.repayments.routes.RepaymentsCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustEqual controllers.repayments.routes.RepaymentsWaitingRoomController.onPageLoad().url
          verify(mockSessionRepository).set(eqTo(userAnswer))
        }
      }

    }

  }
}
