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
import forms.CaptureTelephoneDetailsFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.RegistrationPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.registrationview.CaptureTelephoneDetailsView

import scala.concurrent.Future

class CaptureTelephoneDetailsControllerSpec extends SpecBase {

  val formProvider = new CaptureTelephoneDetailsFormProvider()

  def controller(): CaptureTelephoneDetailsController =
    new CaptureTelephoneDetailsController(
      mockUserAnswersConnectors,
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      formProvider,
      stubMessagesControllerComponents(),
      viewCaptureTelephoneDetailsView
    )

  "Capture Telephone Details Controller" when {

    "must return OK and the correct view for a GET" in {
      val userAnswersData =
        emptyUserAnswers.set(RegistrationPage, validNoIdRegData(telephoneNumber = None)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersData)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.CaptureTelephoneDetailsController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CaptureTelephoneDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("TestName"), NormalMode, "TestName")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must redirect to checkYourAnswers when valid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithNoId))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, routes.CaptureTelephoneDetailsController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              ("telephoneNumber", "123456789")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.registration.routes.UpeCheckYourAnswersController.onPageLoad.url
      }

    }
    "return bad request if required fields are not filled" in {

      val request =
        FakeRequest(POST, routes.CaptureTelephoneDetailsController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("telephoneNumber", ""))
      when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
      val result = controller.onSubmit(NormalMode)()(request)
      status(result) mustEqual BAD_REQUEST

    }

    "redirect to journey recovery for GET " when {
      "no data is found" in {
        val application = applicationBuilder(userAnswers = None).build()
        running(application) {
          val request = FakeRequest(GET, controllers.registration.routes.CaptureTelephoneDetailsController.onPageLoad(NormalMode).url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
      "upe is registered in the uk" in {
        val userAnswer  = emptyUserAnswers.set(RegistrationPage, validWithIdNoGRSRegData).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswer)).build()
        running(application) {
          val request = FakeRequest(GET, controllers.registration.routes.CaptureTelephoneDetailsController.onPageLoad(NormalMode).url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "GRS data is found in the database" in {
        val userAnswer  = emptyUserAnswers.set(RegistrationPage, validWithIdRegDataForLLP).success.value
        val application = applicationBuilder(Some(userAnswer)).build()
        running(application) {
          val request = FakeRequest(GET, controllers.registration.routes.CaptureTelephoneDetailsController.onPageLoad(NormalMode).url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
      "no contact name is found" in {
        val userAnswer  = emptyUserAnswers.set(RegistrationPage, validWithoutIdRegDataWithoutName()).success.value
        val application = applicationBuilder(Some(userAnswer)).build()
        running(application) {
          val request = FakeRequest(GET, controllers.registration.routes.CaptureTelephoneDetailsController.onPageLoad(NormalMode).url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
    "Journey recovery when no data for POST" in {
      val application = applicationBuilder(userAnswers = None).build()
      val request = FakeRequest(POST, routes.CaptureTelephoneDetailsController.onSubmit(NormalMode).url).withFormUrlEncodedBody(
        "telephoneNumber" -> "12321321"
      )
      running(application) {
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
