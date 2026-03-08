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

package controllers.subscription.manageAccount

import base.SpecBase
import models.MneOrDomestic
import models.subscription.AccountingPeriodDisplay
import pages.SubMneOrDomesticPage
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import java.time.LocalDate

class ViewAccountingPeriodControllerSpec extends SpecBase {

  private val periods = Seq(
    AccountingPeriodDisplay(
      startDate = LocalDate.of(2022, 9, 28),
      endDate = LocalDate.of(2023, 9, 27),
      dueDate = LocalDate.of(2023, 10, 27),
      canAmendStartDate = true,
      canAmendEndDate = true
    )
  )

  private val sessionWithPeriods = Map(
    ManageAccountV2SessionKeys.DisplaySubscriptionV2Periods -> Json.toJson(periods).toString
  )

  "ViewAccountingPeriodController" when {

    "onPageLoad" must {

      "redirect to ManageGroupDetailsCheckYourAnswers when session has no periods" in {
        val ua = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
        val sd = emptySubscriptionLocalData.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
        val application = applicationBuilder(Some(ua), subscriptionLocalData = Some(sd)).build()

        running(application) {
          val request = FakeRequest(GET, routes.ViewAccountingPeriodController.onPageLoad(0).url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url
        }
      }

      "return OK and display period dates when index is valid" in {
        val ua = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
        val sd = emptySubscriptionLocalData.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
        val application = applicationBuilder(Some(ua), subscriptionLocalData = Some(sd)).build()

        running(application) {
          val request = FakeRequest(GET, routes.ViewAccountingPeriodController.onPageLoad(0).url)
            .withSession(sessionWithPeriods.toSeq*)
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("28 September 2022")
          contentAsString(result) must include("27 September 2023")
        }
      }

      "redirect to ManageGroupDetailsCheckYourAnswers when index is out of range" in {
        val ua = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
        val sd = emptySubscriptionLocalData.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
        val application = applicationBuilder(Some(ua), subscriptionLocalData = Some(sd)).build()

        running(application) {
          val request = FakeRequest(GET, routes.ViewAccountingPeriodController.onPageLoad(1).url)
            .withSession(sessionWithPeriods.toSeq*)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url
        }
      }
    }
  }
}
