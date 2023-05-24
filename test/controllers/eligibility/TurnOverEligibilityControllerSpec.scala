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
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._

class TurnOverEligibilityControllerSpec extends ControllerBaseSpec {

  def controller(): TurnOverEligibilityController =
    new TurnOverEligibilityController(
      getTurnOverEligibilityProvider,
      stubMessagesControllerComponents(),
      turnOverEligibilityView,
      mockSessionData
    )

  val mockFormYesData = Map("confirmForm" -> "true")
  val mockFormNoData  = Map("confirmForm" -> "false")

  "Turn Over Eligibility Controller" should {
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(controllers.eligibility.routes.TurnOverEligibilityController.onPageLoad)

    "must return OK and the correct view for a GET" in {

      val request =
        FakeRequest(GET, controllers.eligibility.routes.TurnOverEligibilityController.onPageLoad.url)

      val result = controller.onPageLoad()()(request)
      status(result) shouldBe OK
      contentAsString(result) should include(
        "Has your group had revenue of more than 750 million euros in 2 of the last 4 accounting periods?"
      )

    }

    "must redirect to the next page when chosen Yes and submitted" in {

      val request =
        FakeRequest(POST, controllers.eligibility.routes.BusinessActivityUKController.onSubmit.url)
          .withFormUrlEncodedBody(("value", "yes"))
      val result = controller.onSubmit()()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.eligibility.routes.EligibilityConfirmationController.onPageLoad.url

    }

    "must redirect to the next page when chosen No and submitted" in {

      val request =
        FakeRequest(POST, controllers.eligibility.routes.BusinessActivityUKController.onSubmit.url)
          .withFormUrlEncodedBody(("value", "no"))
      val result = controller.onSubmit()()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.eligibility.routes.Kb750IneligibleController.onPageLoad.url

    }

    "must show error page with 400 if no option is selected " in {
      val formData = Map("value" -> "")
      val request =
        FakeRequest(POST, controllers.eligibility.routes.TurnOverEligibilityController.onSubmit.url).withFormUrlEncodedBody(formData.toSeq: _*)
      val result = controller.onSubmit()()(request)
      status(result) shouldBe 400
    }
  }
}
