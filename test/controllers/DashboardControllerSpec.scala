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
import connectors.ReadSubscriptionConnector
import generators.ModelGenerators
import models.subscription.{AccountStatus, DashboardInfo}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{fmDashboardPage, subAccountStatusPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.ReadSubscriptionService
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
  val jsonDashboard = Json.toJson(dashboardInfo)

  "Dashboard Controller" should {

    "return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.setOrException(fmDashboardPage, dashboardInfo)
      val application =
        applicationBuilder(userAnswers = Some(userAnswers), enrolments)
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository), bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService))
          .build()
      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockReadSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(jsonDashboard))
        val result = route(application, request).value
        val view   = application.injector.instanceOf[DashboardView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          dashboardInfo.organisationName,
          dashboardInfo.registrationDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
          "12345678",
          inactiveStatus = false,
          showPaymentsSection = true
        )(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }

    }

    "redirect to journey recovery if no dashboard info is found" in {
      val userAnswers = emptyUserAnswers
        .setOrException(subAccountStatusPage, AccountStatus(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers), enrolments)
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository), bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService))
          .build()
      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        when(mockReadSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(jsonDashboard))
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

    }
    "redirect to error page if no valid Js value is found from read subscription api" in {
      val application =
        applicationBuilder(userAnswers = None, enrolments)
          .overrides(
            bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService)
          )
          .build()
      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
        when(mockReadSubscriptionService.readSubscription(any())(any())).thenReturn(Future.failed(models.InternalIssueError))

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad.url

      }
    }

    "redirect to Journey Recovery if no pillar 2 reference is found in session repository or enrolment data" in {
      val application =
        applicationBuilder(userAnswers = None)
          .overrides(
            bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService),
            bind[ReadSubscriptionConnector].toInstance(mockReadSubscriptionConnector)
          )
          .build()
      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
        when(mockReadSubscriptionService.readSubscription(any())(any())).thenReturn(Future.failed(models.InternalIssueError))

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

    }

    "redirect to journey recovery if read subscription has happened successfully but no dashboard info is found" in {
      val ua = emptyUserAnswers.setOrException(subAccountStatusPage, AccountStatus(true))
      val application = applicationBuilder(userAnswers = Some(ua), enrolments)
        .overrides(
          bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService),
          bind[ReadSubscriptionConnector].toInstance(mockReadSubscriptionConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
        when(mockReadSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(jsonDashboard))

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }

}
