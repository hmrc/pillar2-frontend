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
import models.{CheckMode, NormalMode}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.test.FakeRequest
import play.api.test.Helpers._

class CheckNewFilingMemberControllerSpec extends SpecBase {

  "CheckNewFilingMemberController" must {

    "return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.CheckNewFilingMemberController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value

        status(result) shouldBe OK

      }
    }

    "redirect to the correct route on onSubmit" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(POST, controllers.rfm.routes.CheckNewFilingMemberController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody("someField" -> "someValue")

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.rfm.routes.UkBasedFilingMemberController.onPageLoad(NormalMode).url)
      }
    }

    "return OK and the correct view for a GET in CheckMode" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {

        val request = FakeRequest(GET, controllers.rfm.routes.CheckNewFilingMemberController.onPageLoad(CheckMode).url)

        val result = route(application, request).value

        status(result) mustBe OK
      }
    }

  }
}
