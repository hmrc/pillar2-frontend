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
import connectors.UserAnswersConnectors
import forms.AddSecondaryContactFormProvider
import models.{CheckMode, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{subAddSecondaryContactPage, subPrimaryContactNamePage, subPrimaryEmailPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.subscriptionview.manageAccount.AddSecondaryContactView

import scala.concurrent.Future

class AddSecondaryContactControllerSpec extends SpecBase {

  val formProvider = new AddSecondaryContactFormProvider()

  "AddSecondaryContact Controller for View Contact details" when {

    "must return OK and the correct view for a GET" in {
      val userAnswers = UserAnswers(userAnswersId)
        .setOrException(subPrimaryContactNamePage, "name")
        .setOrException(subPrimaryEmailPage, "asda")

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.AddSecondaryContactController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddSecondaryContactView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), "name", CheckMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .setOrException(subPrimaryContactNamePage, "name")
        .setOrException(subPrimaryEmailPage, "asda")
        .setOrException(subAddSecondaryContactPage, true)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.AddSecondaryContactController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddSecondaryContactView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(true), "name", CheckMode)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val userAnswers = UserAnswers(userAnswersId)
        .set(subPrimaryContactNamePage, "name")
        .success
        .value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.AddSecondaryContactController.onPageLoad.url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AddSecondaryContactView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, "name", NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must redirect to secondary contact name if they answer yes " in {
      val userAnswers = UserAnswers(userAnswersId)
        .set(subPrimaryContactNamePage, "name")
        .success
        .value
        .set(subAddSecondaryContactPage, true)
        .success
        .value

      val application = applicationBuilder(Some(userAnswers))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.AddSecondaryContactController.onSubmit.url)
        .withFormUrlEncodedBody("value" -> "true")

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.manageAccount.routes.SecondaryContactNameController.onPageLoad.url

      }
    }
    "must redirect to the page where we capture their address if they answer no " in {
      val userAnswers = UserAnswers(userAnswersId)
        .set(subPrimaryContactNamePage, "name")
        .success
        .value

      val application = applicationBuilder(Some(userAnswers))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.AddSecondaryContactController.onSubmit.url)
        .withFormUrlEncodedBody("value" -> "false")

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.manageAccount.routes.CaptureSubscriptionAddressController.onPageLoad.url

      }
    }

    "must redirect to book mark page for a GET if no previous existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request     = FakeRequest(GET, controllers.subscription.manageAccount.routes.AddSecondaryContactController.onSubmit.url)

      running(application) {
        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.BookmarkPreventionController.onPageLoad.url
      }
    }

    "must redirect to Journey Recovery for a POST if no previous existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.AddSecondaryContactController.onSubmit.url)
        .withFormUrlEncodedBody(
          "value" -> "true"
        )

      running(application) {
        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
