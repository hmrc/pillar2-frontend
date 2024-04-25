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
import connectors.UserAnswersConnectors
import forms.ContactEmailAddressFormProvider
import models.NormalMode
import pages.{SubPrimaryContactNamePage, SubPrimaryEmailPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.subscriptionview.ContactEmailAddressView

class ContactEmailAddressControllerSpec extends SpecBase {

  val formProvider = new ContactEmailAddressFormProvider()

  "ContactEmail Address Controller" when {

    "must return OK and the correct view for a GET if page previously  not answered" in {

      val userAnswersSubContactEmail =
        emptyUserAnswers.set(SubPrimaryContactNamePage, "name").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersSubContactEmail))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.ContactEmailAddressController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactEmailAddressView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("name"), NormalMode, "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET if page previously has been answered" in {

      val userAnswersSubContactEmail =
        emptyUserAnswers.setOrException(SubPrimaryContactNamePage, "name").setOrException(SubPrimaryEmailPage, "hello@goodbye.com")

      val application = applicationBuilder(userAnswers = Some(userAnswersSubContactEmail))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.ContactEmailAddressController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactEmailAddressView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("name").fill("hello@goodbye.com"), NormalMode, "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request when invalid data is submitted" in {
      val ua          = emptyUserAnswers.set(SubPrimaryContactNamePage, "name").success.value
      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.ContactEmailAddressController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("emailAddress", ""))
        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
      }
    }

    "redirect to bookmark page  if no primary contact name is found for GET" in {

      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.ContactEmailAddressController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.InprogressTaskListController.onPageLoad.url
      }

    }
    "must redirect to journey recovery if no primary contact name is found for POST" in {
      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(POST, controllers.subscription.routes.ContactEmailAddressController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.InprogressTaskListController.onPageLoad.url
      }
    }
  }
}
