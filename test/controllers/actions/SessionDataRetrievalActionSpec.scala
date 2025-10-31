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

package controllers.actions

import base.SpecBase
import models.UserAnswers
import models.requests.{IdentifierRequest, SessionOptionalDataRequest}
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import repositories.SessionRepository

import scala.concurrent.Future

class SessionDataRetrievalActionSpec extends SpecBase {

  class Harness(sessionRepository: SessionRepository) extends SessionDataRetrievalActionImpl(sessionRepository)(ec) {
    def callTransform[A](request: IdentifierRequest[A]): Future[SessionOptionalDataRequest[A]] = transform(request)
  }

  "Session Data Retrieval Action" when {

    "there is no data in the cache" should {

      "set userAnswers to 'None' in the request" in {

        val sessionRepository = mock[SessionRepository]
        when(sessionRepository.get("id")) thenReturn Future.successful(None)
        val action = new Harness(sessionRepository)

        val result = action.callTransform(IdentifierRequest(FakeRequest(), "id", Some("groupID"), userIdForEnrolment = "userId")).futureValue

        result.userAnswers must not be defined
      }
    }

    "there is data in the cache" should {

      "build a userAnswers object and add it to the request" in {

        val sessionRepository = mock[SessionRepository]
        when(sessionRepository.get("id")) thenReturn Future.successful(Some(UserAnswers("id")))
        val action = new Harness(sessionRepository)

        val result = action.callTransform(IdentifierRequest(FakeRequest(), "id", Some("groupID"), userIdForEnrolment = "userId")).futureValue
        result.userAnswers mustBe defined
      }
    }
  }
}
