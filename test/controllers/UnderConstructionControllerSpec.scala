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

package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.{UnderConstruction, UnderConstructionAgent, UnderConstructionError}

class UnderConstructionControllerSpec extends SpecBase {

  "Under construction  Controller" when {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.UnderConstructionController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnderConstruction]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, applicationConfig, messages(application)).toString
      }
    }

    "must return OK and the correct view PageLoad Error for a GET" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.UnderConstructionController.onPageLoadError.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnderConstructionError]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, applicationConfig, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET for agent in repayments" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.UnderConstructionController.onPageLoadAgent.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnderConstructionAgent]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, applicationConfig, messages(application)).toString
      }
    }
  }
}
