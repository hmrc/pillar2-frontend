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
import controllers.actions.TestAuthRetrievals.~
import forms.ContactNameComplianceFormProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.SubPrimaryContactNamePage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.auth.core.{AuthConnector, User}
import views.html.subscriptionview.manageAccount.ContactNameComplianceView

import java.util.UUID
import scala.concurrent.Future

class ContactNameComplianceControllerSpec extends SpecBase {

  val formProvider = new ContactNameComplianceFormProvider()
  val id:           String = UUID.randomUUID().toString
  val providerId:   String = UUID.randomUUID().toString
  val providerType: String = UUID.randomUUID().toString

  "ContactNameCompliance Controller for Organisation View Contact details" when {

    "must return OK and the correct view for a GET when no previous data is found" in {

      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactNameComplianceController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactNameComplianceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), isAgent = false, Some("OrgName"))(
          request,
          applicationConfig,
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
        contentAsString(result) mustEqual view(formProvider().fill("name"), isAgent = false, Some("OrgName"))(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(subscriptionLocalData = Some(emptySubscriptionLocalData)).build()

      running(application) {
        val stringInput = randomStringGenerator(161)
        val request     =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactNameComplianceController.onPageLoad().url)
            .withFormUrlEncodedBody(("value", stringInput))

        val boundForm = formProvider().bind(Map("value" -> stringInput))

        val view = application.injector.instanceOf[ContactNameComplianceView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, isAgent = false, Some("OrgName"))(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "redirect to the next page when save and continue if valid data is provided" in {
      when(mockSubscriptionConnector.save(any(), any())(using any())).thenReturn(Future.successful(Json.obj()))
      val answers = emptySubscriptionLocalData

      val application = applicationBuilder(subscriptionLocalData = Some(answers))
        .overrides(
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        .build()
      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactNameComplianceController.onSubmit().url)
          .withFormUrlEncodedBody("value" -> "name")
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController
          .onPageLoad()
          .url
      }
    }

  }

  "ContactNameCompliance Controller for Agent View Contact details" when {

    "must return OK and the correct view for a GET when no previous data is found" in {

      val application = applicationBuilder()
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()
      when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(using any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request =
          FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactNameComplianceController.onPageLoad().url)
        val result = route(application, request).value
        val view   = application.injector.instanceOf[ContactNameComplianceView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), isAgent = false, Some("OrgName"))(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET when previous data is found" in {

      val ua          = emptySubscriptionLocalData.set(SubPrimaryContactNamePage, "name").success.value
      val application = applicationBuilder(subscriptionLocalData = Some(ua))
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()
      when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(using any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request =
          FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactNameComplianceController.onPageLoad().url)
        val result = route(application, request).value
        val view   = application.injector.instanceOf[ContactNameComplianceView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill("name"), isAgent = false, Some("OrgName"))(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(subscriptionLocalData = Some(emptySubscriptionLocalData))
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()
      when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(using any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val stringInput = randomStringGenerator(161)
        val request     =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactNameComplianceController.onPageLoad().url)
            .withFormUrlEncodedBody(("value", stringInput))
        val boundForm = formProvider().bind(Map("value" -> stringInput))
        val view      = application.injector.instanceOf[ContactNameComplianceView]
        val result    = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, isAgent = false, Some("OrgName"))(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

  }

}
