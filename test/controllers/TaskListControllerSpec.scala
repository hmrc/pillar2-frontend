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

package controllers

import base.SpecBase
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.Pillar2SessionKeys
import views.html.TaskListView

class TaskListControllerSpec extends SpecBase {

  "Task List Controller" must {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.TaskListController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TaskListView]

        status(result) mustEqual OK

        contentAsString(result) should include(
          "Register your group"
        )
        contentAsString(result) should include(
          "Registration incomplete"
        )
        contentAsString(result) should include(
          "Review and submit"
        )
        contentAsString(result) should include(
          "Check your answers"
        )
      }
    }

    "redirected to subscription confirmation page if the user has already subscribed with a pillar 2 reference" in {
      val application = applicationBuilder(None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.routes.TaskListController.onPageLoad.url).withSession(Pillar2SessionKeys.plrId -> "")
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.RegistrationConfirmationController.onPageLoad.url
      }
    }
  }
}
