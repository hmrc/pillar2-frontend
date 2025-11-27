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

package mapping

import cats.syntax.either.*
import mapping.SubmissionAnswerLookup.ExtractResult
import models.UserAnswers
import models.btn.BTNStatus
import models.longrunningsubmissions.LongRunningSubmission.*
import models.longrunningsubmissions.{LongRunningSubmission, SubmissionLookupError, SubmissionState}
import models.repayments.RepaymentsStatus
import models.rfm.RfmStatus
import models.subscription.{ManageContactDetailsStatus, ManageGroupDetailsStatus, SubscriptionStatus}
import pages.*
import play.api.libs.json.Reads
import queries.Gettable

trait SubmissionAnswerLookup[A <: LongRunningSubmission] {
  def extractStateFromAnswers(submission: A, answers: UserAnswers): ExtractResult
}

object SubmissionAnswerLookup {
  type ExtractResult = Either[SubmissionLookupError, SubmissionState]

  object Instances {

    implicit def forAnySubmission[A <: LongRunningSubmission]: SubmissionAnswerLookup[A] =
      (submission: LongRunningSubmission, answers: UserAnswers) =>
        submission match {
          case LongRunningSubmission.BTN                  => forBtn.extractStateFromAnswers(BTN, answers)
          case LongRunningSubmission.ManageContactDetails => forContactDetails.extractStateFromAnswers(ManageContactDetails, answers)
          case LongRunningSubmission.ManageGroupDetails   => forGroupDetails.extractStateFromAnswers(ManageGroupDetails, answers)
          case LongRunningSubmission.Registration         => forRegistration.extractStateFromAnswers(Registration, answers)
          case LongRunningSubmission.Repayments           => forRepayments.extractStateFromAnswers(Repayments, answers)
          case LongRunningSubmission.RFM                  => forRfm.extractStateFromAnswers(RFM, answers)
        }

    implicit val forBtn: SubmissionAnswerLookup[BTN.type] = (_, answers: UserAnswers) =>
      withField(BTNStatus, answers) {
        case BTNStatus.submitted      => SubmissionState.Submitted.asRight
        case BTNStatus.error          => SubmissionState.Error.GenericTechnical.asRight
        case BTNStatus.processing | _ => SubmissionState.Processing.asRight
      }

    implicit val forContactDetails: SubmissionAnswerLookup[ManageContactDetails.type] = (_, answers: UserAnswers) =>
      withField(ManageContactDetailsStatusPage, answers) {
        case ManageContactDetailsStatus.SuccessfullyCompleted                                               => SubmissionState.Submitted.asRight
        case ManageContactDetailsStatus.InProgress                                                          => SubmissionState.Processing.asRight
        case ManageContactDetailsStatus.FailedInternalIssueError | ManageContactDetailsStatus.FailException =>
          SubmissionState.Error.GenericTechnical.asRight
      }

    implicit val forGroupDetails: SubmissionAnswerLookup[ManageGroupDetails.type] = (_, answers: UserAnswers) =>
      withField(ManageGroupDetailsStatusPage, answers) {
        case ManageGroupDetailsStatus.SuccessfullyCompleted                                             => SubmissionState.Submitted.asRight
        case ManageGroupDetailsStatus.InProgress                                                        => SubmissionState.Processing.asRight
        case ManageGroupDetailsStatus.FailedInternalIssueError | ManageGroupDetailsStatus.FailException =>
          SubmissionState.Error.GenericTechnical.asRight
      }

    implicit val forRegistration: SubmissionAnswerLookup[Registration.type] = (_, answers: UserAnswers) =>
      withField(SubscriptionStatusPage, answers) {
        case SubscriptionStatus.SuccessfullyCompletedSubscription        => SubmissionState.Submitted.asRight
        case SubscriptionStatus.RegistrationInProgress                   => SubmissionState.Processing.asRight
        case SubscriptionStatus.FailedWithDuplicatedSubmission           => SubmissionState.Error.Duplicate.Unrecoverable.asRight
        case SubscriptionStatus.FailedWithUnprocessableEntity            => SubmissionState.Error.Unprocessable.asRight
        case SubscriptionStatus.FailedWithInternalIssueError             => SubmissionState.Error.GenericTechnical.asRight
        case SubscriptionStatus.FailedWithDuplicatedSafeIdError          => SubmissionState.Error.Duplicate.Recoverable.asRight
        case SubscriptionStatus.FailedWithNoMneOrDomesticValueFoundError => SubmissionLookupError.SpecificAnswerNotFound(SubMneOrDomesticPage).asLeft
      }

    implicit val forRepayments: SubmissionAnswerLookup[Repayments.type] = (_, answers: UserAnswers) =>
      withField(RepaymentsStatusPage, answers) {
        case RepaymentsStatus.SuccessfullyCompleted   => SubmissionState.Submitted.asRight
        case RepaymentsStatus.InProgress              => SubmissionState.Processing.asRight
        case RepaymentsStatus.UnexpectedResponseError => SubmissionState.Error.GenericTechnical.asRight
        case RepaymentsStatus.IncompleteDataError     => SubmissionState.Error.Incomplete.asRight
      }

    implicit val forRfm: SubmissionAnswerLookup[RFM.type] = (_, answers: UserAnswers) =>
      withField(RfmStatusPage, answers) {
        case RfmStatus.SuccessfullyCompleted    => SubmissionState.Submitted.asRight
        case RfmStatus.InProgress               => SubmissionState.Processing.asRight
        case RfmStatus.FailedInternalIssueError => SubmissionState.Error.GenericTechnical.asRight
        case RfmStatus.FailException            => SubmissionState.Error.Incomplete.asRight
      }

    private def withField[A](key: Gettable[A], ua: UserAnswers)(extractor: A => ExtractResult)(implicit reads: Reads[A]): ExtractResult =
      ua.get(key).fold[ExtractResult](SubmissionLookupError.SpecificAnswerNotFound(key).asLeft)(extractor)

  }
}
