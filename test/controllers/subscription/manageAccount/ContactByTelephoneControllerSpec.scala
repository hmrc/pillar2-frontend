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
import forms.ContactByTelephoneFormProvider
import navigation.AmendSubscriptionNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import pages.{SubPrimaryContactNamePage, SubPrimaryPhonePreferencePage}
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.{AuthConnector, User}
import uk.gov.hmrc.auth.core.retrieve.Credentials
import views.html.subscriptionview.manageAccount.ContactByTelephoneView

import java.util.UUID
import scala.concurrent.Future

class ContactByTelephoneControllerSpec extends SpecBase {

  val form = new ContactByTelephoneFormProvider()
  val formProvider: Form[Boolean] = form("name")
  val id:           String        = UUID.randomUUID().toString
  val providerId:   String        = UUID.randomUUID().toString
  val providerType: String        = UUID.randomUUID().toString

  "ContactByTelephone Controller for Organisation View Contact details" should {

    "return OK and the correct view for a GET" in {
      val ua =
        emptySubscriptionLocalData.set(SubPrimaryContactNamePage, "name").success.value

      val application = applicationBuilder(subscriptionLocalData = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactByTelephoneController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactByTelephoneView]
        status(result) mustEqual OK

        contentAsString(result) mustEqual view(formProvider.fill(false), "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString

      }
    }

    "return OK and the correct view for a GET if page has previously been answered" in {
      val ua =
        emptySubscriptionLocalData
          .set(SubPrimaryContactNamePage, "name")
          .success
          .value
          .set(SubPrimaryPhonePreferencePage, true)
          .success
          .value

      val application = applicationBuilder(subscriptionLocalData = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactByTelephoneController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactByTelephoneView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider.fill(true), "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString

      }
    }

    "redirect to journey recovery if no primary contact name is found" in {

      val application = applicationBuilder(subscriptionLocalData = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactByTelephoneController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

      }
    }
    "must return bad request when invalid data is submitted" in {
      val userAnswer  = emptySubscriptionLocalData.set(SubPrimaryContactNamePage, "name").success.value
      val application = applicationBuilder(subscriptionLocalData = Some(userAnswer)).build()
      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactByTelephoneController.onSubmit.url)
          .withFormUrlEncodedBody("value" -> "")
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

  }

  "ContactByTelephone Controller for Agent View Contact details" should {

    "return OK and the correct view for a GET" in {
      val ua =
        emptySubscriptionLocalData.set(SubPrimaryContactNamePage, "name").success.value
      val application = applicationBuilder(subscriptionLocalData = Some(ua))
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()
      when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactByTelephoneController.onPageLoad.url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[ContactByTelephoneView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider.fill(false), "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString

      }
    }

    "return OK and the correct view for a GET if page has previously been answered" in {
      val ua =
        emptySubscriptionLocalData
          .set(SubPrimaryContactNamePage, "name")
          .success
          .value
          .set(SubPrimaryPhonePreferencePage, true)
          .success
          .value

      val application = applicationBuilder(subscriptionLocalData = Some(ua))
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()
      when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactByTelephoneController.onPageLoad.url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[ContactByTelephoneView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider.fill(true), "name")(
          request,
          appConfig(application),
          messages(application)
        ).toString

      }
    }

    "redirect to journey recovery if no primary contact name is found" in {

      val application = applicationBuilder(subscriptionLocalData = None)
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()
      when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ContactByTelephoneController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return bad request when invalid data is submitted" in {
      val userAnswer = emptySubscriptionLocalData.set(SubPrimaryContactNamePage, "name").success.value
      val application = applicationBuilder(subscriptionLocalData = Some(userAnswer))
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()
      when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactByTelephoneController.onSubmit.url)
          .withFormUrlEncodedBody("value" -> "")
        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
      }
    }

  }

  " redirect to journey recovery if no primary contact name is found for POST" in {
    val application = applicationBuilder(subscriptionLocalData = None).build()
    running(application) {
      val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactByTelephoneController.onSubmit.url)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
    }
  }

  " remove primary telephone and redirect to the next page when the user answers no" in {
    import play.api.inject.bind
    val expectedNextPage = Call(GET, "/")
    val mockNavigator    = mock[AmendSubscriptionNavigator]
    when(mockNavigator.nextPage(any(), any())).thenReturn(expectedNextPage)
    when(mockSubscriptionConnector.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))

    val answers = emptySubscriptionLocalData.copy(subPrimaryPhonePreference = false, subPrimaryCapturePhone = Some("12312"))

    val application = applicationBuilder(subscriptionLocalData = Some(answers))
      .overrides(
        bind[AmendSubscriptionNavigator].toInstance(mockNavigator),
        bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
      )
      .build()

    running(application) {
      val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactByTelephoneController.onSubmit.url)
        .withFormUrlEncodedBody("value" -> "false")

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual expectedNextPage.url

      verify(mockNavigator).nextPage(SubPrimaryPhonePreferencePage, answers)
    }
  }

  "redirect to the next page when the user answers yes" in {
    import play.api.inject.bind
    val expectedNextPage = Call(GET, "/")
    val mockNavigator    = mock[AmendSubscriptionNavigator]
    when(mockNavigator.nextPage(any(), any())).thenReturn(expectedNextPage)
    when(mockSubscriptionConnector.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))

    val answers = emptySubscriptionLocalData.copy(subPrimaryPhonePreference = true, subPrimaryCapturePhone = Some("12312"))

    val application = applicationBuilder(subscriptionLocalData = Some(answers))
      .overrides(
        bind[AmendSubscriptionNavigator].toInstance(mockNavigator),
        bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
      )
      .build()

    running(application) {
      val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ContactByTelephoneController.onSubmit.url)
        .withFormUrlEncodedBody("value" -> "true")

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual expectedNextPage.url

      verify(mockNavigator).nextPage(SubPrimaryPhonePreferencePage, answers)
    }
  }
}
