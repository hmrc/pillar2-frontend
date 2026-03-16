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
import config.FrontendAppConfig
import connectors.FinancialDataConnector
import controllers.actions.TestAuthRetrievals.~
import generators.ModelGenerators
import models.*
import models.financialdata.*
import models.obligationsandsubmissions.ObligationStatus
import models.subscription.*
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{ManageContactDetailsStatusPage, ManageGroupDetailsStatusPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}
import uk.gov.hmrc.http.HeaderCarrier
import utils.DateTimeUtils.toDateFormat
import views.html.HomepageView

import java.time.{Clock, LocalDate}
import java.util.UUID
import scala.concurrent.Future

class HomepageControllerSpec extends SpecBase with ModelGenerators with ScalaCheckPropertyChecks {
  given Clock             = Clock.systemUTC()
  given FrontendAppConfig = applicationConfig

  private type RetrievalsType = Option[String] ~ Enrolments ~ Option[AffinityGroup] ~ Option[CredentialRole] ~ Option[Credentials]

  val enrolments: Set[Enrolment] = Set(
    Enrolment(
      key = "HMRC-PILLAR2-ORG",
      identifiers = Seq(
        EnrolmentIdentifier("PLRID", "12345678"),
        EnrolmentIdentifier("UTR", "ABC12345")
      ),
      state = "activated"
    )
  )

  val agentEnrolment: Set[Enrolment] =
    Set(
      Enrolment("HMRC-PILLAR2-ORG", List(EnrolmentIdentifier("PLRID", "XMPLR0123456789")), "Activated", Some("pillar2-auth"))
    )

  val dashboardInfo: DashboardInfo = DashboardInfo(organisationName = "name", registrationDate = LocalDate.now())

  val id:           String = UUID.randomUUID().toString
  val groupId:      String = UUID.randomUUID().toString
  val providerId:   String = UUID.randomUUID().toString
  val providerType: String = UUID.randomUUID().toString

