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

package controllers.eligibility

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.eligibilityview.EligibilityConfirmationView

class EligibilityConfirmationControllerSpec extends SpecBase {

  "Eligibility Confirmation Controller" when {
    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.eligibility.routes.EligibilityConfirmationController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EligibilityConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, applicationConfig, messages(application)).toString

      }
    }
    "must redirect to the taskList" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, controllers.eligibility.routes.EligibilityConfirmationController.onSubmit.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url
      }
    }
  }
}
