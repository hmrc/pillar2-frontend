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
import models.UserAnswers
import models.rfm.RegistrationDate
import pages.{RfmPillar2ReferencePage, RfmRegistrationDatePage}
import play.api.Configuration
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.rfm.{RfmContactDetailsRegistrationView, RfmSaveProgressInformView}

import java.time.LocalDate

class RfmSaveProgressInformControllerSpec extends SpecBase {

  "Rfm Save Progress inform Controller" when {

    "return OK and the correct view for a GET" in {
      val plrReference = "XE1111123456789"
      val date         = LocalDate.of(2024, 12, 31)
      val testConfig   = Configuration("features.rfmAccessEnabled" -> true)
      val userAnswer = UserAnswers(userAnswersId)
        .set(RfmPillar2ReferencePage, plrReference)
        .success
        .value
        .set(RfmRegistrationDatePage, RegistrationDate(date))
        .success
        .value
      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .configure(testConfig)
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmSaveProgressInformController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RfmSaveProgressInformView]

        status(result) mustEqual OK
        contentAsString(result) must include("Replace filing member")
        contentAsString(result) must include("Saving progress")
        contentAsString(result) must include(
          "From this point, the information you enter will be saved as you progress." +
            " If you sign out, the information you have already entered will be saved for 28 days." +
            " After that time you will need to enter all of the information again."
        )
        contentAsString(result) mustEqual view()(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "Send to recovery page if directly hit this page and no answer provided to security related questions" in {
      val date       = LocalDate.of(2024, 12, 31)
      val testConfig = Configuration("features.rfmAccessEnabled" -> true)
      val userAnswer = UserAnswers(userAnswersId)
        .set(RfmRegistrationDatePage, RegistrationDate(date))
        .success
        .value
      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .configure(testConfig)
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmSaveProgressInformController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)

      }
    }

    "must redirect to Under Construction page if RFM access is disabled" in {
      val ua = emptyUserAnswers
      val application = applicationBuilder(userAnswers = Some(ua))
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> false
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmSaveProgressInformController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.UnderConstructionController.onPageLoad.url)
      }
    }
  }
}
