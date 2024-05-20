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
import connectors.{SubscriptionConnector, UserAnswersConnectors}
import controllers.actions.{AgentIdentifierAction, FakeIdentifierAction}
import forms.SecondaryContactEmailFormProvider
import navigation.AmendSubscriptionNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{verify, when}
import pages.{SubAddSecondaryContactPage, SubSecondaryContactNamePage, SubSecondaryEmailPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{Call, PlayBodyParsers}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.subscriptionview.manageAccount.SecondaryContactEmailView

import scala.concurrent.Future

class SecondaryContactEmailControllerSpec extends SpecBase {

  val form         = new SecondaryContactEmailFormProvider()
  val formProvider = form("name")

  "SecondaryContactEmail Controller for Organisation View Contact details" should {

    "must return OK and the correct view for a GET when no data is found" in {
      val ua          = emptySubscriptionLocalData.set(SubSecondaryContactNamePage, "name").success.value
      val application = applicationBuilder(subscriptionLocalData = Some(ua)).build()
      running(application) {
        val request =
          FakeRequest(GET, controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onPageLoad(clientPillar2Id = None).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondaryContactEmailView]

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
        .set(SubSecondaryContactNamePage, "name")
        .success
        .value
        .set(SubSecondaryEmailPage, "my@my.com")
        .success
        .value
      val application = applicationBuilder(subscriptionLocalData = Some(ua)).build()
      running(application) {
        val request =
          FakeRequest(GET, controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onPageLoad(clientPillar2Id = None).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondaryContactEmailView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider.fill("my@my.com"), clientPillar2Id = None, "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val ua = emptySubscriptionLocalData.set(SubSecondaryContactNamePage, "name").success.value
      val application = applicationBuilder(subscriptionLocalData = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockSubscriptionConnector.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onPageLoad(clientPillar2Id = None).url)
            .withFormUrlEncodedBody(("emailAddress", "12345"))

        val view      = application.injector.instanceOf[SecondaryContactEmailView]
        val boundForm = formProvider.bind(Map("emailAddress" -> "12345"))
        val result    = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, clientPillar2Id = None, "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no data is found for secondary contact name" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request =
        FakeRequest(POST, controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onPageLoad(clientPillar2Id = None).url)
          .withFormUrlEncodedBody("emailAddress" -> "name@gmail.com")

      running(application) {
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to bookmark page if no data is found for primary contact name page" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request =
        FakeRequest(GET, controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onPageLoad(clientPillar2Id = None).url)

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

      val userAnswers =
        emptySubscriptionLocalData.setOrException(SubSecondaryContactNamePage, "Keith")

      val expectedUserAnswers = userAnswers.setOrException(SubSecondaryEmailPage, "keith@google.com")

      val application = applicationBuilder(subscriptionLocalData = Some(userAnswers))
        .overrides(
          bind[AmendSubscriptionNavigator].toInstance(mockNavigator),
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onSubmit(clientPillar2Id = None).url)
            .withFormUrlEncodedBody("emailAddress" -> "keith@google.com")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedNextPage.url
        verify(mockSubscriptionConnector).save(eqTo("id"), eqTo(Json.toJson(expectedUserAnswers)))(any[HeaderCarrier])
        verify(mockNavigator).nextPage(SubSecondaryEmailPage, clientPillar2Id = None, expectedUserAnswers)
      }
    }

  }

  "SecondaryContactEmail Controller for Agent View Contact details" should {

    "must return OK and the correct view for a GET when no data is found" in {
      val ua = emptySubscriptionLocalData.set(SubSecondaryContactNamePage, "name").success.value
      val application = applicationBuilder(subscriptionLocalData = Some(ua))
        .overrides(bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction))
        .build()
      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      running(application) {
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))

        val request =
          FakeRequest(
            GET,
            controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onPageLoad(clientPillar2Id = Some(PlrReference)).url
          )

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondaryContactEmailView]

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
        .set(SubSecondaryContactNamePage, "name")
        .success
        .value
        .set(SubSecondaryEmailPage, "my@my.com")
        .success
        .value
      val application = applicationBuilder(subscriptionLocalData = Some(ua))
        .overrides(bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction))
        .build()
      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      running(application) {
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))

        val request =
          FakeRequest(
            GET,
            controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onPageLoad(clientPillar2Id = Some(PlrReference)).url
          )

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondaryContactEmailView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider.fill("my@my.com"), clientPillar2Id = Some(PlrReference), "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val ua = emptySubscriptionLocalData.set(SubSecondaryContactNamePage, "name").success.value
      val application = applicationBuilder(subscriptionLocalData = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction))
        .build()
      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      running(application) {
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))
        when(mockSubscriptionConnector.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        val request =
          FakeRequest(
            POST,
            controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onPageLoad(clientPillar2Id = Some(PlrReference)).url
          )
            .withFormUrlEncodedBody(("emailAddress", "12345"))

        val view      = application.injector.instanceOf[SecondaryContactEmailView]
        val boundForm = formProvider.bind(Map("emailAddress" -> "12345"))
        val result    = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, clientPillar2Id = Some(PlrReference), "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no data is found for secondary contact name" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction))
        .build()
      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      val request =
        FakeRequest(
          POST,
          controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onPageLoad(clientPillar2Id = Some(PlrReference)).url
        )
          .withFormUrlEncodedBody("emailAddress" -> "name@gmail.com")

      running(application) {
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to bookmark page if no data is found for primary contact name page" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction))
        .build()
      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      val request =
        FakeRequest(
          GET,
          controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onPageLoad(clientPillar2Id = Some(PlrReference)).url
        )

      running(application) {
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))

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

      val userAnswers =
        emptySubscriptionLocalData.setOrException(SubSecondaryContactNamePage, "Keith")

      val expectedUserAnswers = userAnswers.setOrException(SubSecondaryEmailPage, "keith@google.com")

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

        val request =
          FakeRequest(
            POST,
            controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onSubmit(clientPillar2Id = Some(PlrReference)).url
          )
            .withFormUrlEncodedBody("emailAddress" -> "keith@google.com")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedNextPage.url
        verify(mockSubscriptionConnector).save(eqTo("id"), eqTo(Json.toJson(expectedUserAnswers)))(any[HeaderCarrier])
        verify(mockNavigator).nextPage(SubSecondaryEmailPage, clientPillar2Id = Some(PlrReference), expectedUserAnswers)
      }
    }

  }
}
