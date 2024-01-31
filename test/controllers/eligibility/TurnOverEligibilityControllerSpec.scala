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
import forms.TurnOverEligibilityFormProvider
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.TurnOverEligibilityView

class TurnOverEligibilityControllerSpec extends SpecBase {

  val formProvider = new TurnOverEligibilityFormProvider()

  "Turn Over Eligibility Controller" when {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder().build()
      running(application) {
        val request =
          FakeRequest(GET, controllers.eligibility.routes.TurnOverEligibilityController.onPageLoad.url)
        val view   = application.injector.instanceOf[TurnOverEligibilityView]
        val result = route(application, request).value
        status(result) shouldBe OK
        contentAsString(result) mustEqual view(formProvider())(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }

    }

    "must redirect to the next page when chosen Yes and submitted" in {
      val application = applicationBuilder().build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.eligibility.routes.TurnOverEligibilityController.onSubmit.url)
            .withFormUrlEncodedBody(("value", "true"))
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.eligibility.routes.EligibilityConfirmationController.onPageLoad.url
      }
    }

    "must redirect to the next page when chosen No and submitted" in {
      val application = applicationBuilder().build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.eligibility.routes.TurnOverEligibilityController.onSubmit.url)
            .withFormUrlEncodedBody(("value", "false"))
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.eligibility.routes.Kb750IneligibleController.onPageLoad.url
      }
    }

    "return bad request if invalid data is submitted" in {
      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(POST, controllers.eligibility.routes.TurnOverEligibilityController.onSubmit.url).withFormUrlEncodedBody(
          "value" -> ""
        )
        val result = route(application, request).value
        status(result) shouldBe BAD_REQUEST
      }
    }
  }
}
