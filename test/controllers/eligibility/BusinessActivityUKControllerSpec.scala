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
import forms.BusinessActivityUKFormProvider
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.test.FakeRequest
import play.api.test.Helpers._

class BusinessActivityUKControllerSpec extends SpecBase {
  val formProvider = new BusinessActivityUKFormProvider()

  def controller(): BusinessActivityUKController =
    new BusinessActivityUKController(
      formProvider,
      stubMessagesControllerComponents(),
      viewBusinessActivityUK,
      mockSessionData
    )

  "Trading Business Confirmation Controller" must {
    implicit val request = FakeRequest(controllers.eligibility.routes.BusinessActivityUKController.onPageLoad)

    "must return OK and the correct view for a GET" in {

      val request =
        FakeRequest(GET, controllers.eligibility.routes.BusinessActivityUKController.onPageLoad.url).withFormUrlEncodedBody(("value", "no"))

      val result = controller.onPageLoad()()(request)
      status(result) shouldBe OK
      contentAsString(result) should include(
        "Does the group have business operations in the UK?"
      )
    }

    "must redirect to the next page when valid data is submitted" in {

      val request =
        FakeRequest(POST, controllers.eligibility.routes.BusinessActivityUKController.onSubmit.url)
          .withFormUrlEncodedBody(("value", "yes"))
      val result = controller.onSubmit()()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.eligibility.routes.TurnOverEligibilityController.onPageLoad.url

    }
    "must redirect to the next page when valid data is submitted with no selected" in {

      val request =
        FakeRequest(POST, controllers.eligibility.routes.BusinessActivityUKController.onSubmit.url)
          .withFormUrlEncodedBody(("value", "no"))
      val result = controller.onSubmit()()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.eligibility.routes.KbUKIneligibleController.onPageLoad.url

    }
    "must show error page with 400 if no option is selected " in {
      val formData = Map("value" -> "")
      val request =
        FakeRequest(POST, controllers.eligibility.routes.BusinessActivityUKController.onSubmit.url).withFormUrlEncodedBody(formData.toSeq: _*)
      val result = controller.onSubmit()()(request)
      status(result) shouldBe 400
    }
  }
}
