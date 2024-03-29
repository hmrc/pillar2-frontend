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
        content must include(messages(application)("alreadyRegistered.message2"))
      }
    }

    "must include specific messages or links" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.AlreadyRegisteredController.onPageLoad.url)
        val result  = route(application, request).value

        val content = contentAsString(result)
        content must include(messages(application)("alreadyRegistered.message1"))
        content must include(messages(application)("alreadyRegistered.message2"))
      }
    }
  }
}
