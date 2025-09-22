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
import controllers.actions.TestAuthRetrievals.Ops
import controllers.payments.OutstandingPaymentsControllerSpec.samplePaymentsDataWithNoTag
import generators.ModelGenerators
import helpers.FinancialDataHelper.Pillar2UktrName
import models._
import models.obligationsandsubmissions.ObligationStatus
import models.subscription._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{ObligationsAndSubmissionsService, OutstandingPaymentsService, SubscriptionService}
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.{DashboardView, HomepageView}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import scala.concurrent.Future

class DashboardControllerSpec extends SpecBase with ModelGenerators {

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

    "return OK and the correct view for a GET" when {
      "newHomepageEnabled is true" in {
        val application =
          applicationBuilder(userAnswers = None, enrolments)
            .configure("features.newHomepageEnabled" -> true)
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[SubscriptionService].toInstance(mockSubscriptionService),
              bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService),
              bind[OutstandingPaymentsService].toInstance(mockOutstandingPaymentsService),
              bind[FinancialDataConnector].toInstance(mockFinancialDataConnector)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
          when(mockSessionRepository.get(any()))
            .thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockSessionRepository.set(any()))
            .thenReturn(Future.successful(true))
          when(mockSubscriptionService.maybeReadSubscription(any())(any())).thenReturn(Future.successful(Some(subscriptionData)))
          when(mockSubscriptionService.cacheSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
          when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(any[HeaderCarrier]))
            .thenReturn(Future.successful(obligationsAndSubmissionsSuccessResponse(ObligationStatus.Fulfilled)))
          when(mockOutstandingPaymentsService.retrieveData(any(), any(), any())(any[HeaderCarrier]))
            .thenReturn(Future.successful(samplePaymentsDataWithNoTag))

          val result = route(application, request).value
          val view   = application.injector.instanceOf[HomepageView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            subscriptionData.upeDetails.organisationName,
            subscriptionData.upeDetails.registrationDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
            btnActive = false,
            None,
            None,
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

      "newHomepageEnabled is false" in {
        val application =
          applicationBuilder(userAnswers = None, enrolments)
            .configure("features.newHomepageEnabled" -> false)
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[SubscriptionService].toInstance(mockSubscriptionService)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
          when(mockSessionRepository.get(any()))
            .thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockSessionRepository.set(any()))
            .thenReturn(Future.successful(true))
          when(mockSubscriptionService.maybeReadSubscription(any())(any())).thenReturn(Future.successful(Some(subscriptionData)))
          when(mockSubscriptionService.cacheSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
          val result = route(application, request).value
          val view   = application.injector.instanceOf[DashboardView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            subscriptionData.upeDetails.organisationName,
            subscriptionData.upeDetails.registrationDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
            "12345678",
            inactiveStatus = false,
            agentView = false
          )(
            request,
            applicationConfig,
            messages(application)
          ).toString
        }
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
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSubscriptionService.maybeReadSubscription(any())(any())).thenReturn(Future.failed(models.UnprocessableEntityError))
        when(mockSubscriptionService.cacheSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
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
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
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
      when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )
      when(mockSubscriptionService.maybeReadSubscription(any())(any())).thenReturn(Future.failed(models.UnprocessableEntityError))
      when(mockSubscriptionService.cacheSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(UserAnswers("id"))))

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
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
        val controller = application.injector.instanceOf[DashboardController]

        val futureDueDate = LocalDate.now().plusDays(7)
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
        val controller = application.injector.instanceOf[DashboardController]

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
        val controller = application.injector.instanceOf[DashboardController]

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
        val controller = application.injector.instanceOf[DashboardController]

        val futureDueDate = LocalDate.now().plusDays(7)
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
        val controller = application.injector.instanceOf[DashboardController]

        val futureDueDate = LocalDate.now().plusDays(7)
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
        val controller = application.injector.instanceOf[DashboardController]

        val futureDueDate = LocalDate.now().plusDays(7)
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
        val controller = application.injector.instanceOf[DashboardController]

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
        val controller = application.injector.instanceOf[DashboardController]

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
        val controller = application.injector.instanceOf[DashboardController]

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
        val controller = application.injector.instanceOf[DashboardController]

        val futureDueDate = LocalDate.now().plusDays(7)
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
        val controller = application.injector.instanceOf[DashboardController]

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
        val controller = application.injector.instanceOf[DashboardController]

        val futureDueDate = LocalDate.now().plusDays(7)
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
        val controller = application.injector.instanceOf[DashboardController]

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
        val controller = application.injector.instanceOf[DashboardController]

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
        val controller = application.injector.instanceOf[DashboardController]

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
        val controller = application.injector.instanceOf[DashboardController]

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
        val controller = application.injector.instanceOf[DashboardController]

        val pastDueDate          = LocalDate.now().minusDays(7)
        val recentSubmissionDate = java.time.ZonedDateTime.now().minusDays(30)
        val obligations = Seq(
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
        val controller = application.injector.instanceOf[DashboardController]

        val recentSubmissionDate = java.time.ZonedDateTime.now().minusDays(70)
        val obligations = Seq(
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
        val controller = application.injector.instanceOf[DashboardController]

        val recentSubmissionDate = java.time.ZonedDateTime.now().minusDays(30)
        val obligations = Seq(
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
        val controller = application.injector.instanceOf[DashboardController]

        val recentSubmissionDate = java.time.ZonedDateTime.now().minusDays(30)
        val obligations = Seq(
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
        val controller = application.injector.instanceOf[DashboardController]

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

    "return Outstanding when there is an outstanding payment that has exceeded the due date to be made" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[DashboardController]

        val pastDueDate = LocalDate.now.minusDays(7)
        val transactions = Seq(
          TransactionSummary(
            name = Pillar2UktrName,
            outstandingAmount = 100,
            dueDate = pastDueDate
          )
        )
        val financialSummary = FinancialSummary(
          accountingPeriod = AccountingPeriod(LocalDate.now.minusMonths(12), LocalDate.now),
          transactions = transactions
        )

        val result = controller.getOutstandingPaymentsStatus(Some(Seq(financialSummary)))

        result mustBe Some(Outstanding)
      }
    }

    "return None when there is an outstanding payment that has not exceeded the due date to be made" in {
      val application = applicationBuilder(userAnswers = None, enrolments).build()
      running(application) {
        val controller = application.injector.instanceOf[DashboardController]

        val futureDueDate = LocalDate.now.plusDays(7)
        val transactions = Seq(
          TransactionSummary(
            name = Pillar2UktrName,
            outstandingAmount = 100,
            dueDate = futureDueDate
          )
        )
        val financialSummary = FinancialSummary(
          accountingPeriod = AccountingPeriod(LocalDate.now.minusMonths(12), LocalDate.now),
          transactions = transactions
        )

        val result = controller.getOutstandingPaymentsStatus(Some(Seq(financialSummary)))

        result mustBe None
      }
    }
  }

}
