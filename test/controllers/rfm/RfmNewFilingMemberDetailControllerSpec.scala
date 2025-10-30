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
import forms.RfmPrimaryContactNameFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.RfmPrimaryContactNamePage
import play.api.inject
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.rfm.RfmPrimaryContactNameView

import scala.concurrent.Future

class RfmNewFilingMemberDetailControllerSpec extends SpecBase {

  val formProvider = new RfmPrimaryContactNameFormProvider()

  "RFM UPE Name Registration controller" should {

    "return OK and the correct view for a GET" in {
      val ua = emptyUserAnswers
      val application = applicationBuilder(userAnswers = Some(ua))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmPrimaryContactNameController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RfmPrimaryContactNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

    "return OK and populate the view correctly when the question has been previously answered" in {
      val userAnswers = emptyUserAnswers.setOrException(RfmPrimaryContactNamePage, "name")

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmPrimaryContactNameController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[RfmPrimaryContactNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill("name"), NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "redirect to the under construction page when valid data is submitted" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.rfm.routes.RfmPrimaryContactNameController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody("value" -> "name")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmPrimaryContactEmailController.onPageLoad(NormalMode).url
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = None)
        .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.rfm.routes.RfmPrimaryContactNameController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

  }
}
