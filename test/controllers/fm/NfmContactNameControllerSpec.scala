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

package controllers.fm

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.NfmContactNameFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{FmContactNamePage, FmRegisteredAddressPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.fmview.NfmContactNameView

import scala.concurrent.Future

class NfmContactNameControllerSpec extends SpecBase {
  val formProvider = new NfmContactNameFormProvider()
  "NFMContactName Controller" when {

    "must return OK and the correct view for a GET if page previously not answered" in {
      val userAnswers = emptyUserAnswers.setOrException(FmRegisteredAddressPage, nonUkAddress)
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmContactNameController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NfmContactNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }
    "must return OK and the correct view for a GET if page previously answered" in {
      val userAnswers = emptyUserAnswers
        .setOrException(FmRegisteredAddressPage, nonUkAddress)
        .setOrException(FmContactNamePage, "name")
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmContactNameController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NfmContactNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill("name"), NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }
    "redirect to bookmark page if previous page not answered" in {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmContactNameController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

    "must return a Bad Request when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val stringInput = randomStringGenerator(106)
        val request     =
          FakeRequest(POST, controllers.fm.routes.NfmContactNameController.onPageLoad(NormalMode).url).withFormUrlEncodedBody(("value", stringInput))
        val boundForm = formProvider().bind(Map("value" -> stringInput))
        val view      = application.injector.instanceOf[NfmContactNameView]
        val result    = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }
    "must redirect to next page when valid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.fm.routes.NfmContactNameController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              ("value", "name")
            )
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.fm.routes.NfmEmailAddressController.onPageLoad(NormalMode).url
      }
    }
  }
}
