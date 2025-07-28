/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.actions

import base.SpecBase
import models.requests.IdentifierRequest
import org.mockito.Mockito.when
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._

class AgentAccessFilterActionSpec extends SpecBase {

  "AgentAccessFilterAction" must {
    "redirect to unauthorised page if user is agent and agent journey is disabled" in {
      when(mockFrontendAppConfig.asaAccessEnabled).thenReturn(false)

      val filterAction      = new AgentAccessFilterAction(mockFrontendAppConfig)
      val identifierRequest = new IdentifierRequest[AnyContent](FakeRequest(), "userId", enrolments = Set.empty, isAgent = true, userIdForEnrolment = "userId")
      val futureResult      = filterAction.filter(identifierRequest)

      status(futureResult.map(_.get)) mustBe SEE_OTHER

      redirectLocation(futureResult.map(_.get)) mustBe Some(controllers.routes.UnauthorisedController.onPageLoad.url)
    }

    "allow the request if user is agent and agent journey is enabled" in {
      when(mockFrontendAppConfig.asaAccessEnabled).thenReturn(true)

      val filterAction      = new AgentAccessFilterAction(mockFrontendAppConfig)
      val identifierRequest = new IdentifierRequest[AnyContent](FakeRequest(), "userId", enrolments = Set.empty, isAgent = true, userIdForEnrolment = "userId")
      val futureResult      = filterAction.filter(identifierRequest).futureValue

      futureResult.map(_ mustBe None)
    }

    "allow the request if user is not an agent regardless of agent journey being enabled" in {
      when(mockFrontendAppConfig.asaAccessEnabled).thenReturn(true)

      val filterAction      = new AgentAccessFilterAction(mockFrontendAppConfig)
      val identifierRequest = new IdentifierRequest[AnyContent](FakeRequest(), "userId", enrolments = Set.empty, isAgent = false, userIdForEnrolment = "userId")
      val futureResult      = filterAction.filter(identifierRequest).futureValue

      futureResult.map(_ mustBe None)
    }

    "allow the request if user is not an agent regardless of agent journey being disabled" in {
      when(mockFrontendAppConfig.asaAccessEnabled).thenReturn(false)

      val filterAction      = new AgentAccessFilterAction(mockFrontendAppConfig)
      val identifierRequest = new IdentifierRequest[AnyContent](FakeRequest(), "userId", enrolments = Set.empty, isAgent = false, userIdForEnrolment = "userId")
      val futureResult      = filterAction.filter(identifierRequest).futureValue

      futureResult.map(_ mustBe None)
    }
  }

}
