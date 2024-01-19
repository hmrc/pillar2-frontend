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
import config.FrontendAppConfig
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.errors.AlreadyRegisteredView

class AlreadyRegisteredControllerSpec extends SpecBase {

  "AlreadyRegistered Controller" when {

    "must return OK and the continue view" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.AlreadyRegisteredController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual OK

        val content = contentAsString(result)
        content must include(messages(application)("alreadyRegistered.heading"))
        content must include(messages(application)("alreadyRegistered.message1"))
        content must include(appConfig(application).contactEmail)
        content must include(messages(application)("alreadyRegistered.newtab"))
      }
    }

    "must include the correct email address in the response" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.AlreadyRegisteredController.onPageLoad.url)

        val result = route(application, request).value

        contentAsString(result) must include(appConfig(application).contactEmail)
      }
    }

    "must include specific messages or links" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.AlreadyRegisteredController.onPageLoad.url)
        val result  = route(application, request).value

        val content = contentAsString(result)
        content must include(messages(application)("alreadyRegistered.message1"))
        content must include(messages(application)("alreadyRegistered.newtab"))
      }
    }
  }
}
