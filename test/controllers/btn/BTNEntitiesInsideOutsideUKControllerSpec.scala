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

package controllers.btn

import base.SpecBase
import connectors.SubscriptionConnector
import controllers.btn.routes._
import forms.BTNEntitiesInsideOutsideUKFormProvider
import models.{MneOrDomestic, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.EntitiesInsideOutsideUKPage
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.btn.{BTNAmendDetailsView, BTNEntitiesInsideOutsideUKView}

import scala.concurrent.Future

class BTNEntitiesInsideOutsideUKControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new BTNEntitiesInsideOutsideUKFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val entitiesInsideOutsideUKRoute: String = BTNEntitiesInsideOutsideUKController.onPageLoad(NormalMode).url

  def application: Application = applicationBuilder(subscriptionLocalData = Some(someSubscriptionLocalData), userAnswers = Some(emptyUserAnswers))
    .configure("features.phase2ScreensEnabled" -> true)
    .overrides(
      bind[SessionRepository].toInstance(mockSessionRepository),
      bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
    )
    .build()

  "EntitiesBothInUKAndOutsideController" when {

    "must return OK and the correct view for a GET" in {

      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
        val request = FakeRequest(GET, entitiesInsideOutsideUKRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BTNEntitiesInsideOutsideUKView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, isAgent = false, Some("orgName"), NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(EntitiesInsideOutsideUKPage, true).success.value

      running(application) {
        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(userAnswers))

        val request = FakeRequest(GET, entitiesInsideOutsideUKRoute)

        val view = application.injector.instanceOf[BTNEntitiesInsideOutsideUKView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), isAgent = false, Some("orgName"), NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must redirect to the CheckYourAnswers page when answer is Yes and valid data is submitted" in {
      running(application) {
        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(emptyUserAnswers))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val request =
          FakeRequest(POST, entitiesInsideOutsideUKRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual CheckYourAnswersController.onPageLoad.url
      }
    }

    "must redirect to a knockback page when a BTN is submitted" in {

      val application = applicationBuilder(subscriptionLocalData = Some(someSubscriptionLocalData), userAnswers = Some(emptyUserAnswers))
        .configure("features.phase2ScreensEnabled" -> true)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        .build()

      when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(submittedBTNRecord))

      running(application) {
        val request = FakeRequest(GET, entitiesInsideOutsideUKRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual CheckYourAnswersController.cannotReturnKnockback.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      running(application) {
        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(emptyUserAnswers))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(false)

        val request =
          FakeRequest(POST, entitiesInsideOutsideUKRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[BTNEntitiesInsideOutsideUKView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, isAgent = false, Some("orgName"), NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must redirect to BTN specific error page when subscription data is not returned" in {
      val application = applicationBuilder(subscriptionLocalData = None, userAnswers = Some(emptyUserAnswers))
        .configure("features.phase2ScreensEnabled" -> true)
        .overrides(
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, entitiesInsideOutsideUKRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.btn.routes.BTNProblemWithServiceController.onPageLoad.url
      }
    }

    "must return OK and the correct view for amend group details page" in {

      val application = applicationBuilder(subscriptionLocalData = Some(emptySubscriptionLocalData))
        .configure("features.phase2ScreensEnabled" -> true)
        .build()

      running(application) {
        val request = FakeRequest(GET, BTNEntitiesInsideOutsideUKController.onPageLoadAmendGroupDetails().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BTNAmendDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(MneOrDomestic.Uk, isAgent = false)(request, applicationConfig, messages(application)).toString
      }
    }

    "redirect to journey recovery if MNE details are not present amend group details page" in {

      val application = applicationBuilder(subscriptionLocalData = None)
        .configure("features.phase2ScreensEnabled" -> true)
        .build()

      running(application) {
        val request = FakeRequest(GET, BTNEntitiesInsideOutsideUKController.onPageLoadAmendGroupDetails().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.btn.routes.BTNProblemWithServiceController.onPageLoad.url
      }
    }

    "must redirect to dashboard for onPageLoad when phase2ScreensEnabled is false" in {
      def application: Application = applicationBuilder(subscriptionLocalData = Some(someSubscriptionLocalData), userAnswers = Some(emptyUserAnswers))
        .configure("features.phase2ScreensEnabled" -> false)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        .build()

      running(application) {
        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(emptyUserAnswers))

        val request = FakeRequest(GET, entitiesInsideOutsideUKRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.DashboardController.onPageLoad.url
      }
    }

    "must redirect to dashboard for onSubmit when phase2ScreensEnabled is false" in {
      def application: Application = applicationBuilder(subscriptionLocalData = Some(someSubscriptionLocalData), userAnswers = Some(emptyUserAnswers))
        .configure("features.phase2ScreensEnabled" -> false)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        .build()

      running(application) {
        when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(emptyUserAnswers))
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val request =
          FakeRequest(POST, entitiesInsideOutsideUKRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.DashboardController.onPageLoad.url
      }
    }

    "must redirect to dashboard for onPageLoadAmendGroupDetails when phase2ScreensEnabled is false" in {

      val application = applicationBuilder(subscriptionLocalData = None)
        .configure("features.phase2ScreensEnabled" -> false)
        .build()

      running(application) {
        val request = FakeRequest(GET, BTNEntitiesInsideOutsideUKController.onPageLoadAmendGroupDetails().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.DashboardController.onPageLoad.url
      }
    }
  }
}
