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
import controllers.actions.{AgentIdentifierAction, FakeIdentifierAction}
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.PlrReferencePage
import play.api.inject
import play.api.inject.bind
import play.api.mvc.PlayBodyParsers
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}
import views.html.MakeAPaymentDashboardView

import scala.concurrent.Future

class MakeAPaymentDashboardControllerSpec extends SpecBase {

  "Payment Dashboard Controller" should {
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
          FakeRequest(GET, controllers.routes.MakeAPaymentDashboardController.onPageLoad().url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[MakeAPaymentDashboardView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view("12345678", None)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }

    }

    "return OK and the correct view for a GET for Agents" in {
      val application = applicationBuilder(userAnswers = None, pillar2AgentEnrolmentWithDelegatedAuth.enrolments)
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction)
        )
        .build()
      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      running(application) {
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(UserAnswers("id"))))

        val request =
          FakeRequest(GET, controllers.routes.MakeAPaymentDashboardController.onPageLoad(Some(PlrReference)).url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[MakeAPaymentDashboardView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(PlrReference, Some(PlrReference))(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }

    }

    "return OK and the correct view for a GET pillar 2 reference retrieved from the database" in {
      val sessionUserAnswers = UserAnswers("id").setOrException(PlrReferencePage, "12345678")
      val application = applicationBuilder(userAnswers = Some(sessionUserAnswers))
        .build()
      running(application) {

        val request =
          FakeRequest(GET, controllers.routes.MakeAPaymentDashboardController.onPageLoad().url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[MakeAPaymentDashboardView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view("12345678", None)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }

    }

    "redirect to journey recovery for a GET if pillar 2 reference is missing" in {
      val application = applicationBuilder(userAnswers = None, Set.empty)
        .build()
      running(application) {

        val request =
          FakeRequest(GET, controllers.routes.MakeAPaymentDashboardController.onPageLoad().url)

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

      }
    }
  }

}
