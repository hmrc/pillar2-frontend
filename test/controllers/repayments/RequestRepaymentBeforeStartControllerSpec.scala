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

package controllers.repayments

import base.SpecBase
import controllers.actions.TestAuthRetrievals.~
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.auth.core.{AuthConnector, User}
import views.html.repayments.RequestRefundBeforeStartView

import java.util.UUID
import scala.concurrent.Future

class RequestRepaymentBeforeStartControllerSpec extends SpecBase {

  val id:           String = UUID.randomUUID().toString
  val providerId:   String = UUID.randomUUID().toString
  val providerType: String = UUID.randomUUID().toString

  "Rfm Save Progress inform Controller" when {

    "return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = None, pillar2AgentEnrolmentWithDelegatedAuth.enrolments)
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          bind[AuthConnector].toInstance(mockAuthConnector)
        )
        .build()
      when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(using any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(UserAnswers("id"))))

      running(application) {
        val request = FakeRequest(GET, controllers.repayments.routes.RequestRepaymentBeforeStartController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[RequestRefundBeforeStartView]
        status(result) mustEqual OK
        contentAsString(result) must include("Request a repayment")
        contentAsString(result) mustEqual view(agentView = false)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

  }
}
