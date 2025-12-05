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
import com.google.inject.Inject
import mapping.SubmissionAnswerLookup
import mapping.SubmissionAnswerLookup.Instances.given
import models.longrunningsubmissions.{LongRunningSubmission, SubmissionLookupError, SubmissionState}
import models.requests.UserIdRequest
import play.api.Logging
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import scala.concurrent.{ExecutionContext, Future}

class LongRunningSubmissionService @Inject() (
  sessionRepository: SessionRepository
)(using ec: ExecutionContext)
    extends FrontendHeaderCarrierProvider
    with Logging {

  def getCurrentState[A <: LongRunningSubmission](
    submission: A
  )(using request: UserIdRequest[?]): Future[Either[SubmissionLookupError, SubmissionState]] =
    sessionRepository
      .get(request.userId)
      .map(
        _.toRight(SubmissionLookupError.UserAnswersNotFound(request.userId))
          .flatMap(ua => implicitly[SubmissionAnswerLookup[A]].extractStateFromAnswers(submission, ua))
      )
}
