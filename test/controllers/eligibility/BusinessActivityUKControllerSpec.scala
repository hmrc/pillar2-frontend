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

import controllers.routes
import helpers.ControllerBaseSpec
import models.NormalMode
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.{any, contains}
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}

import scala.concurrent.Future

class BusinessActivityUKControllerSpec extends ControllerBaseSpec {

  def controller(): BusinessActivityUKController =
    new BusinessActivityUKController(
      getBusinessActivityUKFormProvider,
      stubMessagesControllerComponents(),
      businessActivityUKView,
      mockSessionData
    )

  val mockFormYesData = Map("confirmForm" -> "yes")
  val mockFormNoData  = Map("confirmForm" -> "no")

  "Trading Business Confirmation Controller" should {
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(controllers.eligibility.routes.BusinessActivityUKController.onPageLoad)

    "must return OK and the correct view for a GET" in {

      val request =
        FakeRequest(GET, controllers.eligibility.routes.BusinessActivityUKController.onPageLoad.url).withFormUrlEncodedBody(("value", "no"))

      val result = controller.onPageLoad()()(request)
      status(result) shouldBe OK
      contentAsString(result) should include(
        "Does any entity in this group have business activity in the UK?"
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
