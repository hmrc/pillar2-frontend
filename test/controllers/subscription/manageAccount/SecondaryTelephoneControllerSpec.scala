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

package controllers.subscription.manageAccount

import base.SpecBase
import connectors.SubscriptionConnector
import controllers.actions.{AgentIdentifierAction, FakeIdentifierAction}
import forms.SecondaryTelephoneFormProvider
import navigation.AmendSubscriptionNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{verify, when}
import pages._
import play.api.libs.json.Json
import play.api.mvc.{Call, PlayBodyParsers}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.subscriptionview.manageAccount.SecondaryTelephoneView
import play.api.inject.bind

import scala.concurrent.Future

class SecondaryTelephoneControllerSpec extends SpecBase {

  val form         = new SecondaryTelephoneFormProvider()
  val formProvider = form("test")

  "SecondaryTelephone Controller for Organisation View Contact details" when {

    "must return OK and the correct view for a GET if no previous data is found" in {

      val ua = emptySubscriptionLocalData
        .setOrException(SubSecondaryContactNamePage, "name")
        .setOrException(SubSecondaryPhonePreferencePage, true)
      val application = applicationBuilder(subscriptionLocalData = Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.SecondaryTelephoneController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondaryTelephoneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider, clientPillar2Id = None, "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val ua = emptySubscriptionLocalData
        .setOrException(SubSecondaryContactNamePage, "name")
        .setOrException(SubSecondaryPhonePreferencePage, true)
        .setOrException(SubSecondaryCapturePhonePage, "1234567")

      val application = applicationBuilder(subscriptionLocalData = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.SecondaryTelephoneController.onPageLoad().url)

        val view = application.injector.instanceOf[SecondaryTelephoneView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider.fill("1234567"), clientPillar2Id = None, "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val ua          = emptySubscriptionLocalData.set(SubSecondaryContactNamePage, "name").success.value
      val application = applicationBuilder(subscriptionLocalData = Some(ua)).build()
      val bigString   = "123" * 100
      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.SecondaryTelephoneController.onPageLoad().url)
            .withFormUrlEncodedBody(("value", bigString))

        val boundForm = formProvider.bind(Map("value" -> bigString))

        val view = application.injector.instanceOf[SecondaryTelephoneView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, clientPillar2Id = None, "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }
    "redirect to bookmark page if previous page not answered" in {

      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.SecondaryTelephoneController.onPageLoad().url)
        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no previous existing data is found" in {

      val application = applicationBuilder().build()
      val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.SecondaryTelephoneController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "12233444")

      running(application) {
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must update subscription data and redirect to the next page" in {
      import play.api.inject.bind

      val expectedNextPage = Call(GET, "/")
      val mockNavigator    = mock[AmendSubscriptionNavigator]
      when(mockNavigator.nextPage(any(), any(), any())).thenReturn(expectedNextPage)
      when(mockSubscriptionConnector.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))

      val userAnswers = emptySubscriptionLocalData
        .setOrException(SubAddSecondaryContactPage, true)
        .setOrException(SubSecondaryContactNamePage, "name")
        .setOrException(SubSecondaryEmailPage, "name")
        .setOrException(SubSecondaryPhonePreferencePage, true)

      val expectedUserAnswers = userAnswers.setOrException(SubSecondaryCapturePhonePage, "123456")

      val application = applicationBuilder(subscriptionLocalData = Some(userAnswers))
        .overrides(
          bind[AmendSubscriptionNavigator].toInstance(mockNavigator),
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.SecondaryTelephoneController.onSubmit().url)
          .withFormUrlEncodedBody("value" -> "123456")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedNextPage.url
        verify(mockSubscriptionConnector).save(eqTo("id"), eqTo(Json.toJson(expectedUserAnswers)))(any[HeaderCarrier])
        verify(mockNavigator).nextPage(SubSecondaryCapturePhonePage, clientPillar2Id = None, expectedUserAnswers)
      }
    }

  }

  "SecondaryTelephone Controller for Agent View Contact details" when {

    "must return OK and the correct view for a GET if no previous data is found" in {

      val ua = emptySubscriptionLocalData
        .setOrException(SubSecondaryContactNamePage, "name")
        .setOrException(SubSecondaryPhonePreferencePage, true)
      val application = applicationBuilder(subscriptionLocalData = Some(ua))
        .overrides(bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction))
        .build()
      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      running(application) {
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))

