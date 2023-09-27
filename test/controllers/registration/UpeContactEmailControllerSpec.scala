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
import forms.UpeContactEmailFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.RegistrationPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.registrationview.UpeContactEmailView

import scala.concurrent.Future

class UpeContactEmailControllerSpec extends SpecBase {

  def getUpeContactEmailFormProvider: UpeContactEmailFormProvider = new UpeContactEmailFormProvider()
  val formProvider = new UpeContactEmailFormProvider()
  def controller(): UpeContactEmailController =
    new UpeContactEmailController(
      mockUserAnswersConnectors,
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      getUpeContactEmailFormProvider,
      stubMessagesControllerComponents(),
      viewUpeContactEmail
    )

  "UpeContactEmail Controller" when {

    "must return OK and the correct view for a GET" in {
      val userAnswersData =
        emptyUserAnswers.set(RegistrationPage, validNoIdRegData(emailAddress = None)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersData)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.UpeContactEmailController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpeContactEmailView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("TestName"), NormalMode, "TestName")(
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
          FakeRequest(POST, routes.UpeContactEmailController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("emailAddress", "AshleySmith@email.com"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.registration.routes.ContactUPEByTelephoneController.onPageLoad(NormalMode).url
      }
    }
    "Bad request when no data in POST" in {
      val request =
        FakeRequest(POST, routes.UpeContactEmailController.onSubmit(NormalMode).url)
      when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
      val result = controller.onSubmit(NormalMode)()(request)
      status(result) mustEqual BAD_REQUEST

    }

    "Journey Recovery when no data in GET" in {
      val application = applicationBuilder(userAnswers = None).build()
      val request = FakeRequest(GET, routes.UpeContactEmailController.onPageLoad(NormalMode).url).withFormUrlEncodedBody(
        "emailAddress" -> "al@gmail.com"
      )
      running(application) {
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
    "Journey Recovery when no data in POST" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request = FakeRequest(POST, routes.UpeContactEmailController.onSubmit(NormalMode).url).withFormUrlEncodedBody(
        "emailAddress" -> "al@gmail.com"
      )
      running(application) {
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
