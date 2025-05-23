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

package controllers.rfm

import base.SpecBase
import models.{NonUKAddress, NormalMode, UserAnswers}
import pages.{RfmNameRegistrationPage, RfmRegisteredAddressPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency

class RfmCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  "NoId flow questions check your answers controller" must {

    val name = "nfm name"
    val nonUkAddress: NonUKAddress = NonUKAddress("addressLine1", None, "addressLine3", None, None, countryCode = "US")
    val userAnswer = UserAnswers(userAnswersId)
      .set(RfmNameRegistrationPage, name)
      .success
      .value
      .set(RfmRegisteredAddressPage, nonUkAddress)
      .success
      .value

    "return OK and the correct view if an answer is provided to every question " in {
      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .build()
      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmCheckYourAnswersController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("Check your answer")
        contentAsString(result) must include("Name")
        contentAsString(result) must include("Address")
      }
    }

    "redirect to restart error page when rfm noId question status is not completed" in {
      val userAnswer = UserAnswers(userAnswersId)
      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .build()
      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmCheckYourAnswersController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url)
      }
    }

    "redirect to rfm contact details registration page on form submission" in {
      val userAnswer = UserAnswers(userAnswersId)
        .set(RfmNameRegistrationPage, name)
        .success
        .value
        .set(RfmRegisteredAddressPage, nonUkAddress)
        .success
        .value
      val application = applicationBuilder(userAnswers = Some(userAnswer)).build()
      running(application) {
        val request = FakeRequest(POST, controllers.rfm.routes.RfmCheckYourAnswersController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody()
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.rfm.routes.RfmContactDetailsRegistrationController.onPageLoad.url)
      }
    }

  }
}
