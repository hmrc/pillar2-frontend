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

package controllers.rfm

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.NFMRegisteredInUKConfirmationFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.RfmUkBasedPage
import play.api.inject
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.rfm.UkBasedFilingMemberView

import scala.concurrent.Future

class UkBankAccountBasedFilingMemberControllerSpec extends SpecBase {

  val formProvider = new NFMRegisteredInUKConfirmationFormProvider()

  "Is NFM Registered in UK Confirmation Controller" when {
    "onPageLoad" should {
      "return OK and the correct view for a GET if page previously not answered" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.rfm.routes.UkBasedFilingMemberController.onPageLoad(NormalMode).url)
          val view    = application.injector.instanceOf[UkBasedFilingMemberView]
          val result  = route(application, request).value

          contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
          status(result) mustBe OK
        }
      }

      "return OK and the correct view for a GET if page previously answered" in {
        val ua = emptyUserAnswers.setOrException(RfmUkBasedPage, true)
        val application = applicationBuilder(userAnswers = Some(ua))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.rfm.routes.UkBasedFilingMemberController.onPageLoad(NormalMode).url)
          val view    = application.injector.instanceOf[UkBasedFilingMemberView]
          val result  = route(application, request).value

          contentAsString(result) mustEqual view(formProvider().fill(true), NormalMode)(
            request,
            appConfig(application),
            messages(application)
          ).toString
          status(result) mustBe OK
        }
      }
      "redirect to under construction page if rfm functionality is off" in {
        val ua = emptyUserAnswers.setOrException(RfmUkBasedPage, true)
        val application = applicationBuilder(userAnswers = Some(ua))
          .configure("features.rfmAccessEnabled" -> false)
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.rfm.routes.UkBasedFilingMemberController.onPageLoad(NormalMode).url)
          val result  = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
        }
      }
    }
    "onSubmit" should {
      "return bad request if invalid data is submitted" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.UkBasedFilingMemberController.onSubmit(NormalMode).url).withFormUrlEncodedBody(
            "value" -> ""
          )
          val boundForm = formProvider().bind(Map("value" -> ""))

          val view = application.injector.instanceOf[UkBasedFilingMemberView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application), messages(application)).toString
        }
      }

      "redirect to under construction page in case of valid data submission" in {
        val application = applicationBuilder(userAnswers = None)
          .overrides(
            inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()

        running(application) {
          when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
          val request =
            FakeRequest(POST, controllers.rfm.routes.UkBasedFilingMemberController.onSubmit(NormalMode).url).withFormUrlEncodedBody("value" -> "true")
          val result = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rfm.routes.RfmEntityTypeController.onPageLoad(NormalMode).url
        }
      }
    }

  }

}
