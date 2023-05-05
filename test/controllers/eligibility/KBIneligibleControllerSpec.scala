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

package controllers.eligibility

import base.SpecBase
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.test.FakeRequest
import play.api.test.Helpers._

class KBIneligibleControllerSpec extends SpecBase {

  "Kb Ineligible Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.eligibility.routes.KBIneligibleController.onPageLoad.url)

        val result = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) should include("Based on your answers, you’re not covered by Pillar 2 top-up tax")
        contentAsString(result) should include(
          "Pillar 2 top-up tax applies to businesses with activities in more" +
            " than one country. It’s likely that you’re not covered by this law."
        )
      }
    }
  }
}
