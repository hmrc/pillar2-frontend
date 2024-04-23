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

package controllers.subscription

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.RfmAddSecondaryContactFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{RfmAddSecondaryContactPage, RfmPrimaryContactEmailPage, RfmPrimaryContactNamePage}
import play.api.inject
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.rfm.RfmAddSecondaryContactView

import scala.concurrent.Future

class RfmAddSecondaryContactControllerSpec extends SpecBase {

  val formProvider = new RfmAddSecondaryContactFormProvider()

  "RfmAddSecondaryContact Controller" when {

    "must return OK and the correct view for a GET" in {
      val userAnswers = UserAnswers(userAnswersId)
        .setOrException(RfmPrimaryContactNamePage, "name")
        .setOrException(RfmPrimaryContactEmailPage, "john.doe@example.com")

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmAddSecondaryContactController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RfmAddSecondaryContactView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), "name", NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .setOrException(RfmPrimaryContactNamePage, "name")
        .setOrException(RfmPrimaryContactEmailPage, "john.doe@example.com")
        .setOrException(RfmAddSecondaryContactPage, true)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmAddSecondaryContactController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RfmAddSecondaryContactView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(true), "name", NormalMode)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "redirect to UnderConstructionController page if RFM access is disabled" in {
      val ua = emptyUserAnswers
      val application = applicationBuilder(userAnswers = Some(ua))
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> false
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmAddSecondaryContactController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }
    }

    "must redirect to RFM Secondary Contact Name page when user selects Yes" in {

      val userAnswers = UserAnswers(userAnswersId)
        .setOrException(RfmPrimaryContactNamePage, "name")
        .setOrException(RfmPrimaryContactEmailPage, "john.doe@example.com")
        .setOrException(RfmAddSecondaryContactPage, true)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.rfm.routes.RfmAddSecondaryContactController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmSecondaryContactNameController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val userAnswers = UserAnswers(userAnswersId)
        .set(RfmPrimaryContactNamePage, "name")
        .success
        .value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.rfm.routes.RfmAddSecondaryContactController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RfmAddSecondaryContactView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, "name", NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must redirect to book mark page for a GET if no previous existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request     = FakeRequest(GET, controllers.rfm.routes.RfmAddSecondaryContactController.onSubmit(NormalMode).url)

      running(application) {
        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url
      }
    }

    "must redirect to Journey Recovery for a POST if no previous existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request = FakeRequest(POST, controllers.rfm.routes.RfmAddSecondaryContactController.onSubmit(NormalMode).url)
        .withFormUrlEncodedBody(
          "value" -> "true"
        )

      running(application) {
        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url
      }
    }

  }
}
