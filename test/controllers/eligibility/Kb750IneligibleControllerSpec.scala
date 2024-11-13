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
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.test.FakeRequest
import play.api.test.Helpers._

class Kb750IneligibleControllerSpec extends SpecBase with ViewInstances {

  lazy val controller: Kb750IneligibleController =
    new Kb750IneligibleController(
      stubMessagesControllerComponents(),
      viewKb750Ineligible
    )

  "Trading Business Confirmation Controller" when {
    "must return OK and the correct view for a GET" in {

      val request =
        FakeRequest(GET, controllers.eligibility.routes.Kb750IneligibleController.onPageLoad.url)

      val result = controller.onPageLoad()()(request)
      status(result) shouldBe OK
      contentAsString(result) should include(
        "Pillar 2 top-up taxes apply to groups that have consolidated global revenues of €750 million or more in at least 2 of the previous 4 accounting periods."
      )
    }

  }
}
