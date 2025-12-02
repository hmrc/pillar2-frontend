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

package services

import mapping.SubmissionAnswerLookup
import mapping.SubmissionAnswerLookup.Instances.forBtn
import models.UserAnswers
import models.longrunningsubmissions.LongRunningSubmission.BTN
import models.longrunningsubmissions.SubmissionLookupError.UserAnswersNotFound
import models.requests.UserIdRequest
import org.mockito.Mockito.when
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LongRunningSubmissionServiceSpec extends AnyWordSpec with must.Matchers with MockitoSugar with ScalaFutures with EitherValues {

  implicit val request: UserIdRequest[?] = {
    val instance = mock[UserIdRequest[Unit]]
    when(instance.userId).thenReturn("some-user-id")
    instance
  }

  "getCurrentState" should {
    "fail with UserAnswersNotFound" when {
      "repo doesn't contain any answers for the user" in {
        val mockRepo = mock[SessionRepository]
        when(mockRepo.get(request.userId)).thenReturn(Future.successful(None))

        val service = new LongRunningSubmissionService(mockRepo)

        service.getCurrentState(BTN).futureValue.left.value mustBe UserAnswersNotFound(request.userId)
      }
    }
    "use the summoned lookup" when {
      "repo contains answers" in {
        val answers  = UserAnswers(request.userId)
        val mockRepo = mock[SessionRepository]
        when(mockRepo.get(request.userId)).thenReturn(Future.successful(Some(answers)))

        val service = new LongRunningSubmissionService(mockRepo)

        service.getCurrentState(BTN).futureValue mustBe implicitly[SubmissionAnswerLookup[BTN.type]].extractStateFromAnswers(BTN, answers)
      }
    }
  }
}
