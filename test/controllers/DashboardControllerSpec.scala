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
import controllers.actions.TestAuthRetrievals.Ops
import generators.ModelGenerators
import models.UserAnswers
import models.obligationsandsubmissions._
import models.subscription._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{ObligationsAndSubmissionsService, SubscriptionService}
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.{DashboardView, HomepageView}

import java.time.{LocalDate, ZonedDateTime}
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
              bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
          when(mockSessionRepository.get(any()))
            .thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockSessionRepository.set(any()))
            .thenReturn(Future.successful(true))
          when(mockSubscriptionService.readAndCacheSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
          when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(any[HeaderCarrier]))
            .thenReturn(Future.successful(obligationsAndSubmissionsSuccessResponse(ObligationStatus.Fulfilled)))

          val result = route(application, request).value
          val view   = application.injector.instanceOf[HomepageView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            subscriptionData.upeDetails.organisationName,
            subscriptionData.upeDetails.registrationDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
            None,
            None,
            "12345678",
            isAgent = false
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
          when(mockSubscriptionService.readAndCacheSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
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
        when(mockSubscriptionService.readAndCacheSubscription(any())(any())).thenReturn(Future.failed(models.InternalIssueError))
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
      when(mockSubscriptionService.readAndCacheSubscription(any())(any())).thenReturn(Future.failed(models.InternalIssueError))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

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
      when(mockSubscriptionService.readAndCacheSubscription(any())(any())).thenReturn(Future.failed(models.InternalIssueError))
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

  "UKTR Banner Logic" should {
    "return None when no open UKTR obligations exist" in {
      val application = applicationBuilder(userAnswers = None, enrolments)
        .configure("features.newHomepageEnabled" -> true)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[SubscriptionService].toInstance(mockSubscriptionService),
          bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService)
        )
        .build()

      running(application) {
        val controller = application.injector.instanceOf[DashboardController]
        val response   = obligationsAndSubmissionsSuccessResponse(ObligationStatus.Fulfilled)

        val result = controller.uktrBannerScenario(response)
        result mustBe None
      }
    }

    "return UktrIncomplete for obligations with submissions" in {
      val application = applicationBuilder(userAnswers = None, enrolments)
        .configure("features.newHomepageEnabled" -> true)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[SubscriptionService].toInstance(mockSubscriptionService),
          bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService)
        )
        .build()

      running(application) {
        val controller = application.injector.instanceOf[DashboardController]
        val responseWithSubmissions = ObligationsAndSubmissionsSuccess(
          processingDate = ZonedDateTime.now(),
          accountingPeriodDetails = Seq(
            AccountingPeriodDetails(
              startDate = LocalDate.now(),
              endDate = LocalDate.now().plusMonths(12),
              dueDate = LocalDate.now().plusDays(30),
              underEnquiry = false,
              obligations = Seq(
                Obligation(
                  obligationType = ObligationType.UKTR,
                  status = ObligationStatus.Open,
                  canAmend = false,
                  submissions = Seq(Submission(SubmissionType.UKTR_CREATE, ZonedDateTime.now(), None))
                )
              )
            )
          )
        )

        val result = controller.uktrBannerScenario(responseWithSubmissions)
        result.map(_.toString) mustBe Some("UktrIncomplete")
      }
    }

    "return UktrOverdue for overdue obligations" in {
      val application = applicationBuilder(userAnswers = None, enrolments)
        .configure("features.newHomepageEnabled" -> true)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[SubscriptionService].toInstance(mockSubscriptionService),
          bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService)
        )
        .build()

      running(application) {
        val controller = application.injector.instanceOf[DashboardController]
        val responseOverdue = ObligationsAndSubmissionsSuccess(
          processingDate = ZonedDateTime.now(),
          accountingPeriodDetails = Seq(
            AccountingPeriodDetails(
              startDate = LocalDate.now().minusMonths(12),
              endDate = LocalDate.now(),
              dueDate = LocalDate.now().minusDays(1), // Overdue
              underEnquiry = false,
              obligations = Seq(
                Obligation(
                  obligationType = ObligationType.UKTR,
                  status = ObligationStatus.Open,
                  canAmend = false,
                  submissions = Seq.empty
                )
              )
            )
          )
        )

        val result = controller.uktrBannerScenario(responseOverdue)
        result.map(_.toString) mustBe Some("UktrOverdue")
      }
    }

    "return UktrDue for obligations due within 60 days" in {
      val application = applicationBuilder(userAnswers = None, enrolments)
        .configure("features.newHomepageEnabled" -> true)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[SubscriptionService].toInstance(mockSubscriptionService),
          bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService)
        )
        .build()

      running(application) {
        val controller = application.injector.instanceOf[DashboardController]
        val responseDue = ObligationsAndSubmissionsSuccess(
          processingDate = ZonedDateTime.now(),
          accountingPeriodDetails = Seq(
            AccountingPeriodDetails(
              startDate = LocalDate.now(),
              endDate = LocalDate.now().plusMonths(12),
              dueDate = LocalDate.now().plusDays(30), // Due within 60 days
              underEnquiry = false,
              obligations = Seq(
                Obligation(
                  obligationType = ObligationType.UKTR,
                  status = ObligationStatus.Open,
                  canAmend = false,
                  submissions = Seq.empty
                )
              )
            )
          )
        )

        val result = controller.uktrBannerScenario(responseDue)
        result.map(_.toString) mustBe Some("UktrDue")
      }
    }

    "return None for obligations due beyond 60 days" in {
      val application = applicationBuilder(userAnswers = None, enrolments)
        .configure("features.newHomepageEnabled" -> true)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[SubscriptionService].toInstance(mockSubscriptionService),
          bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService)
        )
        .build()

      running(application) {
        val controller = application.injector.instanceOf[DashboardController]
        val responseFuture = ObligationsAndSubmissionsSuccess(
          processingDate = ZonedDateTime.now(),
          accountingPeriodDetails = Seq(
            AccountingPeriodDetails(
              startDate = LocalDate.now(),
              endDate = LocalDate.now().plusMonths(12),
              dueDate = LocalDate.now().plusDays(70), // Beyond 60 days
              underEnquiry = false,
              obligations = Seq(
                Obligation(
                  obligationType = ObligationType.UKTR,
                  status = ObligationStatus.Open,
                  canAmend = false,
                  submissions = Seq.empty
                )
              )
            )
          )
        )

        val result = controller.uktrBannerScenario(responseFuture)
        result mustBe None
      }
    }
  }

}
