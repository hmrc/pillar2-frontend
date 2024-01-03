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
import models.{NonUKAddress, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{fmContactNamePage, fmRegisteredAddressPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.fmview.NfmContactNameView

import scala.concurrent.Future

class NfmContactNameControllerSpec extends SpecBase {
  val formProvider = new NfmContactNameFormProvider()
  val nonUkAddress: NonUKAddress = NonUKAddress("this", None, "over", None, None, countryCode = "AR")
  "NFMContactName Controller" when {

    "must return OK and the correct view for a GET if page previously not answered" in {
      val userAnswers = emptyUserAnswers.setOrException(fmRegisteredAddressPage, nonUkAddress)
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
          appConfig(application),
          messages(application)
        ).toString
      }
    }
    "must return OK and the correct view for a GET if page previously answered" in {
      val userAnswers = emptyUserAnswers
        .setOrException(fmRegisteredAddressPage, nonUkAddress)
        .setOrException(fmContactNamePage, "name")
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
          appConfig(application),
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
        redirectLocation(result) mustBe Some(controllers.routes.BookmarkPreventionController.onPageLoad.url)
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.fm.routes.NfmContactNameController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody("value" -> "goodbye")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.fm.routes.NfmEmailAddressController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.fm.routes.NfmContactNameController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))
        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
      }
    }
  }
}
