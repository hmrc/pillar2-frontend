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
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.test.FakeRequest
import play.api.test.Helpers._

class RfmCannotReturnAfterConfirmationControllerSpec extends SpecBase {

  "RfmCannotReturnAfterConfirmationController" must {

    "return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()
      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmCannotReturnAfterConfirmationController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) shouldBe OK
      }
    }

    "redirect to Journey Recovery page when RFM feature is disabled" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure("features.rfmAccessEnabled" -> false)
        .build()
      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmCannotReturnAfterConfirmationController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/error/page-not-found")
      }
    }

  }
}