  "Homepage Controller" should {

    "return OK and the correct view for a GET" in {
      val application =
        applicationBuilder(userAnswers = None, enrolments, additionalData = Map("features.amendMultipleAccountingPeriods" -> false))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService),
            bind[FinancialDataService].toInstance(mockFinancialDataService),
            bind[FinancialDataConnector].toInstance(mockFinancialDataConnector),
            bind[HomepageBannerService].toInstance(mockHomepageBannerService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.HomepageController.onPageLoad().url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))
        when(mockSubscriptionService.maybeReadSubscription(any())(using any())).thenReturn(Future.successful(Some(subscriptionData)))
        when(mockSubscriptionService.cacheSubscription(any())(using any())).thenReturn(Future.successful(subscriptionData))
        when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(obligationsAndSubmissionsSuccessResponse(ObligationStatus.Fulfilled)))
        when(mockObligationsAndSubmissionsService.getDueOrOverdueReturnsStatus(any())).thenReturn(None)
        when(mockFinancialDataService.retrieveFinancialData(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(FinancialData(Seq.empty)))
        when(mockFinancialDataService.retrieveAccountActivityData(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(AccountActivityData(Seq.empty)))
        when(mockHomepageBannerService.determineNotificationArea(any(), any(), any())(using any(), any()))
          .thenReturn(DynamicNotificationAreaState.NoNotification)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[HomepageView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          subscriptionData.upeDetails.organisationName,
          subscriptionData.upeDetails.registrationDate.toDateFormat,
          BtnBanner.Hide,
          None,
          None,
          DynamicNotificationAreaState.NoNotification,
          "12345678",
          isAgent = false,
          hasReturnsUnderEnquiry = false
        )(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "remove ManageGroupDetailsStatusPage and ManageContactDetailsStatusPage from user answers" in {
      val initialUserAnswers = emptyUserAnswers
        .set(ManageGroupDetailsStatusPage, ManageGroupDetailsStatus.InProgress)
        .success
        .value
        .set(ManageContactDetailsStatusPage, ManageContactDetailsStatus.InProgress)
        .success
        .value

      val application =
        applicationBuilder(
          userAnswers = Some(initialUserAnswers),
          enrolments,
          additionalData = Map("features.amendMultipleAccountingPeriods" -> false)
        )
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService),
            bind[FinancialDataService].toInstance(mockFinancialDataService),
            bind[FinancialDataConnector].toInstance(mockFinancialDataConnector)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.HomepageController.onPageLoad().url)

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(initialUserAnswers)))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))
        when(mockSubscriptionService.maybeReadSubscription(any())(using any())).thenReturn(Future.successful(Some(subscriptionData)))
        when(mockSubscriptionService.cacheSubscription(any())(using any())).thenReturn(Future.successful(subscriptionData))
        when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(obligationsAndSubmissionsSuccessResponse(ObligationStatus.Fulfilled)))
        when(mockFinancialDataService.retrieveFinancialData(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(FinancialData(Seq.empty)))

        val result = route(application, request).value
        status(result) mustEqual OK

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())

        val savedAnswers = captor.getValue
        savedAnswers.get(ManageGroupDetailsStatusPage)   must not be defined
        savedAnswers.get(ManageContactDetailsStatusPage) must not be defined
      }
    }

    "retry and eventually succeed when maybeReadSubscription returns RetryableGatewayError then succeeds" in {
      val application =
        applicationBuilder(userAnswers = None, enrolments, additionalData = Map("features.amendMultipleAccountingPeriods" -> false))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService),
            bind[FinancialDataService].toInstance(mockFinancialDataService),
            bind[FinancialDataConnector].toInstance(mockFinancialDataConnector)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.HomepageController.onPageLoad().url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))
        when(mockSubscriptionService.maybeReadSubscription(any())(using any()))
          .thenReturn(Future.failed(RetryableGatewayError))
          .thenReturn(Future.failed(RetryableGatewayError))
          .thenReturn(Future.successful(Some(subscriptionData)))
        when(mockSubscriptionService.cacheSubscription(any())(using any())).thenReturn(Future.successful(subscriptionData))
        when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(obligationsAndSubmissionsSuccessResponse(ObligationStatus.Fulfilled)))
        when(mockFinancialDataService.retrieveFinancialData(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(FinancialData(Seq.empty)))

        val result = route(application, request).value
        status(result) mustEqual OK
        verify(mockSubscriptionService, org.mockito.Mockito.times(3)).maybeReadSubscription(any())(using any())
      }
    }

    "redirect to ViewAmendSubscriptionFailed when retries are exhausted after RetryableGatewayError" in {
      val application =
        applicationBuilder(userAnswers = None, enrolments, additionalData = Map("features.amendMultipleAccountingPeriods" -> false))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService),
            bind[FinancialDataService].toInstance(mockFinancialDataService),
            bind[FinancialDataConnector].toInstance(mockFinancialDataConnector)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.HomepageController.onPageLoad().url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))
        when(mockSubscriptionService.maybeReadSubscription(any())(using any()))
          .thenReturn(Future.failed(RetryableGatewayError))

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad().url
        verify(mockSubscriptionService, org.mockito.Mockito.times(3)).maybeReadSubscription(any())(using any())
      }
    }

    "redirect to registration in progress page when subscription is still processing" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), enrolments, additionalData = Map("features.amendMultipleAccountingPeriods" -> false))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()
      running(application) {
        val request = FakeRequest(GET, controllers.routes.HomepageController.onPageLoad().url)
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSubscriptionService.maybeReadSubscription(any())(using any())).thenReturn(Future.failed(models.UnprocessableEntityError))
        when(mockSubscriptionService.cacheSubscription(any())(using any())).thenReturn(Future.successful(subscriptionData))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.RegistrationInProgressController.onPageLoad("12345678").url

      }
    }

    "redirect to journey recovery if no pillar 2 reference is found in session repository or enrolment data" in {
      val application =
        applicationBuilder(userAnswers = None)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()
      when(mockSessionRepository.get(any()))
        .thenReturn(Future.successful(None))

      running(application) {
        val request = FakeRequest(GET, controllers.routes.HomepageController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

    }

    "not call retrieveAccountActivityData when useAccountActivityApi flag is false" in {
      val application =
        applicationBuilder(
          userAnswers = None,
          enrolments,
          additionalData = Map("features.useAccountActivityApi" -> false, "features.amendMultipleAccountingPeriods" -> false)
        )
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService),
            bind[FinancialDataService].toInstance(mockFinancialDataService),
            bind[FinancialDataConnector].toInstance(mockFinancialDataConnector)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.HomepageController.onPageLoad().url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))
        when(mockSubscriptionService.maybeReadSubscription(any())(using any())).thenReturn(Future.successful(Some(subscriptionData)))
        when(mockSubscriptionService.cacheSubscription(any())(using any())).thenReturn(Future.successful(subscriptionData))
        when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(obligationsAndSubmissionsSuccessResponse(ObligationStatus.Fulfilled)))
        when(mockFinancialDataService.retrieveFinancialData(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(FinancialData(Seq.empty)))

        val result = route(application, request).value
        status(result) mustEqual OK

        verify(mockFinancialDataService, never()).retrieveAccountActivityData(any(), any(), any())(using any[HeaderCarrier])
      }
    }

    "redirect to registration in progress page when subscription is still processing for agent" in {
      val application =
        applicationBuilder(
          userAnswers = Some(emptyUserAnswers),
          agentEnrolment,
          additionalData = Map("features.amendMultipleAccountingPeriods" -> false)
        )
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[AuthConnector].toInstance(mockAuthConnector)
          )
          .build()
      when(mockAuthConnector.authorise[RetrievalsType](any(), any())(using any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )
      when(mockSubscriptionService.maybeReadSubscription(any())(using any())).thenReturn(Future.failed(models.UnprocessableEntityError))
      when(mockSubscriptionService.cacheSubscription(any())(using any())).thenReturn(Future.successful(subscriptionData))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(UserAnswers("id"))))

      running(application) {
        val request = FakeRequest(GET, controllers.routes.HomepageController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.RegistrationInProgressController.onPageLoad("XMPLR0123456789").url
      }
    }

    "return OK using V2 endpoint when amendMultipleAccountingPeriods flag is true" in {
      val v2LocalData = someSubscriptionLocalData.copy(
        registrationDate = Some(subscriptionData.upeDetails.registrationDate)
      )
      val application =
        applicationBuilder(userAnswers = None, enrolments, additionalData = Map("features.amendMultipleAccountingPeriods" -> true))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService),
            bind[FinancialDataService].toInstance(mockFinancialDataService),
            bind[FinancialDataConnector].toInstance(mockFinancialDataConnector)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.HomepageController.onPageLoad().url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))
        when(mockSubscriptionService.readSubscriptionV2AndSave(any(), any())(using any()))
          .thenReturn(Future.successful(v2LocalData))
        when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(obligationsAndSubmissionsSuccessResponse(ObligationStatus.Fulfilled)))
        when(mockFinancialDataService.retrieveFinancialData(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(FinancialData(Seq.empty)))
        when(mockFinancialDataService.retrieveAccountActivityData(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(AccountActivityData(Seq.empty)))

        val result = route(application, request).value
        status(result) mustEqual OK
      }
    }

    "redirect to JourneyRecovery when V2 endpoint returns NoResultFound and flag is true" in {
      val application =
        applicationBuilder(userAnswers = None, enrolments, additionalData = Map("features.amendMultipleAccountingPeriods" -> true))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.HomepageController.onPageLoad().url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))
        when(mockSubscriptionService.readSubscriptionV2AndSave(any(), any())(using any()))
          .thenReturn(Future.failed(NoResultFound))

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to RegistrationInProgress when V2 endpoint returns UnprocessableEntityError and flag is true" in {
      val application =
        applicationBuilder(userAnswers = None, enrolments, additionalData = Map("features.amendMultipleAccountingPeriods" -> true))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.HomepageController.onPageLoad().url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))
        when(mockSubscriptionService.readSubscriptionV2AndSave(any(), any())(using any()))
          .thenReturn(Future.failed(UnprocessableEntityError))

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.RegistrationInProgressController.onPageLoad("12345678").url
      }
    }

    "retry and eventually succeed when V2 endpoint returns RetryableGatewayError then succeeds and flag is true" in {
      val v2LocalData = someSubscriptionLocalData.copy(
        registrationDate = Some(subscriptionData.upeDetails.registrationDate)
      )
      val application =
        applicationBuilder(userAnswers = None, enrolments, additionalData = Map("features.amendMultipleAccountingPeriods" -> true))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService),
            bind[FinancialDataService].toInstance(mockFinancialDataService),
            bind[FinancialDataConnector].toInstance(mockFinancialDataConnector)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.HomepageController.onPageLoad().url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))
        when(mockSubscriptionService.readSubscriptionV2AndSave(any(), any())(using any()))
          .thenReturn(Future.failed(RetryableGatewayError))
          .thenReturn(Future.failed(RetryableGatewayError))
          .thenReturn(Future.successful(v2LocalData))
        when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(obligationsAndSubmissionsSuccessResponse(ObligationStatus.Fulfilled)))
        when(mockFinancialDataService.retrieveFinancialData(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(FinancialData(Seq.empty)))
        when(mockFinancialDataService.retrieveAccountActivityData(any(), any(), any())(using any[HeaderCarrier]))
          .thenReturn(Future.successful(AccountActivityData(Seq.empty)))

        val result = route(application, request).value
        status(result) mustEqual OK
        verify(mockSubscriptionService, org.mockito.Mockito.times(3)).readSubscriptionV2AndSave(any(), any())(using any())
      }
    }

  }
}
