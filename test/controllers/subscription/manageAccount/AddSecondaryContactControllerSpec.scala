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
import controllers.actions.TestAuthRetrievals.Ops
import forms.AddSecondaryContactFormProvider
import navigation.AmendSubscriptionNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{verify, when}
import pages._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.auth.core.{AuthConnector, User}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.subscriptionview.manageAccount.AddSecondaryContactView

import java.util.UUID
import scala.concurrent.Future

class AddSecondaryContactControllerSpec extends SpecBase {

  val formProvider = new AddSecondaryContactFormProvider()
  val id:           String = UUID.randomUUID().toString
  val providerId:   String = UUID.randomUUID().toString
  val providerType: String = UUID.randomUUID().toString

  "AddSecondaryContactController for Organisation View Contact details" when {

    "must populate the view correctly on a GET" in {

      val userAnswers = emptySubscriptionLocalData
        .setOrException(SubPrimaryContactNamePage, "name")
        .setOrException(SubPrimaryEmailPage, "asda")
        .setOrException(SubAddSecondaryContactPage, true)

      val application = applicationBuilder(subscriptionLocalData = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.AddSecondaryContactController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddSecondaryContactView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("name").fill(true), "name")(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val userAnswers = emptySubscriptionLocalData
        .set(SubPrimaryContactNamePage, "name")
        .success
        .value
      val application = applicationBuilder(subscriptionLocalData = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.AddSecondaryContactController.onPageLoad.url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider("name").bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AddSecondaryContactView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, "name")(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }
    "must redirect to book mark page for a GET if no previous existing data is found" in {

      val application = applicationBuilder().build()
      val request     = FakeRequest(GET, controllers.subscription.manageAccount.routes.AddSecondaryContactController.onSubmit.url)

      running(application) {
        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no previous existing data is found" in {

      val application = applicationBuilder().build()
      val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.AddSecondaryContactController.onSubmit.url)
        .withFormUrlEncodedBody(
          "value" -> "true"
        )

      running(application) {
        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must update subscription data and redirect to the next page when the user answers no" in {
      import play.api.inject.bind

      val expectedNextPage = Call(GET, "/")
      val mockNavigator    = mock[AmendSubscriptionNavigator]
      when(mockNavigator.nextPage(any(), any())).thenReturn(expectedNextPage)
      when(mockSubscriptionConnector.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))

      val userAnswers = emptySubscriptionLocalData
        .setOrException(SubAddSecondaryContactPage, true)
        .setOrException(SubSecondaryContactNamePage, "name")
        .setOrException(SubSecondaryEmailPage, "name")
        .setOrException(SubSecondaryPhonePreferencePage, true)
        .setOrException(SubSecondaryCapturePhonePage, "123123")

      val expectedUserAnswers = userAnswers
        .copy(subAddSecondaryContact = false)
        .remove(SubSecondaryContactNamePage)
        .success
        .value
        .remove(SubSecondaryEmailPage)
        .success
        .value
        .remove(SubSecondaryPhonePreferencePage)
        .success
        .value
        .remove(SubSecondaryCapturePhonePage)
        .success
        .value

      val application = applicationBuilder(subscriptionLocalData = Some(userAnswers))
        .overrides(
          bind[AmendSubscriptionNavigator].toInstance(mockNavigator),
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.AddSecondaryContactController.onSubmit.url)
          .withFormUrlEncodedBody("value" -> "false")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedNextPage.url
        verify(mockSubscriptionConnector).save(eqTo("id"), eqTo(Json.toJson(expectedUserAnswers)))(any[HeaderCarrier])
        verify(mockNavigator).nextPage(SubAddSecondaryContactPage, expectedUserAnswers)
      }
    }

    "must update subscription data and redirect to the next page when the user answers yes" in {

      val userAnswers = emptySubscriptionLocalData
        .setOrException(SubAddSecondaryContactPage, true)
        .setOrException(SubSecondaryContactNamePage, "name")

      val application = applicationBuilder(subscriptionLocalData = Some(userAnswers))
        .overrides(
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        .build()
      running(application) {
        when(mockSubscriptionConnector.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.AddSecondaryContactController.onSubmit.url)
          .withFormUrlEncodedBody("value" -> "true")
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad.url
      }
    }

  }

  "AddSecondaryContactController for Agent View Contact details" when {

    "must populate the view correctly on a GET" in {

      val userAnswers = emptySubscriptionLocalData
        .setOrException(SubPrimaryContactNamePage, "name")
        .setOrException(SubPrimaryEmailPage, "asda")
        .setOrException(SubAddSecondaryContactPage, true)

      val application = applicationBuilder(subscriptionLocalData = Some(userAnswers))
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()

      when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.AddSecondaryContactController.onPageLoad.url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[AddSecondaryContactView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("name").fill(true), "name")(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val userAnswers = emptySubscriptionLocalData
        .set(SubPrimaryContactNamePage, "name")
        .success
        .value

      val application = applicationBuilder(subscriptionLocalData = Some(userAnswers))
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()

      when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.AddSecondaryContactController.onPageLoad.url)
            .withFormUrlEncodedBody(("value", ""))
        val boundForm = formProvider("name").bind(Map("value" -> ""))
        val view      = application.injector.instanceOf[AddSecondaryContactView]
        val result    = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, "name")(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }
    "must redirect to book mark page for a GET if no previous existing data is found" in {

      val application = applicationBuilder()
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()

      when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.AddSecondaryContactController.onSubmit.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no previous existing data is found" in {

      val application = applicationBuilder()
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()

      when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.AddSecondaryContactController.onSubmit.url)
          .withFormUrlEncodedBody(
            "value" -> "true"
          )
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must update subscription data and redirect to the next page when the user answers no" in {
      import play.api.inject.bind

      val expectedNextPage = Call(GET, "/")
      val mockNavigator    = mock[AmendSubscriptionNavigator]
      when(mockNavigator.nextPage(any(), any())).thenReturn(expectedNextPage)
      when(mockSubscriptionConnector.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))

      val userAnswers = emptySubscriptionLocalData
        .setOrException(SubAddSecondaryContactPage, true)
        .setOrException(SubSecondaryContactNamePage, "name")
        .setOrException(SubSecondaryEmailPage, "name")
        .setOrException(SubSecondaryPhonePreferencePage, true)
        .setOrException(SubSecondaryCapturePhonePage, "123123")

      val expectedUserAnswers = userAnswers
        .copy(subAddSecondaryContact = false)
        .remove(SubSecondaryContactNamePage)
        .success
        .value
        .remove(SubSecondaryEmailPage)
        .success
        .value
        .remove(SubSecondaryPhonePreferencePage)
        .success
        .value
        .remove(SubSecondaryCapturePhonePage)
        .success
        .value

      val application = applicationBuilder(subscriptionLocalData = Some(userAnswers))
        .overrides(
          bind[AmendSubscriptionNavigator].toInstance(mockNavigator),
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
          bind[AuthConnector].toInstance(mockAuthConnector)
        )
        .build()

      when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.AddSecondaryContactController.onSubmit.url)
          .withFormUrlEncodedBody("value" -> "false")
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedNextPage.url
        verify(mockSubscriptionConnector).save(eqTo("id"), eqTo(Json.toJson(expectedUserAnswers)))(any[HeaderCarrier])
        verify(mockNavigator).nextPage(SubAddSecondaryContactPage, expectedUserAnswers)
      }
    }

  }
}
