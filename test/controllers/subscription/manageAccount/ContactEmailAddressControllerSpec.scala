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
import connectors.UserAnswersConnectors
import controllers.actions.{AgentIdentifierAction, FakeIdentifierAction}
import forms.ContactEmailAddressFormProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{SubPrimaryContactNamePage, SubPrimaryEmailPage}
import play.api.inject.bind
import play.api.mvc.PlayBodyParsers
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.subscriptionview.manageAccount.ContactEmailAddressView

class ContactEmailAddressControllerSpec extends SpecBase {

  val formProvider = new ContactEmailAddressFormProvider()

  "ContactEmail Address Controller for Organisations View Contact details" when {

    "must return OK and the correct view for a GET if page previously  not answered" in {

      val userAnswersSubContactEmail =
        emptySubscriptionLocalData.set(SubPrimaryContactNamePage, "name").success.value

      val application = applicationBuilder(subscriptionLocalData = Some(userAnswersSubContactEmail))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactEmailAddressController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactEmailAddressView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("name"), "name", None)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET if page previously has been answered" in {

      val userAnswersSubContactEmail =
        emptySubscriptionLocalData.setOrException(SubPrimaryContactNamePage, "name").setOrException(SubPrimaryEmailPage, "hello@goodbye.com")

      val application = applicationBuilder(subscriptionLocalData = Some(userAnswersSubContactEmail))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactEmailAddressController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactEmailAddressView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("name").fill("hello@goodbye.com"), "name", None)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }
    "must return a Bad Request when invalid data is submitted" in {
      val ua          = emptySubscriptionLocalData.set(SubPrimaryContactNamePage, "name").success.value
      val application = applicationBuilder(subscriptionLocalData = Some(ua)).build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactEmailAddressController.onPageLoad().url)
            .withFormUrlEncodedBody(("emailAddress", ""))
        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
      }
    }

    "redirect to bookmark page  if no primary contact name is found for GET" in {

      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactEmailAddressController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

    }
    "must redirect to journey recovery if no primary contact name is found for POST" in {
      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactEmailAddressController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  "ContactEmail Address Controller for Agents View Contact details" when {

    "must return OK and the correct view for a GET if page previously  not answered" in {

      val userAnswersSubContactEmail =
        emptySubscriptionLocalData.set(SubPrimaryContactNamePage, "name").success.value

      val application = applicationBuilder(subscriptionLocalData = Some(userAnswersSubContactEmail))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction))
        .build()
      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      running(application) {
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))

        val request = FakeRequest(
          GET,
          controllers.subscription.manageAccount.routes.ContactEmailAddressController.onPageLoad(clientPillar2Id = Some(PlrReference)).url
        )

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactEmailAddressView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("name"), "name", Some(PlrReference))(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET if page previously has been answered" in {

      val userAnswersSubContactEmail =
        emptySubscriptionLocalData.setOrException(SubPrimaryContactNamePage, "name").setOrException(SubPrimaryEmailPage, "hello@goodbye.com")

      val application = applicationBuilder(subscriptionLocalData = Some(userAnswersSubContactEmail))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction))
        .build()
      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      running(application) {
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))
        val request = FakeRequest(
          GET,
          controllers.subscription.manageAccount.routes.ContactEmailAddressController.onPageLoad(clientPillar2Id = Some(PlrReference)).url
        )

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactEmailAddressView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("name").fill("hello@goodbye.com"), "name", Some(PlrReference))(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }
    "must return a Bad Request when invalid data is submitted" in {
      val ua = emptySubscriptionLocalData.set(SubPrimaryContactNamePage, "name").success.value
      val application = applicationBuilder(subscriptionLocalData = Some(ua))
        .overrides(bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction))
        .build()
      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      running(application) {
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))

        val request =
          FakeRequest(
            POST,
            controllers.subscription.manageAccount.routes.ContactEmailAddressController.onPageLoad(clientPillar2Id = Some(PlrReference)).url
          )
            .withFormUrlEncodedBody(("emailAddress", ""))
        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
      }
    }

    "redirect to bookmark page  if no primary contact name is found for GET" in {

      val application = applicationBuilder()
        .overrides(bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction))
        .build()
      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      running(application) {
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))

        val request = FakeRequest(
          GET,
          controllers.subscription.manageAccount.routes.ContactEmailAddressController.onPageLoad(clientPillar2Id = Some(PlrReference)).url
        )
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

    }
    "must redirect to journey recovery if no primary contact name is found for POST" in {
      val application = applicationBuilder()
        .overrides(bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction))
        .build()
      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      running(application) {
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))

        val request = FakeRequest(
          POST,
          controllers.subscription.manageAccount.routes.ContactEmailAddressController.onPageLoad(clientPillar2Id = Some(PlrReference)).url
        )
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
