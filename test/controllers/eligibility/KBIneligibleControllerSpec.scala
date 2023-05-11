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

import helpers.ControllerBaseSpec

import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}

class KbMnIneligibleControllerSpec extends ControllerBaseSpec {

  def controller(): KbMnIneligibleController =
    new KbMnIneligibleController(
      stubMessagesControllerComponents(),
      kBMneIneligibleView
    )

  "Trading Business Confirmation Controller" should {
    "must return OK and the correct view for a GET" in {

      val request =
        FakeRequest(GET, controllers.eligibility.routes.KbUKIneligibleController.onPageLoad.url)

      val result = controller.onPageLoad()()(request)
      status(result) shouldBe OK
      contentAsString(result) should include(
        "Pillar 2 top-up tax applies to businesses with activities in more than one country. It’s likely that you’re not covered by this law"
      )

    }

  }
}