        val request = FakeRequest(
          GET,
          controllers.subscription.manageAccount.routes.SecondaryTelephoneController.onPageLoad(clientPillar2Id = Some(PlrReference)).url
        )

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondaryTelephoneView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider, clientPillar2Id = Some(PlrReference), "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val ua = emptySubscriptionLocalData
        .setOrException(SubSecondaryContactNamePage, "name")
        .setOrException(SubSecondaryPhonePreferencePage, true)
        .setOrException(SubSecondaryCapturePhonePage, "1234567")

      val application = applicationBuilder(subscriptionLocalData = Some(ua))
        .overrides(bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction))
        .build()
      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      running(application) {
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))

        val request = FakeRequest(
          GET,
          controllers.subscription.manageAccount.routes.SecondaryTelephoneController.onPageLoad(clientPillar2Id = Some(PlrReference)).url
        )

        val view = application.injector.instanceOf[SecondaryTelephoneView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider.fill("1234567"), clientPillar2Id = Some(PlrReference), "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val ua = emptySubscriptionLocalData.set(SubSecondaryContactNamePage, "name").success.value
      val application = applicationBuilder(subscriptionLocalData = Some(ua))
        .overrides(bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction))
        .build()
      val bigString   = "123" * 100
      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      running(application) {
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))

        val request =
          FakeRequest(
            POST,
            controllers.subscription.manageAccount.routes.SecondaryTelephoneController.onPageLoad(clientPillar2Id = Some(PlrReference)).url
          )
            .withFormUrlEncodedBody(("value", bigString))

        val boundForm = formProvider.bind(Map("value" -> bigString))

        val view = application.injector.instanceOf[SecondaryTelephoneView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, clientPillar2Id = Some(PlrReference), "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }
    "redirect to bookmark page if previous page not answered" in {

      val application = applicationBuilder()
        .overrides(bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction))
        .build()
      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      running(application) {
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))

        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.SecondaryTelephoneController.onPageLoad().url)
        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no previous existing data is found" in {

      val application = applicationBuilder()
        .overrides(bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction))
        .build()
      val request = FakeRequest(
        POST,
        controllers.subscription.manageAccount.routes.SecondaryTelephoneController.onSubmit(clientPillar2Id = Some(PlrReference)).url
      )
        .withFormUrlEncodedBody("value" -> "12233444")
      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      running(application) {
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must update subscription data and redirect to the next page" in {

      val expectedNextPage = Call(GET, "/")
      val mockNavigator    = mock[AmendSubscriptionNavigator]
      when(mockNavigator.nextPage(any(), any(), any())).thenReturn(expectedNextPage)
      when(mockSubscriptionConnector.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))

      val userAnswers = emptySubscriptionLocalData
        .setOrException(SubAddSecondaryContactPage, true)
        .setOrException(SubSecondaryContactNamePage, "name")
        .setOrException(SubSecondaryEmailPage, "name")
        .setOrException(SubSecondaryPhonePreferencePage, true)

      val expectedUserAnswers = userAnswers.setOrException(SubSecondaryCapturePhonePage, "123456")

      val application = applicationBuilder(subscriptionLocalData = Some(userAnswers))
        .overrides(
          bind[AmendSubscriptionNavigator].toInstance(mockNavigator),
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
          bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction)
        )
        .build()
      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      running(application) {
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))

        val request = FakeRequest(
          POST,
          controllers.subscription.manageAccount.routes.SecondaryTelephoneController.onSubmit(clientPillar2Id = Some(PlrReference)).url
        )
          .withFormUrlEncodedBody("value" -> "123456")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedNextPage.url
        verify(mockSubscriptionConnector).save(eqTo("id"), eqTo(Json.toJson(expectedUserAnswers)))(any[HeaderCarrier])
        verify(mockNavigator).nextPage(SubSecondaryCapturePhonePage, clientPillar2Id = Some(PlrReference), expectedUserAnswers)
      }
    }

  }
}
