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

package controllers

import base.SpecBase
import forms.TradingBusinessConfirmationFormProvider
import helpers.BaseSpec
import models.{NormalMode, TradingBusinessConfirmation, UserAnswers}

import play.api.data.Form

import play.api.mvc.{AnyContentAsEmpty, Call, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.TradingBusinessConfirmationView

class TradingBusinessConfirmationControllerSpec extends SpecBase {

  "Trading Business Confirmation Controller" - {
    val formProvider = new TradingBusinessConfirmationFormProvider()
    val form: Form[TradingBusinessConfirmation] = formProvider()

    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(routes.TradingBusinessConfirmationController.onPageLoad())

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, routes.TradingBusinessConfirmationController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TradingBusinessConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val userAnswers = UserAnswers(userAnswersId)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {

        val request =
          FakeRequest(POST, routes.TradingBusinessConfirmationController.onPageLoad().url)
            .withFormUrlEncodedBody(("value", "no"))

        val result = route(application, request).value

        val view = application.injector.instanceOf[TradingBusinessConfirmationView]

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad.url
      }
    }
  }
}
