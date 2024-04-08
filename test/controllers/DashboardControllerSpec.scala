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
import connectors.{SubscriptionConnector, UserAnswersConnectors}
import generators.ModelGenerators
import models.InternalIssueError
import models.subscription.{AccountStatus, DashboardInfo, ReadSubscriptionResponse, UpeDetails}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{FmDashboardPage, SubAccountStatusPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}
import views.html.DashboardView

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

class DashboardControllerSpec extends SpecBase with ModelGenerators {

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
  val dashboardInfo = DashboardInfo(organisationName = "name", registrationDate = LocalDate.now())
  val readSubscriptionResponse =
    ReadSubscriptionResponse(UpeDetails("International Organisation Inc.", LocalDate.parse("2022-01-31")), Some(AccountStatus(true)))

  "Dashboard Controller" should {

    "return OK and the correct view for a GET" in {
      val accountStatus = AccountStatus(inactive = false)
      val userAnswers = emptyUserAnswers
        .setOrException(FmDashboardPage, dashboardInfo)
        .setOrException(SubAccountStatusPage, accountStatus)
      val application =
        applicationBuilder(userAnswers = Some(userAnswers), enrolments)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSubscriptionService.readAndCacheSubscription(any())(any())).thenReturn(Future.successful(readSubscriptionResponse))
        when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswers)))
        val result = route(application, request).value
        val view   = application.injector.instanceOf[DashboardView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          readSubscriptionResponse.upeDetails.organisationName,
          readSubscriptionResponse.upeDetails.registrationDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
          "12345678",
          inactiveStatus = true
        )(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }

    }

    "redirect to view and amend subscription recovery if no dashboard info is found" in {
      val application =
        applicationBuilder(userAnswers = None, enrolments)
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
        when(mockSubscriptionService.readAndCacheSubscription(any())(any())).thenReturn(Future.failed(InternalIssueError))
        when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad.url
      }

    }
    "redirect to error page if no valid Js value is found from read subscription api" in {
      val application =
        applicationBuilder(userAnswers = None, enrolments)
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()
      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
        when(mockSubscriptionService.readAndCacheSubscription(any())(any())).thenReturn(Future.failed(models.InternalIssueError))

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad.url

      }
    }

    "redirect to journey recovery if no pillar 2 reference is found in session repository or enrolment data" in {
      val application =
        applicationBuilder(userAnswers = None)
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
          )
          .build()
      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
        when(mockSubscriptionService.readAndCacheSubscription(any())(any())).thenReturn(Future.failed(models.InternalIssueError))

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

    }

    "redirect to error page if no userAnswer is found from the connector" in {
      val application = applicationBuilder(None)
        .overrides(
          bind[SubscriptionService].toInstance(mockSubscriptionService),
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(None))
        when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(None))
        when(mockSubscriptionService.readAndCacheSubscription(any())(any())).thenReturn(Future.successful(readSubscriptionResponse))
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }

}
