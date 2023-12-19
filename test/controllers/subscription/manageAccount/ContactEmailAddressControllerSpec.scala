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

package controllers.subscription.manageAccount

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.ContactEmailAddressFormProvider
import models.{CheckMode, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{subPrimaryContactNamePage, subPrimaryEmailPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.subscriptionview.manageAccount.ContactEmailAddressView

import scala.concurrent.Future

class ContactEmailAddressControllerSpec extends SpecBase {

  val formProvider = new ContactEmailAddressFormProvider()

  "ContactEmail Address Controller for View Contact details" when {

    "must return OK and the correct view for a GET if page previously  not answered" in {

      val userAnswersSubContactEmail =
        emptyUserAnswers.set(subPrimaryContactNamePage, "name").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersSubContactEmail))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactEmailAddressController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactEmailAddressView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("name"), CheckMode, "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET if page previously has been answered" in {

      val userAnswersSubContactEmail =
        emptyUserAnswers.setOrException(subPrimaryContactNamePage, "name").setOrException(subPrimaryEmailPage, "hello@goodbye.com")

      val application = applicationBuilder(userAnswers = Some(userAnswersSubContactEmail))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactEmailAddressController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactEmailAddressView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("name").fill("hello@goodbye.com"), CheckMode, "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val userAnswersSubContactEmail =
        emptyUserAnswers.set(subPrimaryContactNamePage, "name").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersSubContactEmail))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactEmailAddressController.onSubmit.url)
            .withFormUrlEncodedBody(("value", "AshleySmith@email.com"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.manageAccount.routes.ContactByTelephoneController.onPageLoad.url
      }
    }
    "must return a Bad Request when invalid data is submitted" in {
      val ua          = emptyUserAnswers.set(subPrimaryContactNamePage, "name").success.value
      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactEmailAddressController.onPageLoad.url)
            .withFormUrlEncodedBody(("value", ""))
        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
      }
    }

    "redirect to bookmark page  if no primary contact name is found for GET" in {

      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactEmailAddressController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.BookmarkPreventionController.onPageLoad.url
      }

    }
    "must redirect to journey recovery if no primary contact name is found for POST" in {
      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactEmailAddressController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}