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

package controllers.registration

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.UpeContactNameFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.registrationview.UpeContactNameView

import scala.concurrent.Future

class UpeContactNameControllerSpec extends SpecBase {
  def getUpeContactNameFormProvider: UpeContactNameFormProvider = new UpeContactNameFormProvider()
  val formProvider = new UpeContactNameFormProvider()
  def controller(): UpeContactNameController =
    new UpeContactNameController(
      mockUserAnswersConnectors,
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      getUpeContactNameFormProvider,
      stubMessagesControllerComponents(),
      viewUpeContactName
    )

  "UpeContactName Controller" when {

    "must return OK and the correct view for a GET" in {
      val userAnswersWithNoIdNoContactName =
        emptyUserAnswers.set(RegistrationPage, validNoIdRegData(upeContactName = None)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithNoIdNoContactName)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.UpeContactNameController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpeContactNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithNoId))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, routes.UpeContactNameController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("upeContactName", "Ashley Smith"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.registration.routes.UpeContactEmailController.onPageLoad(NormalMode).url
      }
    }

    "redirect to journey recovery" should {

      "redirected to journey recovery if no data found with GET" in {
        val application = applicationBuilder(userAnswers = None).build()
        running(application) {
          val request = FakeRequest(GET, routes.UpeContactNameController.onPageLoad(NormalMode).url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
      "if upe is registered in the UK" in {
        val userAnswers = emptyUserAnswers.set(RegistrationPage, validWithIdNoGRSRegData).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
        running(application) {
          val request = FakeRequest(GET, routes.UpeContactNameController.onPageLoad(NormalMode).url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        }
      }
      "if any data found from the GRS journey" in {
        val userAnswers = emptyUserAnswers.set(RegistrationPage, validWithIdRegDataForLLP).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
        running(application) {
          val request = FakeRequest(GET, routes.UpeContactNameController.onPageLoad(NormalMode).url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        }
      }

      "if no withoutID data or address is found" in {
        val userAnswers = emptyUserAnswers.set(RegistrationPage, validWithoutIdRegData).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
        running(application) {
          val request = FakeRequest(GET, routes.UpeRegisteredAddressController.onPageLoad(NormalMode).url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "redirected to journey recovery if no data found with POST" in {
        val application = applicationBuilder(userAnswers = None).build()
        running(application) {
          val request = FakeRequest(POST, routes.UpeContactNameController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "upeContactName" -> "Ashley Craig"
            )
          val result = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
