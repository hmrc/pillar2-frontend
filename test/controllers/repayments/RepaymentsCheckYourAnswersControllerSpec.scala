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
import connectors.{TaxEnrolmentConnector, UserAnswersConnectors}
import models.grs.{EntityType, GrsRegistrationResult, RegistrationStatus}
import models.registration._
import models.subscription.AccountingPeriod
import models.{DuplicateSubmissionError, InternalIssueError, MneOrDomestic, UKAddress, UserAnswers}
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SubscriptionService
import utils.RowStatus
import viewmodels.govuk.SummaryListFluency

import java.time.LocalDate
import scala.concurrent.Future

class RepaymentsCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  private val plrReference = "XE1111123456789"

  private val date = LocalDate.now()
  private val ukAddress = UKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = "m19hgs",
    countryCode = "AB"
  )

  val amount = BigDecimal(9.99)
  private val subData = emptyUserAnswers
    .setOrException(RepaymentsRefundAmountPage, amount)
    .setOrException(ReasonForRequestingRefundPage, "The reason for refund")

  " Repayments Check Your Answers Controller" must {
    "on page load method " should {

      "return OK and the correct view if an answer is provided to every contact detail question" in {
        val userAnswer = UserAnswers("id")
        val application = applicationBuilder(userAnswers = Some(subData))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))
        running(application) {
          val request = FakeRequest(GET, controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad().url)
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
      "redirect to confirmation page in case of a success response" in {

        val userAnswer = UserAnswers("id")
          .setOrException(RepaymentsRefundAmountPage, amount)
          .setOrException(ReasonForRequestingRefundPage, "The reason for refund")
        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
        running(application) {
          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest(POST, controllers.repayments.routes.RepaymentsCheckYourAnswersController.onSubmit().url)
          val result  = route(application, request).value
          status(result) mustBe SEE_OTHER
        }
      }

//      "redirect to inProgress error page if no user data is found" in {
//        val application = applicationBuilder(userAnswers = None).build()
//        running(application) {
//          val request = FakeRequest(POST, controllers.routes.CheckYourAnswersController.onSubmit.url)
//          val result  = route(application, request).value
//          status(result) mustBe SEE_OTHER
//          redirectLocation(result) mustBe Some(controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad().url)
//        }
//      }

    }
  }
}
