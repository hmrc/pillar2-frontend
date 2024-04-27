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

package controllers

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.AgentClientPillar2ReferenceFormProvider
import models.InternalIssueError
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.AgentClientPillar2ReferencePage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SubscriptionService
import views.html.{AgentClientNoMatch, AgentClientPillarIdView}
import views.html.rfm.AgentView

import scala.concurrent.Future

class AgentControllerSpec extends SpecBase {

  val formProvider = new AgentClientPillar2ReferenceFormProvider

  "Agent Controller" must {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, appConfig(application), messages(application)).toString
      }
    }

    "must redirect to error page if the feature flag is false" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), additionalData = Map("features.asaAccessEnabled" -> false)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoadClientPillarId.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/error/page-not-found")
      }
    }

    "must return the correct view if the feature flag is true and user is agent" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoadClientPillarId.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentClientPillarIdView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider())(request, appConfig(application), messages(application)).toString
      }
    }

    "must return the correct view if user answer is present" in {
      val userAnswer = emptyUserAnswers
        .set(AgentClientPillar2ReferencePage, "XMPLR0123456789")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswer)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoadClientPillarId.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentClientPillarIdView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill("XMPLR0123456789"))(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must redirect to next page if the valid ID is inputted and valid read subscription response is returned" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          bind[SubscriptionService].toInstance(mockSubscriptionService)
        )
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        when(mockSubscriptionService.readSubscription(ArgumentMatchers.eq("XMPLR0123456789"))(any()))
          .thenReturn(Future.successful(subscriptionData))

        val request = FakeRequest(POST, routes.AgentController.onSubmitClientPillarId.url)
          .withFormUrlEncodedBody("value" -> "XMPLR0123456789")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.UnderConstructionController.onPageLoad.url
      }
    }

    "must redirect to no match if the read subscription returns a failed response" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          bind[SubscriptionService].toInstance(mockSubscriptionService)
        )
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        when(mockSubscriptionService.readSubscription(any())(any()))
          .thenReturn(Future.failed(InternalIssueError))

        val request = FakeRequest(POST, routes.AgentController.onSubmitClientPillarId.url)
          .withFormUrlEncodedBody("value" -> "XMPLR0123456789")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.AgentController.onPageLoadNoClientMatch.url
      }
    }

    "must return correct view and form with errors if invalid ID is entered" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, routes.AgentController.onSubmitClientPillarId.url)
          .withFormUrlEncodedBody("value" -> "foobar")

        val result = route(application, request).value
        val view   = application.injector.instanceOf[AgentClientPillarIdView]

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(formProvider().bind(Map("value" -> "foobar")))(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }
  }

  "Agent No Client Match" must {
    "must redirect to error page if the feature flag is false" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), additionalData = Map("features.asaAccessEnabled" -> false)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoadNoClientMatch.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/error/page-not-found")
      }
    }

    "must return the correct view if the feature flag is true and user is agent" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoadNoClientMatch.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AgentClientNoMatch]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, appConfig(application), messages(application)).toString
      }
    }
  }
}
