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
import pages._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency

class RfmContactCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  "Check Your Answers Controller" when {

    "must redirect to correct view when rfm feature false" in {
      val ua = emptyUserAnswers
        .setOrException(RfmPrimaryContactNamePage, "sad")
      val application = applicationBuilder(userAnswers = Some(rfmID))
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> false
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }
    }

    "return to recovery page if any part is missing for check answer page" in {

      val application = applicationBuilder(userAnswers = Some(rfmCorpPosition))
        .build()
      running(application) {

        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "return OK and the correct view if an answer is provided to every New RFM ID journey questions - Upe" in {

      val application = applicationBuilder(userAnswers = Some(rfmUpe))
        .build()
      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK

        contentAsString(result) must include("Filing member details")
        contentAsString(result) must include("Ultimate parent entity (UPE)")
        contentAsString(result) must include("First contact")
        contentAsString(result) must include("Contact name")
        contentAsString(result) must include("Email address")
        contentAsString(result) must include("Telephone contact")
        contentAsString(result) must include("Telephone number")
        contentAsString(result) must include("Second contact")
        contentAsString(result) must include("Second contact name")
        contentAsString(result) must include("Second contact email address")
        contentAsString(result) must include("Can we contact by telephone?")
        contentAsString(result) must include("Second contact telephone number")
        contentAsString(result) must include("Contact address")
        contentAsString(result) must include("Address")
        contentAsString(result) must include("the first contact name")
        contentAsString(result) must include("the first contact email address")
        contentAsString(result) must include("can we contact the first contact by telephone")
        contentAsString(result) must include("the telephone number for the first contact")
        contentAsString(result) must include("the telephone number for the first contact")
        contentAsString(result) must include("do you have a second contact")
        contentAsString(result) must include("the second contact name")
        contentAsString(result) must include("the second contact email address")
        contentAsString(result) must include("can we contact the second contact by telephone")
        contentAsString(result) must include("the telephone number for the second contact")
        contentAsString(result) must include("Now submit your details to replace the current filing member")
        contentAsString(result) must include(
          "By sending these details, you are confirming that the information is correct and complete to the best of your knowledge."
        )

      }
    }

    "return OK and the correct view if an answer is provided to every New RFM ID journey questions - NoId" in {

      val application = applicationBuilder(userAnswers = Some(rfmNoID))
        .build()
      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK

        contentAsString(result) must include("Filing member details")
        contentAsString(result) must include("New nominated filing member")
        contentAsString(result) must include("Name")
        contentAsString(result) must include("Address")
        contentAsString(result) must include("First contact")
        contentAsString(result) must include("Contact name")
        contentAsString(result) must include("Email address")
        contentAsString(result) must include("Telephone contact")
        contentAsString(result) must include("Telephone number")
        contentAsString(result) must include("Second contact")
        contentAsString(result) must include("Second contact name")
        contentAsString(result) must include("Second contact email address")
        contentAsString(result) must include("Can we contact by telephone?")
        contentAsString(result) must include("Second contact telephone number")
        contentAsString(result) must include("Contact address")
        contentAsString(result) must include("Address")
        contentAsString(result) must include("the first contact name")
        contentAsString(result) must include("the first contact email address")
        contentAsString(result) must include("can we contact the first contact by telephone")
        contentAsString(result) must include("the telephone number for the first contact")
        contentAsString(result) must include("the telephone number for the first contact")
        contentAsString(result) must include("do you have a second contact")
        contentAsString(result) must include("the second contact name")
        contentAsString(result) must include("the second contact email address")
        contentAsString(result) must include("can we contact the second contact by telephone")
        contentAsString(result) must include("the telephone number for the second contact")
        contentAsString(result) must include("Now submit your details to replace the current filing member")
        contentAsString(result) must include(
          "By sending these details, you are confirming that the information is correct and complete to the best of your knowledge."
        )

      }
    }

    "return OK and the correct view if an answer is provided to every New RFM ID journey questions" in {

      val application = applicationBuilder(userAnswers = Some(rfmID))
        .build()
      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK

        contentAsString(result) must include("Filing member details")
        contentAsString(result) must include("New nominated filing member")
        contentAsString(result) must include(
          "Company"
        )
        contentAsString(result) must include("ABC Limited")
        contentAsString(result) must include(
          "Company Registration Number"
        )
        contentAsString(result) must include("1234")
        contentAsString(result) must include(
          "Unique Taxpayer Reference"
        )
        contentAsString(result) must include("First contact")
        contentAsString(result) must include("Contact name")
        contentAsString(result) must include("Email address")
        contentAsString(result) must include("Telephone contact")
        contentAsString(result) must include("Telephone number")
        contentAsString(result) must include("Second contact")
        contentAsString(result) must include("Second contact name")
        contentAsString(result) must include("Second contact email address")
        contentAsString(result) must include("Can we contact by telephone?")
        contentAsString(result) must include("Second contact telephone number")
        contentAsString(result) must include("Contact address")
        contentAsString(result) must include("Address")
        contentAsString(result) must include("the first contact name")
        contentAsString(result) must include("the first contact email address")
        contentAsString(result) must include("can we contact the first contact by telephone")
        contentAsString(result) must include("the telephone number for the first contact")
        contentAsString(result) must include("the telephone number for the first contact")
        contentAsString(result) must include("do you have a second contact")
        contentAsString(result) must include("the second contact name")
        contentAsString(result) must include("the second contact email address")
        contentAsString(result) must include("can we contact the second contact by telephone")
        contentAsString(result) must include("the telephone number for the second contact")
        contentAsString(result) must include("Now submit your details to replace the current filing member")
        contentAsString(result) must include(
          "By sending these details, you are confirming that the information is correct and complete to the best of your knowledge."
        )

      }
    }

    "return OK and the correct view if an answer is provided to every New RFM No ID journey questions" in {

      val application = applicationBuilder(userAnswers = Some(rfmNoID))
        .build()
      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK

        contentAsString(result) must include("Filing member details")
        contentAsString(result) must include("New nominated filing member")
        contentAsString(result) must include("name")
        contentAsString(result) must include("First contact")
        contentAsString(result) must include("Contact name")
        contentAsString(result) must include("Email address")
        contentAsString(result) must include("Telephone contact")
        contentAsString(result) must include("Telephone number")
        contentAsString(result) must include("Second contact")
        contentAsString(result) must include("Second contact name")
        contentAsString(result) must include("Second contact email address")
        contentAsString(result) must include("Can we contact by telephone?")
        contentAsString(result) must include("Second contact telephone number")
        contentAsString(result) must include("Contact address")
        contentAsString(result) must include("Address")
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val userAnswers = UserAnswers(userAnswersId)
        .set(RfmPrimaryContactNamePage, "name")
        .success
        .value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }
    }

  }
}
