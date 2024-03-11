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
import connectors.UserAnswersConnectors
import models.rfm.RegistrationDate
import models.rfm.RegistrationDate._
import models.subscription.DashboardInfo
import models.{InternalIssueError, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, inject}
import services.ReadSubscriptionService
import viewmodels.govuk.SummaryListFluency

import java.time.LocalDate
import scala.concurrent.Future

class SecurityQuestionsCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  private val plrReference  = "XEPLR1123456789"
  private val date          = LocalDate.of(2024, 12, 31)
  private val dashboardInfo = DashboardInfo("org name", LocalDate.of(2024, 12, 31))
  "Security Questions Check Your Answers Controller" when {

    "onPageLoad" must {
      "return OK and the correct view if an answer is provided to every question " in {

        val userAnswer = UserAnswers(userAnswersId)
          .setOrException(RfmSecurityCheckPage, plrReference)
          .setOrException(RfmRegistrationDatePage, RegistrationDate(date))

        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .build()
        running(application) {
          val request = FakeRequest(GET, controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Check your answer")
          contentAsString(result) must include("Pillar 2 top-up taxes ID")

        }
      }

      "redirect to Journey Recovery page when security question status is not completed" in {
        val userAnswer = UserAnswers(userAnswersId)
        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .build()
        running(application) {
          val request = FakeRequest(GET, controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
      }

      "redirect to Under Construction page when RFM access is disabled" in {
        val testConfig = Configuration("features.rfmAccessEnabled" -> false)
        val application = applicationBuilder()
          .configure(testConfig)
          .build()
        running(application) {
          val request = FakeRequest(GET, controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.UnderConstructionController.onPageLoad.url)
        }
      }

    }
    "onSubmit" must {
      "redirect to under construction in case of a successful read subscription and matched reg dates " in {
        val ua = emptyUserAnswers
          .setOrException(RfmSecurityCheckPage, plrReference)
          .setOrException(RfmRegistrationDatePage, RegistrationDate(date))
          .setOrException(fmDashboardPage, dashboardInfo)
        val application = applicationBuilder(Some(ua))
          .overrides(
            inject.bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService),
            inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
        running(application) {
          when(mockReadSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(Json.toJson(dashboardInfo)))
          when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(ua)))
          val request = FakeRequest(POST, controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
        }
      }

      "redirect to error page if input dates do not match" in {
        val ua = emptyUserAnswers
          .setOrException(RfmSecurityCheckPage, plrReference)
          .setOrException(RfmRegistrationDatePage, RegistrationDate(LocalDate.now()))
          .setOrException(fmDashboardPage, dashboardInfo)
        val application = applicationBuilder(Some(ua))
          .overrides(
            inject.bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService),
            inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
        running(application) {
          when(mockReadSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(Json.toJson(dashboardInfo)))
          when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(ua)))
          val request = FakeRequest(POST, controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rfm.routes.SecurityQuestionsNoMatchController.onPageLoad.url
        }
      }
    }

    "redirect to error page if subscription service returns a non-success response" in {
      val ua = emptyUserAnswers
        .setOrException(RfmSecurityCheckPage, plrReference)
        .setOrException(RfmRegistrationDatePage, RegistrationDate(date))
        .setOrException(fmDashboardPage, dashboardInfo)
      val application = applicationBuilder(Some(ua))
        .overrides(
          inject.bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService),
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()
      running(application) {
        when(mockReadSubscriptionService.readSubscription(any())(any())).thenReturn(Future.failed(InternalIssueError))
        when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(ua)))
        val request = FakeRequest(POST, controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onSubmit.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.SecurityQuestionsNoMatchController.onPageLoad.url
      }
    }

    "redirect to journey recovery if no input pillar 2 id is found" in {
      val ua = emptyUserAnswers
        .setOrException(RfmRegistrationDatePage, RegistrationDate(date))
        .setOrException(fmDashboardPage, dashboardInfo)
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(POST, controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onSubmit.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to journey recovery if no input registration date is found" in {
      val ua = emptyUserAnswers
        .setOrException(RfmSecurityCheckPage, plrReference)
        .setOrException(fmDashboardPage, dashboardInfo)
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(POST, controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onSubmit.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
    "redirect to journey recovery if no dashboard info is found" in {
      val ua = emptyUserAnswers
        .setOrException(RfmSecurityCheckPage, plrReference)
        .setOrException(RfmRegistrationDatePage, RegistrationDate(date))
      val application = applicationBuilder(Some(ua))
        .overrides(
          inject.bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService),
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()
      running(application) {
        when(mockReadSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(Json.toJson(dashboardInfo)))
        when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(ua)))
        val request = FakeRequest(POST, controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onSubmit.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
