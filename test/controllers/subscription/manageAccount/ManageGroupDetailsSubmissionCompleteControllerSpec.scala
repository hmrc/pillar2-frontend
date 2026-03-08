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
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class ManageGroupDetailsSubmissionCompleteControllerSpec extends SpecBase {

  private val previousJson = Json.obj("startDate" -> "2021-09-28", "endDate" -> "2022-09-27").toString
  private val newJson      = Json.obj("startDate" -> "2021-09-20", "endDate" -> "2022-10-03").toString

  private val sessionWithPeriods = Map(
    ManageAccountV2SessionKeys.PreviousAccountingPeriodForSuccess -> previousJson,
    ManageAccountV2SessionKeys.NewAccountingPeriodForSuccess      -> newJson
  )

  "ManageGroupDetailsSubmissionCompleteController" when {

    "onPageLoad" must {

      "return OK and display success view and clear session keys when session has period data" in {
        val application = applicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, routes.ManageGroupDetailsSubmissionCompleteController.onPageLoad().url)
            .withSession(sessionWithPeriods.toSeq*)
          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Accounting period change successful")
          contentAsString(result) must include("20 September 2021")
          contentAsString(result) must include("3 October 2022")
          contentAsString(result) must include("28 September 2021")
          contentAsString(result) must include("27 September 2022")
          session(result).get(ManageAccountV2SessionKeys.PreviousAccountingPeriodForSuccess) mustBe None
          session(result).get(ManageAccountV2SessionKeys.NewAccountingPeriodForSuccess) mustBe None
          session(result).get(ManageAccountV2SessionKeys.IsAgentForSuccess) mustBe None
        }
      }

      "redirect to homepage when session has no period keys" in {
        val application = applicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, routes.ManageGroupDetailsSubmissionCompleteController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.HomepageController.onPageLoad().url
        }
      }
    }
  }
}
