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

package controllers

import base.SpecBase
import play.api.inject.bind
import connectors.UserAnswersConnectors
import controllers.fm.ContactNfmByTelephoneController
import forms.ContactNfmByTelephoneFormProvider
import models.fm.ContactNFMByTelephone
import models.{NormalMode, UserAnswers}
import pages.NominatedFilingMemberPage
import play.api.test.FakeRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.test.Helpers._
import views.html.fmview.ContactNfmByTelephoneView

import scala.concurrent.Future

class ContactNfmByTelephoneControllerSpec extends SpecBase {

  val formProvider = new ContactNfmByTelephoneFormProvider()

  def controller(): ContactNfmByTelephoneController =
    new ContactNfmByTelephoneController(
      mockUserAnswersConnectors,
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      formProvider,
      stubMessagesControllerComponents(),
      viewpageNotAvailable,
      viewContactNfmByTelephone
    )

  "ContactNfmByTelephone Controller" when {

    "must return OK and the correct view for a GET" in {

      val userAnswers: UserAnswers = emptyUserAnswers.set(NominatedFilingMemberPage, validNoIdFmData(contactNfmByTelephone = None)).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.ContactNfmByTelephoneController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactNfmByTelephoneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("yes"), NormalMode, "TestName")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers: UserAnswers = emptyUserAnswers.set(NominatedFilingMemberPage, validNoIdFmData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.ContactNfmByTelephoneController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactNfmByTelephoneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("yes").fill(ContactNFMByTelephone.Yes), NormalMode, "TestName")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.fm.routes.ContactNfmByTelephoneController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))
        val boundForm = formProvider("TestName").bind(Map("value" -> ""))
        val view      = application.injector.instanceOf[ContactNfmByTelephoneView]
        val result    = route(application, request).value
        status(result) mustEqual BAD_REQUEST
      }
    }

    "redirect to capture telephone page when valid data is submitted with value YES" in {
      val userAnswers: UserAnswers = emptyUserAnswers.set(NominatedFilingMemberPage, validNoIdFmData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.fm.routes.ContactNfmByTelephoneController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", "yes"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.fm.routes.NfmCaptureTelephoneDetailsController.onPageLoad(NormalMode).url
      }
    }

    " redirect to CheckYourAnswers page when valid data is submitted with value NO" in {
      val userAnswers: UserAnswers = emptyUserAnswers.set(NominatedFilingMemberPage, validNoIdFmData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.fm.routes.ContactNfmByTelephoneController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", "no"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad.url
      }

    }

  }
}
