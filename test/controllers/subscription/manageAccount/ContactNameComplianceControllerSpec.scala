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
import controllers.actions.{AgentIdentifierAction, FakeIdentifierAction}
import forms.ContactNameComplianceFormProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.SubPrimaryContactNamePage
import play.api.inject.bind
import play.api.mvc.PlayBodyParsers
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.subscriptionview.manageAccount.ContactNameComplianceView

class ContactNameComplianceControllerSpec extends SpecBase {

  val formProvider = new ContactNameComplianceFormProvider()

  "ContactNameCompliance Controller for Organisation View Contact details" when {

    "must return OK and the correct view for a GET when no previous data is found" in {

      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactNameComplianceController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactNameComplianceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), clientPillar2Id = None)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET when previous data is found" in {

      val ua          = emptySubscriptionLocalData.set(SubPrimaryContactNamePage, "name").success.value
      val application = applicationBuilder(subscriptionLocalData = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactNameComplianceController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactNameComplianceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill("name"), clientPillar2Id = None)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(subscriptionLocalData = Some(emptySubscriptionLocalData)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactNameComplianceController.onPageLoad().url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ContactNameComplianceView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, clientPillar2Id = None)(request, appConfig(application), messages(application)).toString
      }
    }

  }

  "ContactNameCompliance Controller for Agent View Contact details" when {

    "must return OK and the correct view for a GET when no previous data is found" in {

      val application = applicationBuilder()
        .overrides(bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction))
        .build()
      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      running(application) {
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))

        val request =
          FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactNameComplianceController.onPageLoad(Some(PlrReference)).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactNameComplianceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), Some(PlrReference))(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET when previous data is found" in {

      val ua = emptySubscriptionLocalData.set(SubPrimaryContactNamePage, "name").success.value
      val application = applicationBuilder(subscriptionLocalData = Some(ua))
        .overrides(bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction))
        .build()
      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      running(application) {
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))

        val request =
          FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactNameComplianceController.onPageLoad(Some(PlrReference)).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactNameComplianceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill("name"), Some(PlrReference))(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(subscriptionLocalData = Some(emptySubscriptionLocalData))
        .overrides(bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction))
        .build()
      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      running(application) {
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))

        val request =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactNameComplianceController.onPageLoad(Some(PlrReference)).url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ContactNameComplianceView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, Some(PlrReference))(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

  }

}
