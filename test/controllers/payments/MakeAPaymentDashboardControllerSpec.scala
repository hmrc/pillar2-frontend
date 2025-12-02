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

package controllers.payments

import base.SpecBase
import connectors.OPSConnector
import controllers.actions.TestAuthRetrievals.Ops
import models.UserAnswers
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import pages.PlrReferencePage
import play.api.inject
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.http.{GatewayTimeoutException, HeaderCarrier}
import views.html.{LegacyMakeAPaymentDashboardView, MakeAPaymentDashboardView}

import java.util.UUID
import scala.concurrent.Future

class MakeAPaymentDashboardControllerSpec extends SpecBase {

  val id:           String = UUID.randomUUID().toString
  val groupId:      String = UUID.randomUUID().toString
  val providerId:   String = UUID.randomUUID().toString
  val providerType: String = UUID.randomUUID().toString

  "Payment Dashboard Controller" should {
    "redirect to OPS when OPS call succeeds" in {
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
      val mockOpsConnector = mock[OPSConnector]
      val application      = applicationBuilder(userAnswers = None, enrolments)
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          inject.bind[OPSConnector].toInstance(mockOpsConnector)
        )
        .build()
      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(UserAnswers("id"))))
        when(mockOpsConnector.getRedirectLocation(eqTo("12345678"))(any[HeaderCarrier])) thenReturn Future.successful(
          "http://localhost:9900/pay-api/pay"
        )
        val request =
          FakeRequest(GET, controllers.payments.routes.MakeAPaymentDashboardController.onRedirect().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "http://localhost:9900/pay-api/pay"
      }
    }

    "redirect to error page when OPS call fails" in {
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
      val mockOpsConnector = mock[OPSConnector]
      val application      = applicationBuilder(userAnswers = None, enrolments)
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          inject.bind[OPSConnector].toInstance(mockOpsConnector)
        )
        .build()
      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(UserAnswers("id"))))
        when(mockOpsConnector.getRedirectLocation(eqTo("12345678"))(any[HeaderCarrier])) thenReturn Future.failed(
          new GatewayTimeoutException("Call to OPS timed out")
        )
        val request =
          FakeRequest(GET, controllers.payments.routes.MakeAPaymentDashboardController.onRedirect().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "return OK and the correct view for a GET with pillar 2 reference retrieved from enrolment" in {
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
      val application = applicationBuilder(userAnswers = None, enrolments)
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()
      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(UserAnswers("id"))))
        val request =
          FakeRequest(GET, controllers.payments.routes.MakeAPaymentDashboardController.onPageLoad().url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[MakeAPaymentDashboardView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view("12345678")(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }

    }

    "return OK and the legecy view for a GET when feature flag is disabled" in {
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
      val application = applicationBuilder(userAnswers = None, enrolments)
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .configure("features.enablePayByBankAccount" -> false)
        .build()
      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(UserAnswers("id"))))
        val request =
          FakeRequest(GET, controllers.payments.routes.MakeAPaymentDashboardController.onPageLoad().url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[LegacyMakeAPaymentDashboardView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view("12345678")(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }

    }

    "return OK and the correct view for a GET for Agents" in {
      val application = applicationBuilder(userAnswers = None, enrolments = pillar2AgentEnrolmentWithDelegatedAuth.enrolments)
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          bind[AuthConnector].toInstance(mockAuthConnector)
        )
        .build()

      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(UserAnswers("id"))))
        when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
          .thenReturn(
            Future.successful(
              Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
            )
          )
        val request = FakeRequest(GET, controllers.payments.routes.MakeAPaymentDashboardController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[MakeAPaymentDashboardView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(PlrReference)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }

    }

    "return OK and the correct view for a GET pillar 2 reference retrieved from the database" in {
      val sessionUserAnswers = UserAnswers("id").setOrException(PlrReferencePage, "12345678")
      val application        = applicationBuilder(userAnswers = Some(sessionUserAnswers))
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionUserAnswers)))

      running(application) {
        val request = FakeRequest(GET, controllers.payments.routes.MakeAPaymentDashboardController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[MakeAPaymentDashboardView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view("12345678")(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "redirect to journey recovery for a GET if pillar 2 reference is missing" in {
      val application = applicationBuilder(userAnswers = None)
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

      running(application) {
        val request =
          FakeRequest(GET, controllers.payments.routes.MakeAPaymentDashboardController.onPageLoad().url)
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
