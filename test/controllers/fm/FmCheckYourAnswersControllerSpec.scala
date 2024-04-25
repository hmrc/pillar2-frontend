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

package controllers.fm

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency

class FmCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  "UPE no ID Check Your Answers Controller" must {

    "redirect to bookmark prevention page if all required pages have not been answered" in {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.InprogressTaskListController.onPageLoad.url
      }

    }
    "return ok with correct view" in {
      val application = applicationBuilder(userAnswers = Some(fmCompletedGrsResponse)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("Check your answers for filing member details")
      }

    }
  }
}
