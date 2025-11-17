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
import forms.RfmCorporatePositionFormProvider
import models.NormalMode
import models.rfm.CorporatePosition
import navigation.ReplaceFilingMemberNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{verify, when}
import pages.{RfmCorporatePositionPage, RfmPillar2ReferencePage, RfmRegistrationDatePage}
import play.api.inject
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.rfm.CorporatePositionView

import java.time.LocalDate
import scala.concurrent.Future

class CorporatePositionControllerSpec extends SpecBase {

  val formProvider = new RfmCorporatePositionFormProvider()

  "RFM Corporate Position controller" when {

    "must return OK and the correct view for a GET" in {
      val ua          = emptyUserAnswers
      val application = applicationBuilder(userAnswers = Some(ua))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.CorporatePositionController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CorporatePositionView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

    "must return OK and populate the view correctly when the question has been previously answered" in {
      val userAnswers = emptyUserAnswers.setOrException(RfmCorporatePositionPage, CorporatePosition.NewNfm)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.CorporatePositionController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[CorporatePositionView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(CorporatePosition.NewNfm), NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must redirect to the UPE registration start page when valid data is submitted with UPE" in {
      val ua = emptyUserAnswers
        .setOrException(RfmPillar2ReferencePage, "somePillar2Id")
        .setOrException(RfmRegistrationDatePage, LocalDate.now())
      val application = applicationBuilder(userAnswers = None)
        .overrides(
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          inject.bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))

        val request = FakeRequest(POST, controllers.rfm.routes.CorporatePositionController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody("value" -> CorporatePosition.Upe.toString)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmContactDetailsRegistrationController.onPageLoad().url
      }
    }

    "must redirect to content page to begin their filing member journey when valid data is submitted with New NFM" in {

      val ua = emptyUserAnswers
        .setOrException(RfmPillar2ReferencePage, "somePillar2Id")
        .setOrException(RfmRegistrationDatePage, LocalDate.now())
      val application = applicationBuilder(userAnswers = None)
        .overrides(
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          inject.bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))
        val request = FakeRequest(POST, controllers.rfm.routes.CorporatePositionController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody("value" -> CorporatePosition.NewNfm.toString)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.CheckNewFilingMemberController.onPageLoad(NormalMode).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val ua = emptyUserAnswers
        .setOrException(RfmPillar2ReferencePage, "somePillar2Id")
        .setOrException(RfmRegistrationDatePage, LocalDate.now())
      val application = applicationBuilder(userAnswers = None)
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))
        val request =
          FakeRequest(POST, controllers.rfm.routes.CorporatePositionController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must return Bad Request and show specific error message when no option is selected" in {
      val ua = emptyUserAnswers
        .setOrException(RfmPillar2ReferencePage, "somePillar2Id")
        .setOrException(RfmRegistrationDatePage, LocalDate.now())
      val application = applicationBuilder(userAnswers = None)
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(ua)))
        val request =
          FakeRequest(POST, controllers.rfm.routes.CorporatePositionController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Select if you are the Ultimate Parent Entity or a new nominated filing member")
      }
    }
    "redirect to journey recovery if no data is found in sessionRepository or the BE database" in {
      val application = applicationBuilder(userAnswers = None)
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
        val request =
          FakeRequest(POST, controllers.rfm.routes.CorporatePositionController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url
      }
    }
    "must get the pillar2 ID from the backend database if it exists there and no data is found in the frontend repository and save the relevant data" in {
      val expectedNextPage = Call(GET, "/")
      val mockNavigator    = mock[ReplaceFilingMemberNavigator]

      val ua = emptyUserAnswers
        .setOrException(RfmPillar2ReferencePage, "somePillar2Id")
        .setOrException(RfmCorporatePositionPage, CorporatePosition.NewNfm)
        .setOrException(RfmRegistrationDatePage, LocalDate.now())
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          inject.bind[ReplaceFilingMemberNavigator].toInstance(mockNavigator),
          inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {
        when(mockNavigator.nextPage(any(), any(), any())).thenReturn(expectedNextPage)
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
        val request =
          FakeRequest(POST, controllers.rfm.routes.CorporatePositionController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", "newNfm"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedNextPage.url
        verify(mockUserAnswersConnectors).save(eqTo(ua.id), eqTo(ua.data))(any())
        verify(mockNavigator).nextPage(RfmCorporatePositionPage, NormalMode, ua)
      }
    }

  }
}
