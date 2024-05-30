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
import controllers.actions.{AgentIdentifierAction, FakeIdentifierAction}
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.{Configuration, inject}
import play.api.inject.bind
import play.api.mvc.PlayBodyParsers
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.payment.RequestRefundBeforeStartView

import scala.concurrent.Future

class RequestRefundBeforeStartControllerSpec extends SpecBase {

  "Rfm Save Progress inform Controller" when {

    "return OK and the correct view for a GET" in {
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
        val request = FakeRequest(GET, controllers.repayments.routes.RequestRefundBeforeStartController.onPageLoad(Some(PlrReference)).url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[RequestRefundBeforeStartView]

        status(result) mustEqual OK
        contentAsString(result) must include("Request a refund")
        contentAsString(result) mustEqual view(PlrReference, Some(PlrReference))(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must redirect to Under Construction page if requestRefundEnabled is disabled" in {
      val ua = emptyUserAnswers
      val application = applicationBuilder(userAnswers = None, pillar2AgentEnrolmentWithDelegatedAuth.enrolments)
        .configure(
          Seq(
            "features.requestRefundEnabled" -> false
          ): _*
        )
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction)
        )
        .build()
      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      running(application) {
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(UserAnswers("id"))))
        val request = FakeRequest(GET, controllers.repayments.routes.RequestRefundBeforeStartController.onPageLoad(Some(PlrReference)).url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.UnderConstructionController.onPageLoad.url)
      }
    }
  }
}
