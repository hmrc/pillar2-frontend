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
import forms.RfmSecondaryContactNameFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{RfmAddSecondaryContactPage, RfmPrimaryContactNamePage, RfmSecondaryContactNamePage}
import play.api.inject
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.rfm.RfmSecondaryContactNameView

import scala.concurrent.Future

class RfmSecondaryContactNameControllerSpec extends SpecBase {

  val formProvider = new RfmSecondaryContactNameFormProvider()

  "SecondaryContactName Controller" when {

    "must return OK and the correct view for a GET if no previous data is found" in {
      val ua          = emptyUserAnswers.setOrException(RfmAddSecondaryContactPage, true).setOrException(RfmPrimaryContactNamePage, "asd")
      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmSecondaryContactNameController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RfmSecondaryContactNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val ua = emptyUserAnswers
        .setOrException(RfmSecondaryContactNamePage, "name")
        .setOrException(RfmAddSecondaryContactPage, true)
        .setOrException(RfmPrimaryContactNamePage, "asd")
      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmSecondaryContactNameController.onPageLoad(NormalMode).url)

        val view = application.injector.instanceOf[RfmSecondaryContactNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill("name"), NormalMode)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must redirect to the under-construction page when rfm feature is set to false" in {

      val ua = emptyUserAnswers
      val application = applicationBuilder(userAnswers = Some(ua))
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> false
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmSecondaryContactNameController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }
    }

    "redirect to JourneyRecoveryController if previous page not answered" in {
      val application = applicationBuilder(userAnswers = None)
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmSecondaryContactNameController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

    "must redirect to RFM Secondary Email page with updated valid data" in {
      val ua = emptyUserAnswers
        .setOrException(RfmSecondaryContactNamePage, "name")
        .setOrException(RfmAddSecondaryContactPage, true)
        .setOrException(RfmPrimaryContactNamePage, "asd")

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.rfm.routes.RfmSecondaryContactNameController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody("value" -> "alexy")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmSecondaryContactEmailController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.rfm.routes.RfmSecondaryContactNameController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RfmSecondaryContactNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

  }
}
