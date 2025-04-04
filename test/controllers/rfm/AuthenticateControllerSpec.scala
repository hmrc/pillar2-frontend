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

package controllers.rfm

import base.SpecBase
import models.NormalMode
import play.api.test.FakeRequest
import play.api.test.Helpers._

class AuthenticateControllerSpec extends SpecBase {

  "Authenticate Controller" when {

    "must redirect to security question page" in {

      val application = applicationBuilder(userAnswers = None)
        .build()
      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.AuthenticateController.rfmAuthenticate.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.rfm.routes.SecurityCheckController.onPageLoad(NormalMode).url)

      }
    }
  }

}
