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
import helpers.ViewInstances
import models.NormalMode
import org.scalatest.matchers.should.Matchers._
import play.api.test.FakeRequest
import play.api.test.Helpers._

class StartPageRegistrationControllerSpec extends SpecBase with ViewInstances {

  def controller(): StartPageRegistrationController =
    new StartPageRegistrationController(
      preAuthenticatedActionBuilders,
      stubMessagesControllerComponents(),
      viewStartPageRegistration
    )

  "StartPageRegistrationController" should {

    "return OK and the correct view for a GET" in {

      val request =
        FakeRequest(GET, controllers.registration.routes.StartPageRegistrationController.onPageLoad(NormalMode).url)

      val result = controller.onPageLoad(NormalMode)()(request)
      status(result) shouldBe OK
      contentAsString(result) should include(
        "We need to match the details of the Ultimate Parent Entity to HMRC records"
      )
      contentAsString(result) should include(
        "If the Ultimate Parent Entity is registered in the UK, we will ask you for identifying information about the ultimate parent so we can match it with our records."
      )
      contentAsString(result) should include(
        "If the Ultimate Parent Entity is registered outside of the UK, we will ask you for identifying information about the ultimate parent so we can create a HMRC record."
      )
    }

    "submit and redirect to " in {
      val request =
        FakeRequest(POST, controllers.registration.routes.StartPageRegistrationController.onSubmit(NormalMode).url)
      val result = controller.onSubmit(NormalMode)()(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).value mustEqual controllers.registration.routes.UPERegisteredInUKConfirmationController.onPageLoad(NormalMode).url
    }

  }
}
