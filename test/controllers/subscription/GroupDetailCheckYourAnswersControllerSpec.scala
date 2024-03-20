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

package controllers.subscription

import base.SpecBase
import models.subscription.AccountingPeriod
import models.{MneOrDomestic, UserAnswers}
import pages._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency
import java.time.LocalDate

class GroupDetailCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  "Subscription Check Your Answers Controller" must {

    val startDate = LocalDate.of(2023, 12, 31)
    val endDate   = LocalDate.of(2025, 12, 31)
    val date      = AccountingPeriod(startDate, endDate)

    "return OK and the correct view if an answer is provided to every question " in {
      val userAnswer = UserAnswers(userAnswersId)
        .set(SubMneOrDomesticPage, MneOrDomestic.Uk)
        .success
        .value
        .set(SubAccountingPeriodPage, date)
        .success
        .value
      val application = applicationBuilder(userAnswers = Some(userAnswer)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.GroupDetailCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include("Check your answer")
        contentAsString(result) must include("Where does the group operate?")
      }
    }

    "return OK and the correct view if an answer is provided to every question when UkAndOther  option is selected  " in {
      val userAnswer = UserAnswers(userAnswersId)
        .set(SubMneOrDomesticPage, MneOrDomestic.UkAndOther)
        .success
        .value
        .set(SubAccountingPeriodPage, date)
        .success
        .value
      val application = applicationBuilder(userAnswers = Some(userAnswer)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.GroupDetailCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include("Check your answer")
        contentAsString(result) must include("Where does the group operate?")
      }
    }
  }
}
