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
import forms.{RegisteringNFMGroupFormProvider, RegisteringTheUPGroupFormProvider}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._

class RegisteringNFMGroupControllerSpec extends SpecBase {

  val formProvider = new RegisteringNFMGroupFormProvider()

  def controller(): RegisteringNFMGroupController =
    new RegisteringNFMGroupController(
      formProvider,
      stubMessagesControllerComponents(),
      viewRegisteringNFMGroup,
      mockSessionData
    )

  val mockFormYesData = Map("confirmForm" -> "yes")
  val mockFormNoData  = Map("confirmForm" -> "no")

  "Registering NFM Group Controller" when {
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(controllers.eligibility.routes.RegisteringTheUPGroupController.onPageLoad)

    "must return OK and the correct view for a GET" in {

      val request =
        FakeRequest(GET, controllers.eligibility.routes.RegisteringNFMGroupController.onPageLoad.url).withFormUrlEncodedBody(("value", "no"))

      val result = controller.onPageLoad()()(request)
      status(result) shouldBe OK
      contentAsString(result) should include(
        "Are you registering the nominated filing member for this group?"
      )
    }

    "must redirect to the next page when valid data is submitted with yes value" in {

      val request =
        FakeRequest(POST, controllers.eligibility.routes.RegisteringTheUPGroupController.onSubmit.url)
          .withFormUrlEncodedBody(("registeringNfmGroup", "yes"))
      val result = controller.onSubmit()()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.eligibility.routes.BusinessActivityUKController.onPageLoad.url

    }

    "must redirect to the next page when valid data is submitted with no value" in {

      val request =
        FakeRequest(POST, controllers.eligibility.routes.RegisteringTheUPGroupController.onSubmit.url)
          .withFormUrlEncodedBody(("registeringNfmGroup", "no"))
      val result = controller.onSubmit()()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.eligibility.routes.KbMnIneligibleController.onPageLoad.url

    }
  }
}
