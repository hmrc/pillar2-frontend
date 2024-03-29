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

package controllers.subscription.manageAccount

import base.SpecBase
import forms.MneOrDomesticFormProvider
import models.{CheckMode, MneOrDomestic, NormalMode, UserAnswers}
import pages.SubMneOrDomesticPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.subscriptionview.manageAccount.MneOrDomesticView

class MneOrDomesticControllerSpec extends SpecBase {

  val formProvider = new MneOrDomesticFormProvider()

  "MneOrDomestic Controller" when {

    "must return OK and the correct view for a GET when previous data is found" in {
      val userAnswer = UserAnswers(userAnswersId)
        .set(SubMneOrDomesticPage, MneOrDomestic.Uk)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswer)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.MneOrDomesticController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MneOrDomesticView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(MneOrDomestic.Uk), CheckMode)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET when no previous data is found" in {

      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.MneOrDomesticController.onPageLoad.url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[MneOrDomesticView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.MneOrDomesticController.onPageLoad.url)
            .withFormUrlEncodedBody(("value", "invalid value"))
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

  }
}
