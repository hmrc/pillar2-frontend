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

package controllers.registration

import base.SpecBase
import models.NormalMode
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.test.FakeRequest
import play.api.test.Helpers._

class StartPageRegistrationControllerSpec extends SpecBase {

  def controller(): StartPageRegistrationController =
    new StartPageRegistrationController(
      preAuthenticatedActionBuilders,
      stubMessagesControllerComponents(),
      viewStartPageRegistration
    )

  "StartPageRegistrationController" when {
    "must return OK and the correct view for a GET" in {

      val request =
        FakeRequest(GET, controllers.registration.routes.StartPageRegistrationController.onPageLoad(NormalMode).url)

      val result = controller.onPageLoad(NormalMode)()(request)
      status(result) shouldBe OK
      contentAsString(result) should include(
        "We need to match the details of the ultimate parent entity to HMRC records"
      )
    }

  }
}
