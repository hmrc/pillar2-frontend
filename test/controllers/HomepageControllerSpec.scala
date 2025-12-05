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
import connectors.FinancialDataConnector
import controllers.actions.TestAuthRetrievals.~
import generators.ModelGenerators
import models.*
import models.DueAndOverdueReturnBannerScenario.*
import models.OutstandingPaymentBannerScenario.{Outstanding, Paid}
import models.financialdata.*
import models.financialdata.FinancialTransaction.{OutstandingCharge, Payment}
import models.obligationsandsubmissions.ObligationStatus
import models.subscription.*
import models.subscription.AccountStatus.{ActiveAccount, InactiveAccount}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{ManageContactDetailsStatusPage, ManageGroupDetailsStatusPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.{FinancialDataService, ObligationsAndSubmissionsService, SubscriptionService}
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}
import uk.gov.hmrc.http.HeaderCarrier
import utils.DateTimeUtils.toDateFormat
import views.html.HomepageView

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.Future

class HomepageControllerSpec extends SpecBase with ModelGenerators with ScalaCheckPropertyChecks {

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

  "Dashboard Controller" should {

    "return OK and the correct view for a GET" in {
      val application =
        applicationBuilder(userAnswers = None, enrolments)
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
        applicationBuilder(userAnswers = Some(initialUserAnswers), enrolments)
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

    "redirect to registration in progress page when subscription is still processing" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), enrolments)
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

