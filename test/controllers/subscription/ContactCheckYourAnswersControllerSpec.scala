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

package controllers.subscription

import base.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.Future

class ContactCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {
  val subData = emptyUserAnswers
    .set(subPrimaryContactNamePage, "name")
    .success
    .value
    .set(subPrimaryEmailPage, "email@hello.com")
    .success
    .value
    .set(subPrimaryPhonePreferencePage, true)
    .success
    .value
    .set(subPrimaryCapturePhonePage, "123213")
    .success
    .value
    .set(subSecondaryContactNamePage, "name")
    .success
    .value
    .set(subSecondaryEmailPage, "email@hello.com")
    .success
    .value
    .set(subSecondaryPhonePreferencePage, true)
    .success
    .value
    .set(subSecondaryCapturePhonePage, "123213")
    .success
    .value

  "Contact Check Your Answers Controller" must {

    "must return OK and the correct view if an answer is provided to every question " in {
      val application = applicationBuilder(userAnswers = Some(subData)).build()

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

  }
}
