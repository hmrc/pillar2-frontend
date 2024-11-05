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
import controllers.actions.TestAuthRetrievals.Ops
import forms.AgentClientPillar2ReferenceFormProvider
import models.InternalIssueError
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{AgentClientOrganisationNamePage, AgentClientPillar2ReferencePage, UnauthorisedClientPillar2ReferencePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}
import views.html._
import views.html.rfm.AgentView

import java.util.UUID
import scala.concurrent.Future

class AgentControllerSpec extends SpecBase {

  val formProvider = new AgentClientPillar2ReferenceFormProvider
  val agentEnrolments: Enrolments = Enrolments(
    Set(Enrolment("HMRC-AS-AGENT", List(EnrolmentIdentifier("AgentReference", "1234")), "Activated", None))
  )
  val agentEnrolmentWithDelegatedAuth: Enrolments = Enrolments(
    Set(
      Enrolment("HMRC-PILLAR2-ORG", List(EnrolmentIdentifier("PLRID", "XMPLR0123456789")), "Activated", Some("pillar2-auth"))
    )
  )
  type RetrievalsType = Option[String] ~ Enrolments ~ Option[AffinityGroup] ~ Option[CredentialRole] ~ Option[Credentials]

  val id:           String = UUID.randomUUID().toString
  val groupId:      String = UUID.randomUUID().toString
  val providerId:   String = UUID.randomUUID().toString
  val providerType: String = UUID.randomUUID().toString

