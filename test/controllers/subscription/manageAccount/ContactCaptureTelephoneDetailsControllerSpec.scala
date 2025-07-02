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
import controllers.actions.TestAuthRetrievals.Ops
import forms.CaptureTelephoneDetailsFormProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{SubPrimaryCapturePhonePage, SubPrimaryContactNamePage, SubPrimaryPhonePreferencePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.auth.core.{AuthConnector, User}
import views.html.subscriptionview.manageAccount.ContactCaptureTelephoneDetailsView

import java.util.UUID
import scala.concurrent.Future

class ContactCaptureTelephoneDetailsControllerSpec extends SpecBase {

  val formProvider = new CaptureTelephoneDetailsFormProvider()
  val id:           String = UUID.randomUUID().toString
  val providerId:   String = UUID.randomUUID().toString
  val providerType: String = UUID.randomUUID().toString

  "ContactCaptureTelephoneDetails Controller for Organisation View Contact details" when {

    "must return OK and the correct view for a GET if page previously not answered" in {
      val userAnswers =
        emptySubscriptionLocalData.setOrException(SubPrimaryContactNamePage, "name").setOrException(SubPrimaryPhonePreferencePage, true)
      val application = applicationBuilder(subscriptionLocalData = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactCaptureTelephoneDetailsController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactCaptureTelephoneDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("name"), "name", isAgent = false, Some("OrgName"))(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET if page previously answered" in {
      val userAnswers =
        emptySubscriptionLocalData
          .setOrException(SubPrimaryContactNamePage, "name")
          .setOrException(SubPrimaryPhonePreferencePage, true)
          .setOrException(SubPrimaryCapturePhonePage, "123132")
      val application = applicationBuilder(subscriptionLocalData = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactCaptureTelephoneDetailsController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactCaptureTelephoneDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("name").fill("123132"), "name", isAgent = false, Some("OrgName"))(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }
    "return a Bad Request and errors when invalid data is submitted of more than 24 characters" in {
      val userAnswersSubCapturePhone =
        emptySubscriptionLocalData.set(SubPrimaryContactNamePage, "name").success.value

      val application = applicationBuilder(subscriptionLocalData = Some(userAnswersSubCapturePhone))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactCaptureTelephoneDetailsController.onSubmit.url)
            .withFormUrlEncodedBody(("phoneNumber", "33333222" * 100))
        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
      }
    }

    "redirect to bookmark page if previous page not answered" in {

      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactCaptureTelephoneDetailsController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

    }

    "redirect to the next page when save and continue if valid data is provided" in {
      when(mockSubscriptionConnector.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))
      val answers = emptySubscriptionLocalData

      val application = applicationBuilder(subscriptionLocalData = Some(answers))
        .overrides(
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        .build()
      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactCaptureTelephoneDetailsController.onSubmit.url)
          .withFormUrlEncodedBody("phoneNumber" -> "34323323333")
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad.url
      }
    }
    "must redirect to journey recovery if no primary contact name is found for POST" in {
      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactCaptureTelephoneDetailsController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }

  "ContactCaptureTelephoneDetails Controller for Agent View Contact details" when {

    "must return OK and the correct view for a GET if page previously not answered" in {
      val userAnswers =
        emptySubscriptionLocalData.setOrException(SubPrimaryContactNamePage, "name").setOrException(SubPrimaryPhonePreferencePage, true)
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
        val request = FakeRequest(
          GET,
          controllers.subscription.manageAccount.routes.ContactCaptureTelephoneDetailsController.onPageLoad.url
        )
        val result = route(application, request).value
        val view   = application.injector.instanceOf[ContactCaptureTelephoneDetailsView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("name"), "name", isAgent = false, Some("OrgName"))(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET if page previously answered" in {
      val userAnswers =
        emptySubscriptionLocalData
          .setOrException(SubPrimaryContactNamePage, "name")
          .setOrException(SubPrimaryPhonePreferencePage, true)
          .setOrException(SubPrimaryCapturePhonePage, "123132")
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
        val request = FakeRequest(
          GET,
          controllers.subscription.manageAccount.routes.ContactCaptureTelephoneDetailsController.onPageLoad.url
        )
        val result = route(application, request).value
        val view   = application.injector.instanceOf[ContactCaptureTelephoneDetailsView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider("name").fill("123132"), "name", isAgent = false, Some("OrgName"))(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }
    "return a Bad Request and errors when invalid data is submitted of more than 24 characters" in {
      val userAnswersSubCapturePhone =
        emptySubscriptionLocalData.set(SubPrimaryContactNamePage, "name").success.value

      val application = applicationBuilder(subscriptionLocalData = Some(userAnswersSubCapturePhone))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()
      when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactCaptureTelephoneDetailsController.onSubmit.url)
            .withFormUrlEncodedBody(("phoneNumber", "33333222" * 100))
        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
      }
    }

    "redirect to bookmark page if previous page not answered" in {

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
        val request = FakeRequest(
          GET,
          controllers.subscription.manageAccount.routes.ContactCaptureTelephoneDetailsController.onPageLoad.url
        )
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

    }
    "must redirect to journey recovery if no primary contact name is found for POST" in {
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
        val request = FakeRequest(
          POST,
          controllers.subscription.manageAccount.routes.ContactCaptureTelephoneDetailsController.onPageLoad.url
        )
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
