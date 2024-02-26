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
import models.UserAnswers
import models.rfm.RegistrationDate
import models.rfm.RegistrationDate._
import pages._
import play.api.Configuration
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency

import java.time.LocalDate

class SecurityQuestionsCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  "Security Questions Check Your Answers Controller" must {

    val plrReference = "XE1111123456789"
    val date         = LocalDate.of(2024, 12, 31)
    "return OK and the correct view if an answer is provided to every question " in {

      val testConfig = Configuration("features.rfmAccessEnabled" -> true)
      val userAnswer = UserAnswers(userAnswersId)
        .set(rfmSecurityCheckPage, plrReference)
        .success
        .value
        .set(rfmRegistrationDatePage, RegistrationDate(date))
        .success
        .value
      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .configure(testConfig)
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
      val testConfig = Configuration("features.rfmAccessEnabled" -> true)
      val userAnswer = UserAnswers(userAnswersId)
      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .configure(testConfig)
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
}