  "Default Agent View" must {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoad.url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[AgentView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, applicationConfig, messages(application)).toString
      }
    }
  }

  "Enter And Submit Client Pillar 2 Id" must {

    "must redirect to error page if the feature flag is false" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), additionalData = Map("features.asaAccessEnabled" -> false))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoadClientPillarId.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/error/page-not-found")
      }
    }

    "must return the correct view if the feature flag is true and user is agent" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector), bind[SessionRepository].toInstance(mockSessionRepository))
        .build()
      val userAnswer = emptyUserAnswers
        .setOrException(AgentClientPillar2ReferencePage, PlrReference)
      when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoadClientPillarId.url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[AgentClientPillarIdView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider())(request, applicationConfig, messages(application)).toString
      }
    }

    "must return the correct view if user answer is present" in {
      val userAnswer = emptyUserAnswers
        .set(UnauthorisedClientPillar2ReferencePage, "XMPLR0123456789")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()

      when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoadClientPillarId.url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[AgentClientPillarIdView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill("XMPLR0123456789"))(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must redirect to next page if the valid ID is inputted and valid read subscription response is returned" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[SubscriptionService].toInstance(mockSubscriptionService),
          bind[AuthConnector].toInstance(mockAuthConnector)
        )
        .build()

      when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      when(mockSubscriptionService.readSubscription(ArgumentMatchers.eq("XMPLR0123456789"))(any()))
        .thenReturn(Future.successful(subscriptionData))

      running(application) {
        val request = FakeRequest(POST, routes.AgentController.onSubmitClientPillarId.url)
          .withFormUrlEncodedBody("value" -> "XMPLR0123456789")
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.AgentController.onPageLoadConfirmClientDetails.url
      }
    }

    "must redirect to no match if the read subscription returns a failed response" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[SubscriptionService].toInstance(mockSubscriptionService),
          bind[AuthConnector].toInstance(mockAuthConnector)
        )
        .build()

      when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      when(mockSubscriptionService.readSubscription(any())(any()))
        .thenReturn(Future.failed(InternalIssueError))

      running(application) {
        val request = FakeRequest(POST, routes.AgentController.onSubmitClientPillarId.url)
          .withFormUrlEncodedBody("value" -> "XMPLR0123456789")
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.AgentController.onPageLoadNoClientMatch.url
      }
    }

    "must return correct view and form with errors if invalid ID is entered" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[AuthConnector].toInstance(mockAuthConnector)
        )
        .build()

      when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      running(application) {
        val request = FakeRequest(POST, routes.AgentController.onSubmitClientPillarId.url)
          .withFormUrlEncodedBody("value" -> "foobar")
        val result = route(application, request).value
        val view   = application.injector.instanceOf[AgentClientPillarIdView]
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(formProvider().bind(Map("value" -> "foobar")))(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }
  }

  "Confirm Client Details" must {

    "redirect to error page if the feature flag is false" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), additionalData = Map("features.asaAccessEnabled" -> false))
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()

      when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoadConfirmClientDetails.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/error/page-not-found")
      }
    }

    "return the correct view if client pillar 2 id and organisation name is in user answers" in {
      val userAnswer = emptyUserAnswers
        .set(UnauthorisedClientPillar2ReferencePage, "XMPLR0123456789")
        .success
        .value
        .set(AgentClientOrganisationNamePage, "Some Org")
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()

      when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoadConfirmClientDetails.url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[AgentClientConfirmDetailsView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view("Some Org", "XMPLR0123456789")(request, applicationConfig, messages(application)).toString
      }
    }

    "return to error page if the if client pillar 2 id and organisation name is not present in user answers" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()

      when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoadConfirmClientDetails.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.AgentController.onPageLoadError.url
      }
    }

    "return to agent dashboard view if agent organisation enrolment is present" in {
      val userAnswer = emptyUserAnswers
        .setOrException(UnauthorisedClientPillar2ReferencePage, PlrReference)

      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
      when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request = FakeRequest(POST, routes.AgentController.onSubmitConfirmClientDetails.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.DashboardController.onPageLoad.url
      }

    }

    "return error page if enrolments are found for agent but there is no organisation enrolment for that pillar 2 id" in {
      val userAnswer = emptyUserAnswers
        .setOrException(UnauthorisedClientPillar2ReferencePage, PlrReference)
      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswer)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))
      when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          ),
          Future.failed(InsufficientEnrolments("HMRC-PILLAR2-ORG"))
        )

      running(application) {
        val request = FakeRequest(POST, routes.AgentController.onSubmitConfirmClientDetails.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.AgentController.onPageLoadUnauthorised.url
      }
    }

  }

  "Agent No Client Match" must {
    "must redirect to error page if the feature flag is false" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), additionalData = Map("features.asaAccessEnabled" -> false))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoadNoClientMatch.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/error/page-not-found")
      }
    }

    "must return the correct view if the feature flag is true and user is agent" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()

      when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ agentEnrolments ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoadNoClientMatch.url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[AgentClientNoMatch]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, applicationConfig, messages(application)).toString
      }
    }
  }

  "Agent Error" must {
    "must redirect to error page if the feature flag is false" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), additionalData = Map("features.asaAccessEnabled" -> false))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoadError.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/error/page-not-found")
      }
    }

    "must return the correct view if the feature flag is true and user is agent" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoadError.url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[AgentErrorView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, applicationConfig, messages(application)).toString
      }
    }
  }

  "Agent Client Unauthorised" must {
    "must redirect to error page if the feature flag is false" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), additionalData = Map("features.asaAccessEnabled" -> false))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoadUnauthorised.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/error/page-not-found")
      }
    }

    "must return the correct view if the feature flag is true" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()

      when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ agentEnrolments ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoadUnauthorised.url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[AgentClientUnauthorisedView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, applicationConfig, messages(application)).toString
      }
    }
  }

  "Agent Individual Error" must {
    "must redirect to error page if the feature flag is false" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), additionalData = Map("features.asaAccessEnabled" -> false))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoadIndividualError.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/error/page-not-found")
      }
    }

    "must return the correct view if the feature flag is true and user is agent" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoadIndividualError.url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[AgentIndividualErrorView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, applicationConfig, messages(application)).toString
      }
    }
  }

  "Agent Organisation Error" must {
    "must redirect to error page if the feature flag is false" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), additionalData = Map("features.asaAccessEnabled" -> false))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoadOrganisationError.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/error/page-not-found")
      }
    }

    "must return the correct view if the feature flag is true" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.AgentController.onPageLoadOrganisationError.url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[AgentOrganisationErrorView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, applicationConfig, messages(application)).toString
      }
    }
  }

}
