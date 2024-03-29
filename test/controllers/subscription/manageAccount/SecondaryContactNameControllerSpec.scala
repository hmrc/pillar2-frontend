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
import forms.SecondaryContactNameFormProvider
import models.CheckMode
import pages.{SubAddSecondaryContactPage, SubPrimaryContactNamePage, SubSecondaryContactNamePage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.subscriptionview.manageAccount.SecondaryContactNameView

class SecondaryContactNameControllerSpec extends SpecBase {

  val formProvider = new SecondaryContactNameFormProvider()

  "SecondaryContactName Controller for View Contact details" when {

    "must return OK and the correct view for a GET if no previous data is found" in {
      val ua          = emptyUserAnswers.setOrException(SubAddSecondaryContactPage, true).setOrException(SubPrimaryContactNamePage, "asd")
      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.SecondaryContactNameController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondaryContactNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), CheckMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val ua = emptyUserAnswers
        .setOrException(SubSecondaryContactNamePage, "name")
        .setOrException(SubAddSecondaryContactPage, true)
        .setOrException(SubPrimaryContactNamePage, "asd")
      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.SecondaryContactNameController.onPageLoad.url)

        val view = application.injector.instanceOf[SecondaryContactNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill("name"), CheckMode)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.SecondaryContactNameController.onSubmit.url)
            .withFormUrlEncodedBody(("value", "<>"))

        val boundForm = formProvider().bind(Map("value" -> "<>"))

        val view = application.injector.instanceOf[SecondaryContactNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, CheckMode)(request, appConfig(application), messages(application)).toString
      }
    }

  }
}
