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
import forms.SecondaryContactNameFormProvider
import navigation.AmendSubscriptionNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{verify, when}
import pages.{SubAddSecondaryContactPage, SubPrimaryContactNamePage, SubSecondaryContactNamePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.auth.core.{AuthConnector, User}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.subscriptionview.manageAccount.SecondaryContactNameView

import java.util.UUID
import scala.concurrent.Future

class SecondaryContactNameControllerSpec extends SpecBase {

  val formProvider = new SecondaryContactNameFormProvider()
  val id:           String = UUID.randomUUID().toString
  val providerId:   String = UUID.randomUUID().toString
  val providerType: String = UUID.randomUUID().toString

  "SecondaryContactName Controller for Organisation View Contact details" when {

    "must return OK and the correct view for a GET if no previous data is found" in {
      val ua          = emptySubscriptionLocalData.setOrException(SubAddSecondaryContactPage, true).setOrException(SubPrimaryContactNamePage, "asd")
      val application = applicationBuilder(subscriptionLocalData = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.SecondaryContactNameController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondaryContactNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), isAgent = false, Some("ABC Intl"))(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val ua = emptySubscriptionLocalData
        .setOrException(SubSecondaryContactNamePage, "name")
        .setOrException(SubAddSecondaryContactPage, true)
        .setOrException(SubPrimaryContactNamePage, "asd")
      val application = applicationBuilder(subscriptionLocalData = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.SecondaryContactNameController.onPageLoad.url)

        val view = application.injector.instanceOf[SecondaryContactNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill("name"), isAgent = false, Some("ABC Intl"))(
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
        val request =
          FakeRequest(POST, controllers.subscription.manageAccount.routes.SecondaryContactNameController.onSubmit.url)
            .withFormUrlEncodedBody(("value", stringInput))

        val boundForm = formProvider().bind(Map("value" -> stringInput))

        val view = application.injector.instanceOf[SecondaryContactNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, isAgent = false, Some("ABC Intl"))(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must update subscription data and redirect to the next page" in {
      import play.api.inject.bind

      val expectedNextPage = Call(GET, "/")
      val mockNavigator    = mock[AmendSubscriptionNavigator]
      when(mockNavigator.nextPage(any(), any())).thenReturn(expectedNextPage)
      when(mockSubscriptionConnector.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))

      val userAnswers = emptySubscriptionLocalData
        .setOrException(SubAddSecondaryContactPage, true)

      val expectedUserAnswers = userAnswers.setOrException(SubSecondaryContactNamePage, "Keith")

      val application = applicationBuilder(subscriptionLocalData = Some(userAnswers))
        .overrides(
          bind[AmendSubscriptionNavigator].toInstance(mockNavigator),
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.SecondaryContactNameController.onSubmit.url)
          .withFormUrlEncodedBody("value" -> "Keith")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedNextPage.url
        verify(mockSubscriptionConnector).save(eqTo("id"), eqTo(Json.toJson(expectedUserAnswers)))(any[HeaderCarrier])
        verify(mockNavigator).nextPage(SubSecondaryContactNamePage, expectedUserAnswers)
      }
    }

  }

  "SecondaryContactName Controller for Agent View Contact details" when {

    "must return OK and the correct view for a GET if no previous data is found" in {
      val ua = emptySubscriptionLocalData.setOrException(SubAddSecondaryContactPage, true).setOrException(SubPrimaryContactNamePage, "asd")
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
        val request = FakeRequest(
          GET,
          controllers.subscription.manageAccount.routes.SecondaryContactNameController.onPageLoad.url
        )
        val result = route(application, request).value
        val view   = application.injector.instanceOf[SecondaryContactNameView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), isAgent = false, Some("ABC Intl"))(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val ua = emptySubscriptionLocalData
        .setOrException(SubSecondaryContactNamePage, "name")
        .setOrException(SubAddSecondaryContactPage, true)
        .setOrException(SubPrimaryContactNamePage, "asd")
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
        val request = FakeRequest(
          GET,
          controllers.subscription.manageAccount.routes.SecondaryContactNameController.onPageLoad.url
        )
        val view   = application.injector.instanceOf[SecondaryContactNameView]
        val result = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill("name"), isAgent = false, Some("ABC Intl"))(
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
      when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val stringInput = randomStringGenerator(161)
        val request =
          FakeRequest(
            POST,
            controllers.subscription.manageAccount.routes.SecondaryContactNameController.onSubmit.url
          )
            .withFormUrlEncodedBody(("value", stringInput))
        val boundForm = formProvider().bind(Map("value" -> stringInput))
        val view      = application.injector.instanceOf[SecondaryContactNameView]
        val result    = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, isAgent = false, Some("ABC Intl"))(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must update subscription data and redirect to the next page" in {
      val expectedNextPage = Call(GET, "/")
      val mockNavigator    = mock[AmendSubscriptionNavigator]
      when(mockNavigator.nextPage(any(), any())).thenReturn(expectedNextPage)
      when(mockSubscriptionConnector.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))
      val userAnswers = emptySubscriptionLocalData
        .setOrException(SubAddSecondaryContactPage, true)
      val expectedUserAnswers = userAnswers.setOrException(SubSecondaryContactNamePage, "Keith")
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
        val request = FakeRequest(
          POST,
          controllers.subscription.manageAccount.routes.SecondaryContactNameController.onSubmit.url
        )
          .withFormUrlEncodedBody("value" -> "Keith")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedNextPage.url
        verify(mockSubscriptionConnector).save(eqTo("id"), eqTo(Json.toJson(expectedUserAnswers)))(any[HeaderCarrier])
        verify(mockNavigator).nextPage(SubSecondaryContactNamePage, expectedUserAnswers)
      }
    }

  }
}
