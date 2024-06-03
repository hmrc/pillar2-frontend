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

package controllers.rfm

import base.SpecBase
import models.rfm.RegistrationDate
import models.rfm.RegistrationDate._
import models.{InternalIssueError, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, inject}
import repositories.SessionRepository
import services.SubscriptionService
import viewmodels.govuk.SummaryListFluency

import java.time.LocalDate
import scala.concurrent.Future

class SecurityQuestionsCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  "Security Questions Check Your Answers Controller" must {
    val plrReference = "XE1111123456789"
    "onPageLoad" should {
      "return OK and the correct view if an answer is provided to every question " in {

        val userAnswer = emptyUserAnswers
          .setOrException(RfmPillar2ReferencePage, plrReference)
          .setOrException(RfmRegistrationDatePage, RegistrationDate(registrationDate))

        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .build()
        running(application) {
          val request = FakeRequest(GET, controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onPageLoad(NormalMode).url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Check your answer")
          contentAsString(result) must include("Pillar 2 top-up taxes ID")

        }
      }

      "redirect to Journey Recovery page when security question status is not completed" in {
        val userAnswer = emptyUserAnswers
        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .build()
        running(application) {
          val request = FakeRequest(GET, controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onPageLoad(NormalMode).url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url)
        }
      }

      "redirect to Under Construction page when RFM access is disabled" in {
        val testConfig = Configuration("features.rfmAccessEnabled" -> false)
        val application = applicationBuilder()
          .configure(testConfig)
          .build()
        running(application) {
          val request = FakeRequest(GET, controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onPageLoad(NormalMode).url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.UnderConstructionController.onPageLoad.url)
        }
      }
    }
    "onSubmit" should {
      "redirect to corporate position group page if registration date match our records" in {
        val userAnswer = emptyUserAnswers
          .setOrException(RfmPillar2ReferencePage, plrReference)
          .setOrException(RfmRegistrationDatePage, RegistrationDate(registrationDate))

        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(inject.bind[SubscriptionService].toInstance(mockSubscriptionService))
          .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
          .build()
        running(application) {
          when(mockSessionRepository.get(userAnswer.id)) thenReturn Future.successful(Some(userAnswer))
          when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
          val request = FakeRequest(POST, controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onSubmit.url)
            .withFormUrlEncodedBody()

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rfm.routes.RfmSaveProgressInformController.onPageLoad.url
        }
      }

      "redirect to error page if registration dates do not match" in {
        val userAnswer = emptyUserAnswers
          .setOrException(RfmPillar2ReferencePage, plrReference)
          .setOrException(RfmRegistrationDatePage, RegistrationDate(LocalDate.now()))

        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(inject.bind[SubscriptionService].toInstance(mockSubscriptionService))
          .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
          .build()
        running(application) {
          when(mockSessionRepository.get(userAnswer.id)) thenReturn Future.successful(Some(userAnswer))
          when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
          val request = FakeRequest(POST, controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onSubmit.url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rfm.routes.MismatchedRegistrationDetailsController.onPageLoad.url
        }
      }
      "redirect to error page if read subscription fails" in {
        val userAnswer = emptyUserAnswers
          .setOrException(RfmPillar2ReferencePage, plrReference)
          .setOrException(RfmRegistrationDatePage, RegistrationDate(LocalDate.now()))
        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(inject.bind[SubscriptionService].toInstance(mockSubscriptionService))
          .overrides(inject.bind[SessionRepository].toInstance(mockSessionRepository))
          .build()
        running(application) {
          when(mockSessionRepository.get(userAnswer.id)) thenReturn Future.successful(Some(userAnswer))
          when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.failed(InternalIssueError))
          val request = FakeRequest(POST, controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onSubmit.url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rfm.routes.MismatchedRegistrationDetailsController.onPageLoad.url
        }
      }
      "redirect to journey recovery if no input pillar 2 id is found" in {
        val userAnswer = emptyUserAnswers
          .setOrException(RfmRegistrationDatePage, RegistrationDate(LocalDate.now()))
        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .build()
        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url
        }
      }
      "redirect to journey recovery if no input registration date is found" in {
        val userAnswer = emptyUserAnswers
          .setOrException(RfmPillar2ReferencePage, plrReference)
        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .build()
        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url
        }
      }
    }

  }
}
