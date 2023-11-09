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
import models.{NormalMode, UKAddress}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{upeContactNamePage, upeRegisteredAddressPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.registrationview.UpeContactNameView

import scala.concurrent.Future

class UpeContactNameControllerSpec extends SpecBase {
  def getUpeContactNameFormProvider: UpeContactNameFormProvider = new UpeContactNameFormProvider()
  val formProvider = new UpeContactNameFormProvider()
  val UkAddress: UKAddress = UKAddress("this", None, "over", None, "m123hs", countryCode = "AR")
  "UpeContactName Controller" when {

    "must return OK and the correct view for a GET" in {
      val ua          = emptyUserAnswers.setOrException(upeRegisteredAddressPage, UkAddress)
      val application = applicationBuilder(userAnswers = Some(ua)).build()
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

    "must return OK and the correct view for a GET if page has previously been answered" in {
      val ua          = emptyUserAnswers.setOrException(upeContactNamePage, "name").setOrException(upeRegisteredAddressPage, UkAddress)
      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.UpeContactNameController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpeContactNameView]

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
        val request = FakeRequest(GET, controllers.registration.routes.UpeContactNameController.onPageLoad(NormalMode).url)

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
          FakeRequest(POST, routes.UpeContactNameController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody("value" -> "name")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.registration.routes.UpeContactEmailController.onPageLoad(NormalMode).url
      }
    }

    "return bad request when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, routes.UpeContactNameController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody("value" -> "")

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

  }
}