    "redirect to registration in progress page when subscription is still processing for agent" in {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), agentEnrolment)
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

  }

  "getDueOrOverdueReturnsStatus" should {

    "return Due when UKTR obligation is open and due date has not passed" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller: HomepageController = application.injector.instanceOf[HomepageController]

        val futureDueDate = LocalDate.now().plusDays(7)
        val obligations   = Seq(
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.UKTR,
            status = ObligationStatus.Open,
            canAmend = false,
            submissions = Seq.empty
          )
        )
        val accountingPeriod = models.obligationsandsubmissions.AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(12),
          endDate = LocalDate.now(),
          dueDate = futureDueDate,
          underEnquiry = false,
          obligations = obligations
        )
        val obligationsAndSubmissions = models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess(
          processingDate = java.time.ZonedDateTime.now(),
          accountingPeriodDetails = Seq(accountingPeriod)
        )

        val result = controller.getDueOrOverdueReturnsStatus(obligationsAndSubmissions)

        result mustBe Some(Due)
      }
    }

    "return Overdue when UKTR obligation is open and due date has passed" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[HomepageController]

        val pastDueDate = LocalDate.now().minusDays(7)
        val obligations = Seq(
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.UKTR,
            status = ObligationStatus.Open,
            canAmend = false,
            submissions = Seq.empty
          )
        )
        val accountingPeriod = models.obligationsandsubmissions.AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(12),
          endDate = LocalDate.now(),
          dueDate = pastDueDate,
          underEnquiry = false,
          obligations = obligations
        )
        val obligationsAndSubmissions = models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess(
          processingDate = java.time.ZonedDateTime.now(),
          accountingPeriodDetails = Seq(accountingPeriod)
        )

        val result = controller.getDueOrOverdueReturnsStatus(obligationsAndSubmissions)

        result mustBe Some(Overdue)
      }
    }

    "return None when both UKTR and GIR obligations are fulfilled" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[HomepageController]

        val pastDueDate = LocalDate.now().minusDays(7)
        val obligations = Seq(
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.UKTR,
            status = ObligationStatus.Fulfilled,
            canAmend = false,
            submissions = Seq.empty
          ),
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.GIR,
            status = ObligationStatus.Fulfilled,
            canAmend = false,
            submissions = Seq.empty
          )
        )
        val accountingPeriod = models.obligationsandsubmissions.AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(12),
          endDate = LocalDate.now(),
          dueDate = pastDueDate,
          underEnquiry = false,
          obligations = obligations
        )
        val obligationsAndSubmissions = models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess(
          processingDate = java.time.ZonedDateTime.now(),
          accountingPeriodDetails = Seq(accountingPeriod)
        )

        val result = controller.getDueOrOverdueReturnsStatus(obligationsAndSubmissions)

        result mustBe None
      }
    }

    "return Due when both UKTR and GIR obligations are open and due date has not passed" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[HomepageController]

        val futureDueDate = LocalDate.now().plusDays(7)
        val obligations   = Seq(
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.UKTR,
            status = ObligationStatus.Open,
            canAmend = false,
            submissions = Seq.empty
          ),
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.GIR,
            status = ObligationStatus.Open,
            canAmend = false,
            submissions = Seq.empty
          )
        )
        val accountingPeriod = models.obligationsandsubmissions.AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(12),
          endDate = LocalDate.now(),
          dueDate = futureDueDate,
          underEnquiry = false,
          obligations = obligations
        )
        val obligationsAndSubmissions = models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess(
          processingDate = java.time.ZonedDateTime.now(),
          accountingPeriodDetails = Seq(accountingPeriod)
        )

        val result = controller.getDueOrOverdueReturnsStatus(obligationsAndSubmissions)

        result mustBe Some(Due)
      }
    }

    "return Due when UKTR is open, GIR is fulfilled and due date has not passed" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[HomepageController]

        val futureDueDate = LocalDate.now().plusDays(7)
        val obligations   = Seq(
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.UKTR,
            status = ObligationStatus.Open,
            canAmend = false,
            submissions = Seq.empty
          ),
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.GIR,
            status = ObligationStatus.Fulfilled,
            canAmend = false,
            submissions = Seq.empty
          )
        )
        val accountingPeriod = models.obligationsandsubmissions.AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(12),
          endDate = LocalDate.now(),
          dueDate = futureDueDate,
          underEnquiry = false,
          obligations = obligations
        )
        val obligationsAndSubmissions = models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess(
          processingDate = java.time.ZonedDateTime.now(),
          accountingPeriodDetails = Seq(accountingPeriod)
        )

        val result = controller.getDueOrOverdueReturnsStatus(obligationsAndSubmissions)

        result mustBe Some(Due)
      }
    }

    "return Due when UKTR is fulfilled, GIR is open and due date has not passed" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[HomepageController]

        val futureDueDate = LocalDate.now().plusDays(7)
        val obligations   = Seq(
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.UKTR,
            status = ObligationStatus.Fulfilled,
            canAmend = false,
            submissions = Seq.empty
          ),
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.GIR,
            status = ObligationStatus.Open,
            canAmend = false,
            submissions = Seq.empty
          )
        )
        val accountingPeriod = models.obligationsandsubmissions.AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(12),
          endDate = LocalDate.now(),
          dueDate = futureDueDate,
          underEnquiry = false,
          obligations = obligations
        )
        val obligationsAndSubmissions = models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess(
          processingDate = java.time.ZonedDateTime.now(),
          accountingPeriodDetails = Seq(accountingPeriod)
        )

        val result = controller.getDueOrOverdueReturnsStatus(obligationsAndSubmissions)

        result mustBe Some(Due)
      }
    }

    "return Overdue when both UKTR and GIR obligations are open and due date has passed" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[HomepageController]

        val pastDueDate = LocalDate.now().minusDays(7)
        val obligations = Seq(
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.UKTR,
            status = ObligationStatus.Open,
            canAmend = false,
            submissions = Seq.empty
          ),
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.GIR,
            status = ObligationStatus.Open,
            canAmend = false,
            submissions = Seq.empty
          )
        )
        val accountingPeriod = models.obligationsandsubmissions.AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(12),
          endDate = LocalDate.now(),
          dueDate = pastDueDate,
          underEnquiry = false,
          obligations = obligations
        )
        val obligationsAndSubmissions = models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess(
          processingDate = java.time.ZonedDateTime.now(),
          accountingPeriodDetails = Seq(accountingPeriod)
        )

        val result = controller.getDueOrOverdueReturnsStatus(obligationsAndSubmissions)

        result mustBe Some(Overdue)
      }
    }

    "return Incomplete when UKTR is open, GIR is fulfilled and due date has passed" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[HomepageController]

        val pastDueDate = LocalDate.now().minusDays(7)
        val obligations = Seq(
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.UKTR,
            status = ObligationStatus.Open,
            canAmend = false,
            submissions = Seq.empty
          ),
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.GIR,
            status = ObligationStatus.Fulfilled,
            canAmend = false,
            submissions = Seq.empty
          )
        )
        val accountingPeriod = models.obligationsandsubmissions.AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(12),
          endDate = LocalDate.now(),
          dueDate = pastDueDate,
          underEnquiry = false,
          obligations = obligations
        )
        val obligationsAndSubmissions = models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess(
          processingDate = java.time.ZonedDateTime.now(),
          accountingPeriodDetails = Seq(accountingPeriod)
        )

        val result = controller.getDueOrOverdueReturnsStatus(obligationsAndSubmissions)

        result mustBe Some(Incomplete)
      }
    }

    "return Incomplete when UKTR is fulfilled, GIR is open and due date has passed" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[HomepageController]

        val pastDueDate = LocalDate.now().minusDays(7)
        val obligations = Seq(
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.UKTR,
            status = ObligationStatus.Fulfilled,
            canAmend = false,
            submissions = Seq.empty
          ),
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.GIR,
            status = ObligationStatus.Open,
            canAmend = false,
            submissions = Seq.empty
          )
        )
        val accountingPeriod = models.obligationsandsubmissions.AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(12),
          endDate = LocalDate.now(),
          dueDate = pastDueDate,
          underEnquiry = false,
          obligations = obligations
        )
        val obligationsAndSubmissions = models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess(
          processingDate = java.time.ZonedDateTime.now(),
          accountingPeriodDetails = Seq(accountingPeriod)
        )

        val result = controller.getDueOrOverdueReturnsStatus(obligationsAndSubmissions)

        result mustBe Some(Incomplete)
      }
    }

    "return Due when UKTR is fulfilled and GIR is open but due date has not passed" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[HomepageController]

        val futureDueDate = LocalDate.now().plusDays(7)
        val obligations   = Seq(
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.UKTR,
            status = ObligationStatus.Fulfilled,
            canAmend = false,
            submissions = Seq.empty
          ),
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.GIR,
            status = ObligationStatus.Open,
            canAmend = false,
            submissions = Seq.empty
          )
        )
        val accountingPeriod = models.obligationsandsubmissions.AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(12),
          endDate = LocalDate.now(),
          dueDate = futureDueDate,
          underEnquiry = false,
          obligations = obligations
        )
        val obligationsAndSubmissions = models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess(
          processingDate = java.time.ZonedDateTime.now(),
          accountingPeriodDetails = Seq(accountingPeriod)
        )

        val result = controller.getDueOrOverdueReturnsStatus(obligationsAndSubmissions)

        result mustBe Some(Due)
      }
    }

    "return None when only UKTR obligation is fulfilled" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[HomepageController]

        val pastDueDate = LocalDate.now().minusDays(7)
        val obligations = Seq(
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.UKTR,
            status = ObligationStatus.Fulfilled,
            canAmend = false,
            submissions = Seq.empty
          )
        )
        val accountingPeriod = models.obligationsandsubmissions.AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(12),
          endDate = LocalDate.now(),
          dueDate = pastDueDate,
          underEnquiry = false,
          obligations = obligations
        )
        val obligationsAndSubmissions = models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess(
          processingDate = java.time.ZonedDateTime.now(),
          accountingPeriodDetails = Seq(accountingPeriod)
        )

        val result = controller.getDueOrOverdueReturnsStatus(obligationsAndSubmissions)

        result mustBe None
      }
    }

    "return Due when only GIR obligation is open and due date has not passed" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[HomepageController]

        val futureDueDate = LocalDate.now().plusDays(7)
        val obligations   = Seq(
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.GIR,
            status = ObligationStatus.Open,
            canAmend = false,
            submissions = Seq.empty
          )
        )
        val accountingPeriod = models.obligationsandsubmissions.AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(12),
          endDate = LocalDate.now(),
          dueDate = futureDueDate,
          underEnquiry = false,
          obligations = obligations
        )
        val obligationsAndSubmissions = models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess(
          processingDate = java.time.ZonedDateTime.now(),
          accountingPeriodDetails = Seq(accountingPeriod)
        )

        val result = controller.getDueOrOverdueReturnsStatus(obligationsAndSubmissions)

        result mustBe Some(Due)
      }
    }

    "return Overdue when only GIR obligation is open and due date has passed" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[HomepageController]

        val pastDueDate = LocalDate.now().minusDays(7)
        val obligations = Seq(
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.GIR,
            status = ObligationStatus.Open,
            canAmend = false,
            submissions = Seq.empty
          )
        )
        val accountingPeriod = models.obligationsandsubmissions.AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(12),
          endDate = LocalDate.now(),
          dueDate = pastDueDate,
          underEnquiry = false,
          obligations = obligations
        )
        val obligationsAndSubmissions = models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess(
          processingDate = java.time.ZonedDateTime.now(),
          accountingPeriodDetails = Seq(accountingPeriod)
        )

        val result = controller.getDueOrOverdueReturnsStatus(obligationsAndSubmissions)

        result mustBe Some(Overdue)
      }
    }

    "return None when only GIR obligation is fulfilled" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[HomepageController]

        val pastDueDate = LocalDate.now().minusDays(7)
        val obligations = Seq(
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.GIR,
            status = ObligationStatus.Fulfilled,
            canAmend = false,
            submissions = Seq.empty
          )
        )
        val accountingPeriod = models.obligationsandsubmissions.AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(12),
          endDate = LocalDate.now(),
          dueDate = pastDueDate,
          underEnquiry = false,
          obligations = obligations
        )
        val obligationsAndSubmissions = models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess(
          processingDate = java.time.ZonedDateTime.now(),
          accountingPeriodDetails = Seq(accountingPeriod)
        )

        val result = controller.getDueOrOverdueReturnsStatus(obligationsAndSubmissions)

        result mustBe None
      }
    }

    "return None when no accounting periods exist" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[HomepageController]

        val obligationsAndSubmissions = models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess(
          processingDate = java.time.ZonedDateTime.now(),
          accountingPeriodDetails = Seq.empty
        )

        val result = controller.getDueOrOverdueReturnsStatus(obligationsAndSubmissions)

        result mustBe None
      }
    }

    "return the earliest period status when multiple accounting periods exist" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[HomepageController]

        val futureDueDate = LocalDate.now().plusDays(7)
        val pastDueDate   = LocalDate.now().minusDays(7)

        // First period (earlier end date) - Due
        val firstPeriodObligations = Seq(
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.UKTR,
            status = ObligationStatus.Open,
            canAmend = false,
            submissions = Seq.empty
          )
        )
        val firstPeriod = models.obligationsandsubmissions.AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(24),
          endDate = LocalDate.now().minusDays(10), // Earlier end date
          dueDate = futureDueDate,
          underEnquiry = false,
          obligations = firstPeriodObligations
        )

        // Second period (later end date) - Overdue
        val secondPeriodObligations = Seq(
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.UKTR,
            status = ObligationStatus.Open,
            canAmend = false,
            submissions = Seq.empty
          )
        )
        val secondPeriod = models.obligationsandsubmissions.AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(12),
          endDate = LocalDate.now().minusDays(5), // Later end date
          dueDate = pastDueDate,
          underEnquiry = false,
          obligations = secondPeriodObligations
        )

        val obligationsAndSubmissions = models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess(
          processingDate = java.time.ZonedDateTime.now(),
          accountingPeriodDetails = Seq(secondPeriod, firstPeriod) // Order shouldn't matter
        )

        val result = controller.getDueOrOverdueReturnsStatus(obligationsAndSubmissions)

        result mustBe Some(Overdue)
      }
    }

    "return Received when UKTR and GIR obligations are both fulfilled and within 60 day period" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[HomepageController]

        val pastDueDate          = LocalDate.now().minusDays(7)
        val recentSubmissionDate = java.time.ZonedDateTime.now().minusDays(30)
        val obligations          = Seq(
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.UKTR,
            status = ObligationStatus.Fulfilled,
            canAmend = false,
            submissions = Seq(
              models.obligationsandsubmissions.Submission(
                submissionType = models.obligationsandsubmissions.SubmissionType.UKTR_CREATE,
                receivedDate = recentSubmissionDate,
                country = None
              )
            )
          ),
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.GIR,
            status = ObligationStatus.Fulfilled,
            canAmend = false,
            submissions = Seq(
              models.obligationsandsubmissions.Submission(
                submissionType = models.obligationsandsubmissions.SubmissionType.GIR,
                receivedDate = recentSubmissionDate,
                country = None
              )
            )
          )
        )
        val accountingPeriod = models.obligationsandsubmissions.AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(12),
          endDate = LocalDate.now(),
          dueDate = pastDueDate,
          underEnquiry = false,
          obligations = obligations
        )
        val obligationsAndSubmissions = models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess(
          processingDate = java.time.ZonedDateTime.now(),
          accountingPeriodDetails = Seq(accountingPeriod)
        )

        val result = controller.getDueOrOverdueReturnsStatus(obligationsAndSubmissions)

        result mustBe Some(Received)
      }
    }

    "return None when UKTR and GIR obligations are both fulfilled and outside 60 day period" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[HomepageController]

        val recentSubmissionDate = java.time.ZonedDateTime.now().minusDays(70)
        val obligations          = Seq(
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.UKTR,
            status = ObligationStatus.Fulfilled,
            canAmend = false,
            submissions = Seq(
              models.obligationsandsubmissions.Submission(
                submissionType = models.obligationsandsubmissions.SubmissionType.UKTR_CREATE,
                receivedDate = recentSubmissionDate,
                country = None
              )
            )
          ),
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.GIR,
            status = ObligationStatus.Fulfilled,
            canAmend = false,
            submissions = Seq(
              models.obligationsandsubmissions.Submission(
                submissionType = models.obligationsandsubmissions.SubmissionType.GIR,
                receivedDate = recentSubmissionDate,
                country = None
              )
            )
          )
        )
        val accountingPeriod = models.obligationsandsubmissions.AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(12),
          endDate = LocalDate.now(),
          dueDate = pastDueDate,
          underEnquiry = false,
          obligations = obligations
        )
        val obligationsAndSubmissions = models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess(
          processingDate = java.time.ZonedDateTime.now(),
          accountingPeriodDetails = Seq(accountingPeriod)
        )

        val result = controller.getDueOrOverdueReturnsStatus(obligationsAndSubmissions)

        result mustBe None
      }
    }

    "return None when only UKTR is fulfilled and within 60 day period" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[HomepageController]

        val recentSubmissionDate = java.time.ZonedDateTime.now().minusDays(30)
        val obligations          = Seq(
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.UKTR,
            status = ObligationStatus.Fulfilled,
            canAmend = false,
            submissions = Seq(
              models.obligationsandsubmissions.Submission(
                submissionType = models.obligationsandsubmissions.SubmissionType.UKTR_CREATE,
                receivedDate = recentSubmissionDate,
                country = None
              )
            )
          )
        )
        val accountingPeriod = models.obligationsandsubmissions.AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(12),
          endDate = LocalDate.now(),
          dueDate = pastDueDate,
          underEnquiry = false,
          obligations = obligations
        )
        val obligationsAndSubmissions = models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess(
          processingDate = java.time.ZonedDateTime.now(),
          accountingPeriodDetails = Seq(accountingPeriod)
        )

        val result = controller.getDueOrOverdueReturnsStatus(obligationsAndSubmissions)

        result mustBe None
      }
    }

    "return None when only GIR is fulfilled and within 60 day period" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[HomepageController]

        val recentSubmissionDate = java.time.ZonedDateTime.now().minusDays(30)
        val obligations          = Seq(
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.GIR,
            status = ObligationStatus.Fulfilled,
            canAmend = false,
            submissions = Seq(
              models.obligationsandsubmissions.Submission(
                submissionType = models.obligationsandsubmissions.SubmissionType.GIR,
                receivedDate = recentSubmissionDate,
                country = None
              )
            )
          )
        )
        val accountingPeriod = models.obligationsandsubmissions.AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(12),
          endDate = LocalDate.now(),
          dueDate = pastDueDate,
          underEnquiry = false,
          obligations = obligations
        )
        val obligationsAndSubmissions = models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess(
          processingDate = java.time.ZonedDateTime.now(),
          accountingPeriodDetails = Seq(accountingPeriod)
        )

        val result = controller.getDueOrOverdueReturnsStatus(obligationsAndSubmissions)

        result mustBe None
      }
    }

    "return Received when multiple submissions exist and most recent for both obligations is within 60 days" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[HomepageController]

        val pastDueDate          = LocalDate.now().minusDays(7)
        val oldSubmissionDate    = java.time.ZonedDateTime.now().minusDays(70)
        val recentSubmissionDate = java.time.ZonedDateTime.now().minusDays(5)

        val obligations = Seq(
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.UKTR,
            status = ObligationStatus.Fulfilled,
            canAmend = false,
            submissions = Seq(
              models.obligationsandsubmissions.Submission(
                submissionType = models.obligationsandsubmissions.SubmissionType.UKTR_CREATE,
                receivedDate = oldSubmissionDate,
                country = None
              )
            )
          ),
          models.obligationsandsubmissions.Obligation(
            obligationType = models.obligationsandsubmissions.ObligationType.GIR,
            status = ObligationStatus.Fulfilled,
            canAmend = false,
            submissions = Seq(
              models.obligationsandsubmissions.Submission(
                submissionType = models.obligationsandsubmissions.SubmissionType.GIR,
                receivedDate = recentSubmissionDate,
                country = None
              )
            )
          )
        )
        val accountingPeriod = models.obligationsandsubmissions.AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(12),
          endDate = LocalDate.now(),
          dueDate = pastDueDate,
          underEnquiry = false,
          obligations = obligations
        )
        val obligationsAndSubmissions = models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess(
          processingDate = java.time.ZonedDateTime.now(),
          accountingPeriodDetails = Seq(accountingPeriod)
        )

        val result = controller.getDueOrOverdueReturnsStatus(obligationsAndSubmissions)

        result mustBe Some(Received)
      }
    }

  }

  "getOutstandingPaymentsStatus" should {

    val amountOutstanding = 100

    val notYetDueFinancialTransaction = OutstandingCharge.UktrMainOutstandingCharge(
      AccountingPeriod(LocalDate.now().minusMonths(12), LocalDate.now()),
      EtmpSubtransactionRef.Dtt,
      outstandingAmount = BigDecimal(amountOutstanding),
      chargeItems = OutstandingCharge.FinancialItems(
        earliestDueDate = futureDueDate,
        Seq(FinancialItem(dueDate = Some(pastDueDate), clearingDate = None))
      )
    )

    "return Outstanding when there is an outstanding payment that has exceeded the due date to be made" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller         = application.injector.instanceOf[HomepageController]
        val pastDueDate        = LocalDate.now.minusDays(7)
        val pastDueTransaction =
          notYetDueFinancialTransaction.copy(chargeItems = notYetDueFinancialTransaction.chargeItems.copy(earliestDueDate = pastDueDate))

        val financialData = FinancialData(Seq(pastDueTransaction))

        val result = controller.getPaymentBannerScenario(financialData)

        result mustBe Some(Outstanding)
      }
    }

    "return Outstanding when there is an outstanding payment that has not yet reached its due date" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller    = application.injector.instanceOf[HomepageController]
        val financialData = FinancialData(Seq(notYetDueFinancialTransaction))

        val result = controller.getPaymentBannerScenario(financialData)

        result mustBe Some(Outstanding)
      }
    }

    "return None when there are no outstanding charges or recent payments" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[HomepageController]

        val oldPayment = Payment(
          Payment.FinancialItems(Seq(FinancialItem(dueDate = None, clearingDate = Some(LocalDate.now().minusDays(100)))))
        )
        val financialData = FinancialData(Seq(oldPayment))

        val result = controller.getPaymentBannerScenario(financialData)

        result mustBe None
      }
    }

    "return Paid when there is a payment made in the last 60 days and no outstanding payment on any transaction" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[HomepageController]

        val paymentFinancialTransaction = Payment(
          Payment.FinancialItems(
            Seq(
              FinancialItem(
                dueDate = Some(LocalDate.now.minusDays(26)),
                clearingDate = Some(LocalDate.now.minusDays(25))
              )
            )
          )
        )
        val financialData = FinancialData(Seq(paymentFinancialTransaction))

        val result = controller.getPaymentBannerScenario(financialData)

        result mustBe Some(Paid)
      }
    }
  }

  "determineNotificationArea" should {

    val application = applicationBuilder().build()
    val controller  = application.injector.instanceOf[HomepageController]

    val anyReturnStatus  = Gen.option(Gen.oneOf(DueAndOverdueReturnBannerScenario.values))
    val anyAccountStatus = Gen.oneOf(AccountStatus.values)

    val commonChargeFields = (
      AccountingPeriod(startDate = LocalDate.now().minusYears(1), endDate = LocalDate.now()),
      EtmpSubtransactionRef.Dtt,
      BigDecimal(12345.67),
      OutstandingCharge.FinancialItems(
        earliestDueDate = LocalDate.now().minusDays(1),
        items = Seq(FinancialItem(dueDate = Some(LocalDate.now().minusDays(1)), clearingDate = None))
      )
    )

    val outstandingUktrCharge         = (OutstandingCharge.UktrMainOutstandingCharge.apply _).tupled(commonChargeFields)
    val uktrLatePaymentInterestCharge = (OutstandingCharge.LatePaymentInterestOutstandingCharge.apply _).tupled(commonChargeFields)
    val uktrRepaymentInterestCharge   = (OutstandingCharge.LatePaymentInterestOutstandingCharge.apply _).tupled(commonChargeFields)

    "choose to show an 'outstanding payments w/ BTN' notification" when {
      "there's a regular, non-interest outstanding change and a BTN has been submitted" in forAll(anyReturnStatus) { returnStatus =>
        val financialData = FinancialData(Seq(outstandingUktrCharge))
        val result        = controller.determineNotificationArea(returnStatus, financialData, InactiveAccount)
        result mustBe DynamicNotificationAreaState.OutstandingPaymentsWithBtn(financialData.calculateOutstandingAmount)
      }

      "there's a interest charge and a BTN has been submitted" in forAll(
        Gen.oneOf(uktrLatePaymentInterestCharge, uktrRepaymentInterestCharge),
        anyReturnStatus
      ) { (interestCharge, returnStatus) =>
        val financialData = FinancialData(Seq(outstandingUktrCharge, interestCharge))
        val result        = controller.determineNotificationArea(returnStatus, financialData, InactiveAccount)
        result mustBe DynamicNotificationAreaState.OutstandingPaymentsWithBtn(financialData.calculateOutstandingAmount)
      }
    }

    "choose to show an 'accruing interest' notification" when {
      "there's a payment for interest outstanding and there is no submitted BTN" in forAll(
        Gen.oneOf(uktrLatePaymentInterestCharge, uktrRepaymentInterestCharge),
        anyReturnStatus
      ) { (interestCharge, returnStatus) =>
        val financialData = FinancialData(Seq(interestCharge, outstandingUktrCharge))
        val result        = controller.determineNotificationArea(returnStatus, financialData, ActiveAccount)
        result mustBe DynamicNotificationAreaState.AccruingInterest(financialData.calculateOutstandingAmount)
      }
    }

    "choose to show an 'outstanding payments' notification" when {
      "outstanding charges are past their due date, but there is no interest charge and there is no submitted BTN" in forAll(anyReturnStatus) {
        returnStatus =>
          val financialData = FinancialData(
            Seq(
              outstandingUktrCharge.copy(chargeItems = outstandingUktrCharge.chargeItems.copy(earliestDueDate = LocalDate.now().minusDays(7)))
            )
          )
          val result = controller.determineNotificationArea(returnStatus, financialData, ActiveAccount)
          result mustBe DynamicNotificationAreaState.OutstandingPayments(financialData.calculateOutstandingAmount)
      }

      "outstanding charges have not yet reached their due date, and there is no interest charge" in forAll(anyReturnStatus, anyAccountStatus) {
        (returnStatus, accountStatus) =>
          val financialData = FinancialData(
            Seq(
              outstandingUktrCharge.copy(chargeItems = outstandingUktrCharge.chargeItems.copy(earliestDueDate = LocalDate.now().plusDays(7)))
            )
          )
          val result = controller.determineNotificationArea(returnStatus, financialData, accountStatus)
          result mustBe DynamicNotificationAreaState.OutstandingPayments(financialData.calculateOutstandingAmount)
      }
    }

    val recentPayment = Payment(
      Payment.FinancialItems(
        Seq(FinancialItem(dueDate = None, clearingDate = Some(LocalDate.now().minusDays(14))))
      )
    )

    val nonImpactingFinancialData = Gen.oneOf(Seq(recentPayment), Seq.empty).map(FinancialData.apply)

    "choose to show a 'return expected' notification" when {

      val returnExpectedNotificationMappings = Table(
        "Return status"                              -> "Notification state",
        DueAndOverdueReturnBannerScenario.Due        -> DynamicNotificationAreaState.ReturnExpectedNotification.Due,
        DueAndOverdueReturnBannerScenario.Overdue    -> DynamicNotificationAreaState.ReturnExpectedNotification.Overdue,
        DueAndOverdueReturnBannerScenario.Incomplete -> DynamicNotificationAreaState.ReturnExpectedNotification.Incomplete
      )

      "there is no outstanding payment and a return is expected" in forAll(returnExpectedNotificationMappings) { (returnStatus, notificationState) =>
        forAll(nonImpactingFinancialData, anyAccountStatus) { (financialData, accountStatus) =>
          val result = controller.determineNotificationArea(Some(returnStatus), financialData, accountStatus)
          result mustBe notificationState
        }
      }
    }

    "choose to avoid displaying a notification" when {

      "there is no outstanding payment and a return is not expected" in forAll(
        Gen.option(DueAndOverdueReturnBannerScenario.Received),
        nonImpactingFinancialData,
        anyAccountStatus
      ) { case (uktr, financialData, accountStatus) =>
        val result = controller.determineNotificationArea(uktr, financialData, accountStatus)
        result mustBe DynamicNotificationAreaState.NoNotification
      }
    }
  }

  "determineBtnBanner" should {
    val application     = applicationBuilder().build()
    val controller      = application.injector.instanceOf[HomepageController]
    val nonBtnDnaStates = Gen.oneOf(
      Gen.const(DynamicNotificationAreaState.AccruingInterest(100)),
      Gen.const(DynamicNotificationAreaState.OutstandingPayments(100)),
      Gen.const(DynamicNotificationAreaState.NoNotification),
      Gen.oneOf(DynamicNotificationAreaState.ReturnExpectedNotification.values)
    )
    val btnDnaState = DynamicNotificationAreaState.OutstandingPaymentsWithBtn(100)

    "hide the banner when the DNA already includes a message about your BTN" in {
      controller.determineBtnBanner(InactiveAccount, btnDnaState) mustBe BtnBanner.Hide
    }

    "show the banner when the account is inactive" in forAll(nonBtnDnaStates) { dnaState =>
      controller.determineBtnBanner(InactiveAccount, dnaState) mustBe BtnBanner.Show
    }

    "hide the banner when the account is active" in forAll(
      Gen.oneOf(nonBtnDnaStates, Gen.const(btnDnaState))
    ) { anyDnaState =>
      controller.determineBtnBanner(ActiveAccount, anyDnaState) mustBe BtnBanner.Hide
    }
  }

}
