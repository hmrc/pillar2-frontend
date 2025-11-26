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
import forms.IsNFMUKBasedFormProvider
import models.{NormalMode, UserAnswers}
import navigation.NominatedFilingMemberNavigator
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import pages.{FmRegisteredInUKPage, GrsFilingMemberStatusPage, NominateFilingMemberPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import utils.RowStatus
import views.html.fmview.IsNFMUKBasedView

import scala.concurrent.Future

class IsNfmUKBasedControllerSpec extends SpecBase {

  val formProvider = new IsNFMUKBasedFormProvider()

  "IsNFMUKBased Controller" when {

    "must return OK and the correct view for a GET" in {
      val userAnswers = emptyUserAnswers.setOrException(NominateFilingMemberPage, true)
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.IsNfmUKBasedController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IsNFMUKBasedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }
    "redirect to bookmark page if previous page not answered" in {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.IsNfmUKBasedController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId)
          .setOrException(FmRegisteredInUKPage, true)
          .setOrException(NominateFilingMemberPage, true)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.IsNfmUKBasedController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IsNFMUKBasedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(true), NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.fm.routes.IsNfmUKBasedController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[IsNFMUKBasedView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

    "must return Bad Request and show specific error message when no option is selected" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.fm.routes.IsNfmUKBasedController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Select yes if the nominated filing member is registered in the UK")
      }
    }
    "must update the user answers and redirect to the next page when the user answers yes and they have GRS progress" in {

      val expectedNextPage = Call(GET, "/")
      val mockNavigator    = mock[NominatedFilingMemberNavigator]
      when(mockNavigator.nextPage(any(), any(), any())).thenReturn(expectedNextPage)
      when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))

      val userAnswers = emptyUserAnswers
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(GrsFilingMemberStatusPage, RowStatus.Completed)

      val expectedUserAnswers = userAnswers
        .setOrException(FmRegisteredInUKPage, true)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[NominatedFilingMemberNavigator].toInstance(mockNavigator),
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, controllers.fm.routes.IsNfmUKBasedController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedNextPage.url

        verify(mockUserAnswersConnectors).save(eqTo(expectedUserAnswers.id), eqTo(expectedUserAnswers.data))(any())
        verify(mockNavigator).nextPage(FmRegisteredInUKPage, NormalMode, expectedUserAnswers)
      }
    }

    "must update the user answers and redirect to the next page when the user answers yes, unable to fetch GRS status, set GRS status in-progress" in {
      val userAnswers = emptyUserAnswers.setOrException(NominateFilingMemberPage, true)
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))
        val request = FakeRequest(POST, controllers.fm.routes.IsNfmUKBasedController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody("value" -> "true")
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.fm.routes.NfmEntityTypeController.onPageLoad(NormalMode).url
      }
    }

    "must update the user answers and redirect to the next page when the user answers no" in {
      val userAnswers = emptyUserAnswers.setOrException(NominateFilingMemberPage, true)
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))
        val request = FakeRequest(POST, controllers.fm.routes.IsNfmUKBasedController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody("value" -> "false")
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.fm.routes.NfmNameRegistrationController.onPageLoad(NormalMode).url
      }
    }

  }
}
