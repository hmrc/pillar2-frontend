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
import helpers.ViewInstances
import org.scalatest.matchers.should.Matchers._
import play.api.test.FakeRequest
import play.api.test.Helpers._

class KbUKIneligibleControllerSpec extends SpecBase with ViewInstances {

  def controller(): KbUKIneligibleController =
    new KbUKIneligibleController(
      stubMessagesControllerComponents(),
      viewKbUKIneligible
    )

  "Trading Business Confirmation Controller" should {
    "return OK and the correct view for a GET" in {

      val request =
        FakeRequest(GET, controllers.eligibility.routes.KbUKIneligibleController.onPageLoad.url)

      val result = controller.onPageLoad()()(request)
      status(result)        shouldBe OK
      contentAsString(result) should include("Pillar 2 Top-up Taxes may be collected when you have an entity located in the UK.")

    }

  }
}
