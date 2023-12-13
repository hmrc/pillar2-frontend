/*
 * Copyright 2023 HM Revenue & Customs
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
import models.subscription.{AccountingPeriod, AmendSubscriptionRequestParameters, DashboardInfo}
import models.{ApiError, MneOrDomestic, NonUKAddress, SubscriptionCreateError, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.inject
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AmendSubscriptionService
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.govuk.SummaryListFluency

import java.time.LocalDate
import scala.concurrent.Future

class GroupDetailCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  "Subscription Check Your Answers Controller" must {

    val startDate = LocalDate.of(2023, 12, 31)
    val endDate   = LocalDate.of(2025, 12, 31)
    val date      = AccountingPeriod(startDate, endDate)

    "must return OK and the correct view if an answer is provided to every question " in {
      val userAnswer = UserAnswers(userAnswersId)
        .set(subMneOrDomesticPage, MneOrDomestic.Uk)
        .success
        .value
        .set(subAccountingPeriodPage, date)
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

    "must return OK and the correct view if an answer is provided to every question when UkAndOther  option is selected  " in {
      val userAnswer = UserAnswers(userAnswersId)
        .set(subMneOrDomesticPage, MneOrDomestic.UkAndOther)
        .success
        .value
        .set(subAccountingPeriodPage, date)
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
