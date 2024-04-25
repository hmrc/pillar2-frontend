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

package controllers.subscription

import base.SpecBase
import models.{NonUKAddress, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.Future

class ContactCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {
  val subDataWithAddress: UserAnswers = emptyUserAnswers
    .setOrException(SubPrimaryContactNamePage, "name")
    .setOrException(SubPrimaryEmailPage, "email@hello.com")
    .setOrException(SubPrimaryPhonePreferencePage, true)
    .setOrException(SubPrimaryCapturePhonePage, "123213")
    .setOrException(SubAddSecondaryContactPage, true)
    .setOrException(SubSecondaryContactNamePage, "name")
    .setOrException(SubSecondaryEmailPage, "email@hello.com")
    .setOrException(SubSecondaryPhonePreferencePage, true)
    .setOrException(SubSecondaryCapturePhonePage, "123213")
    .setOrException(SubRegisteredAddressPage, NonUKAddress("this", None, "over", None, None, countryCode = "AR"))

  val subDataWithoutAddress: UserAnswers = emptyUserAnswers
    .setOrException(SubPrimaryContactNamePage, "name")
    .setOrException(SubPrimaryEmailPage, "email@hello.com")
    .setOrException(SubPrimaryPhonePreferencePage, true)
    .setOrException(SubPrimaryCapturePhonePage, "123213")
    .setOrException(SubSecondaryContactNamePage, "name")
    .setOrException(SubSecondaryEmailPage, "email@hello.com")
    .setOrException(SubSecondaryPhonePreferencePage, true)
    .setOrException(SubSecondaryCapturePhonePage, "123213")

  "Contact Check Your Answers Controller" must {

    "must return OK and the correct view if an answer is provided to every question " in {
      val application = applicationBuilder(userAnswers = Some(subDataWithAddress)).build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.subscription.routes.ContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include(
          "Contact details"
        )
        contentAsString(result) must include(
          "Second contact"
        )
        contentAsString(result) must include(
          "Contact address"
        )
      }
    }

    "redirect to bookmark page if address page not answered" in {
      val application = applicationBuilder(userAnswers = Some(subDataWithoutAddress)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.ContactCheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.subscription.routes.InprogressTaskListController.onPageLoad.url)
      }
    }

  }
}
