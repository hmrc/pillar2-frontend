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

package controllers.rfm

import base.SpecBase
import controllers.actions.TestAuthRetrievals.Ops
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.Credentials
import utils.ViewHelpers
import views.html.rfm.RfmConfirmationView

import java.util.UUID
import scala.concurrent.Future

class RfmConfirmationControllerSpec extends SpecBase {
  val id:           String = UUID.randomUUID().toString
  val groupId:      String = UUID.randomUUID().toString
  val providerId:   String = UUID.randomUUID().toString
  val providerType: String = UUID.randomUUID().toString

  "RfmConfirmation Controller" when {
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
    "must return OK and the correct view with content" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), enrolments)
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmConfirmationController.onPageLoad.url)
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))
        val result      = route(application, request).value
        val currentDate = HtmlFormat.escape(ViewHelpers.getDateTimeGMT)
        val view        = application.injector.instanceOf[RfmConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view("12345678", currentDate.toString())(
          request,
          applicationConfig,
          messages(application)
        ).toString

        contentAsString(result) must include(
          "Replace filing member successful"
        )
        contentAsString(result) must include(
          "Your group’s filing member was replaced on"
        )
        contentAsString(result) must include(
          "As the new filing member, you have taken over the obligations to:"
        )
        contentAsString(result) must include(
          "act as HMRC’s primary contact in relation to the group’s Pillar 2 Top-up Taxes compliance"
        )
        contentAsString(result) must include(
          "submit your group’s Pillar 2 Top-up Taxes returns"
        )
        contentAsString(result) must include(
          "ensure your group’s Pillar 2 Top-up Taxes account accurately reflects their records."
        )
        contentAsString(result) must include(
          "If you fail to meet your obligations as a filing member, you may be liable for penalties."
        )
        contentAsString(result) must include(
          "What happens next"
        )
        contentAsString(result) must include(
          "You can now"
        )
        contentAsString(result) must include(
          "report and manage Pillar 2 Top-up Taxes"
        )
        contentAsString(result) must include(
          "on behalf of your group."
        )
        contentAsString(result) must include("Print this page")
        contentAsString(result) must include(
          currentDate.toString()
        )

      }
    }

    "redirect to journey recover if no pillar 2 reference or data found in session repository" in {
      val application = applicationBuilder(userAnswers = None, pillar2AgentEnrolmentWithDelegatedAuth.enrolments)
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          bind[AuthConnector].toInstance(mockAuthConnector)
        )
        .build()

      running(application) {
        when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
          .thenReturn(
            Future.successful(
              Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
            )
          )
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
        val request = FakeRequest(GET, controllers.rfm.routes.RfmConfirmationController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
