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
import forms.RegisteringNfmForThisGroupFormProvider
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.RegisteringNfmForThisGroupView

class RegisteringNfmForThisGroupControllerSpec extends SpecBase {
  val formProvider = new RegisteringNfmForThisGroupFormProvider()

  def controller(): RegisteringNfmForThisGroupController =
    new RegisteringNfmForThisGroupController(
      formProvider,
      stubMessagesControllerComponents(),
      viewRegisteringNfmForThisGroup,
      mockSessionData
    )

  "Registering Nfm ForThis Group ControllerSpec Controller" must {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.eligibility.routes.RegisteringNfmForThisGroupController.onPageLoad.url)
        val view    = application.injector.instanceOf[RegisteringNfmForThisGroupView]
        val result  = route(application, request).value

        contentAsString(result) mustEqual view(formProvider())(request, appConfig(application), messages(application)).toString
        status(result) mustBe OK
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val request =
        FakeRequest(POST, controllers.eligibility.routes.RegisteringNfmForThisGroupController.onSubmit.url)
          .withFormUrlEncodedBody(("registeringNfmGroup", "yes"))
      val result = controller.onSubmit()()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.eligibility.routes.BusinessActivityUKController.onPageLoad.url

    }
    "must redirect to the next page when valid data is submitted with no selected" in {

      val request =
        FakeRequest(POST, controllers.eligibility.routes.RegisteringNfmForThisGroupController.onSubmit.url)
          .withFormUrlEncodedBody(("registeringNfmGroup", "no"))
      val result = controller.onSubmit()()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.eligibility.routes.KbMnIneligibleController.onPageLoad.url

    }
    "must show error page with 400 if no option is selected " in {
      val formData = Map("value" -> "")
      val request =
        FakeRequest(POST, controllers.eligibility.routes.RegisteringNfmForThisGroupController.onSubmit.url).withFormUrlEncodedBody(formData.toSeq: _*)
      val result = controller.onSubmit()()(request)
      status(result) shouldBe 400
    }
  }
}
