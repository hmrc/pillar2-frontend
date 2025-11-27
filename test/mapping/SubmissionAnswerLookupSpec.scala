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

import models.UserAnswers
import models.btn.BTNStatus
import models.longrunningsubmissions.LongRunningSubmission.*
import models.longrunningsubmissions.SubmissionLookupError.SpecificAnswerNotFound
import models.longrunningsubmissions.{LongRunningSubmission, SubmissionState}
import models.repayments.RepaymentsStatus
import models.rfm.RfmStatus
import models.subscription.{ManageContactDetailsStatus, ManageGroupDetailsStatus, SubscriptionStatus}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.matchers.must
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{Assertion, EitherValues, TryValues}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import pages.*
import queries.Gettable

class SubmissionAnswerLookupSpec extends AnyWordSpec with must.Matchers with ScalaCheckDrivenPropertyChecks with EitherValues with TryValues {

  val ua: UserAnswers = UserAnswers("id")

  "instance for BTN" should {
    import mapping.SubmissionAnswerLookup.Instances.forBtn

    behave like returnsSpecificAnswerNotFound(BTN, BTNStatus)

    "map submitted" in {
      forBtn
        .extractStateFromAnswers(BTN, ua.set(BTNStatus, BTNStatus.submitted).success.value)
        .value mustBe SubmissionState.Submitted
    }

    "map processing (or anything else)" in forAll(
      Gen.oneOf(Gen.const(BTNStatus.processing), arbitrary[String]).retryUntil(!Seq(BTNStatus.submitted, BTNStatus.error).contains(_))
    ) { state =>
      forBtn
        .extractStateFromAnswers(BTN, ua.set(BTNStatus, state).success.value)
        .value mustBe SubmissionState.Processing
    }

    "map error" in {
      forBtn
        .extractStateFromAnswers(BTN, ua.set(BTNStatus, BTNStatus.error).success.value)
        .value mustBe SubmissionState.Error.GenericTechnical
    }
  }

  "instance for manage contact details" should {
    import mapping.SubmissionAnswerLookup.Instances.forContactDetails

    behave like returnsSpecificAnswerNotFound(ManageContactDetails, ManageContactDetailsStatusPage)

    "map completed" in {
      forContactDetails
        .extractStateFromAnswers(
          ManageContactDetails,
          ua.set(ManageContactDetailsStatusPage, ManageContactDetailsStatus.SuccessfullyCompleted).success.value
        )
        .value mustBe SubmissionState.Submitted
    }

    "map processing" in {
      forContactDetails
        .extractStateFromAnswers(ManageContactDetails, ua.set(ManageContactDetailsStatusPage, ManageContactDetailsStatus.InProgress).success.value)
        .value mustBe SubmissionState.Processing
    }

    "map errors" in forAll(Gen.oneOf(ManageContactDetailsStatus.FailException, ManageContactDetailsStatus.FailedInternalIssueError)) { error =>
      forContactDetails
        .extractStateFromAnswers(ManageContactDetails, ua.set(ManageContactDetailsStatusPage, error).success.value)
        .value mustBe SubmissionState.Error.GenericTechnical
    }
  }

  "instance for manage group details" should {
    import mapping.SubmissionAnswerLookup.Instances.forGroupDetails

    behave like returnsSpecificAnswerNotFound(ManageGroupDetails, ManageGroupDetailsStatusPage)

    "map completed" in {
      forGroupDetails
        .extractStateFromAnswers(
          ManageGroupDetails,
          ua.set(ManageGroupDetailsStatusPage, ManageGroupDetailsStatus.SuccessfullyCompleted).success.value
        )
        .value mustBe SubmissionState.Submitted
    }

    "map processing" in {
      forGroupDetails
        .extractStateFromAnswers(ManageGroupDetails, ua.set(ManageGroupDetailsStatusPage, ManageGroupDetailsStatus.InProgress).success.value)
        .value mustBe SubmissionState.Processing
    }

    "map errors" in forAll(Gen.oneOf(ManageGroupDetailsStatus.FailException, ManageGroupDetailsStatus.FailedInternalIssueError)) { error =>
      forGroupDetails
        .extractStateFromAnswers(ManageGroupDetails, ua.set(ManageGroupDetailsStatusPage, error).success.value)
        .value mustBe SubmissionState.Error.GenericTechnical
    }
  }

  "instance for registration" should {
    import mapping.SubmissionAnswerLookup.Instances.forRegistration

    behave like returnsSpecificAnswerNotFound(Registration, SubscriptionStatusPage)

    "map completed" in {
      forRegistration
        .extractStateFromAnswers(
          Registration,
          ua.set(SubscriptionStatusPage, SubscriptionStatus.SuccessfullyCompletedSubscription).success.value
        )
        .value mustBe SubmissionState.Submitted
    }

    "map processing" in {
      forRegistration
        .extractStateFromAnswers(
          Registration,
          ua.set(SubscriptionStatusPage, SubscriptionStatus.RegistrationInProgress).success.value
        )
        .value mustBe SubmissionState.Processing
    }

    "map errors" when {
      "enrolment already exists" in {
        forRegistration
          .extractStateFromAnswers(
            Registration,
            ua.set(SubscriptionStatusPage, SubscriptionStatus.FailedWithDuplicatedSubmission).success.value
          )
          .value mustBe SubmissionState.Error.Duplicate.Unrecoverable
      }

      "we get an 'unprocessable' response" in {
        forRegistration
          .extractStateFromAnswers(
            Registration,
            ua.set(SubscriptionStatusPage, SubscriptionStatus.FailedWithUnprocessableEntity).success.value
          )
          .value mustBe SubmissionState.Error.Unprocessable
      }

      "we fail due to an internal issue" in {
        forRegistration
          .extractStateFromAnswers(
            Registration,
            ua.set(SubscriptionStatusPage, SubscriptionStatus.FailedWithInternalIssueError).success.value
          )
          .value mustBe SubmissionState.Error.GenericTechnical
      }

      "when ID is duplicated across NFM and UPE" in {
        forRegistration
          .extractStateFromAnswers(
            Registration,
            ua.set(SubscriptionStatusPage, SubscriptionStatus.FailedWithDuplicatedSafeIdError).success.value
          )
          .value mustBe SubmissionState.Error.Duplicate.Recoverable
      }

      "when we've persisted a failed lookup of the MNE/Domestic answer" in {
        forRegistration
          .extractStateFromAnswers(
            Registration,
            ua.set(SubscriptionStatusPage, SubscriptionStatus.FailedWithNoMneOrDomesticValueFoundError).success.value
          )
          .left
          .value mustBe SpecificAnswerNotFound(SubMneOrDomesticPage)
      }
    }
  }

  "instance for repayments" should {
    import mapping.SubmissionAnswerLookup.Instances.forRepayments

    behave like returnsSpecificAnswerNotFound(Repayments, RepaymentsStatusPage)

    "map completed" in {
      forRepayments
        .extractStateFromAnswers(Repayments, ua.set(RepaymentsStatusPage, RepaymentsStatus.SuccessfullyCompleted).success.value)
        .value mustBe SubmissionState.Submitted
    }

    "map processing" in {
      forRepayments
        .extractStateFromAnswers(Repayments, ua.set(RepaymentsStatusPage, RepaymentsStatus.InProgress).success.value)
        .value mustBe SubmissionState.Processing
    }

    "map errors" when {
      "we get a non-201 response" in {
        forRepayments
          .extractStateFromAnswers(Repayments, ua.set(RepaymentsStatusPage, RepaymentsStatus.UnexpectedResponseError).success.value)
          .value mustBe SubmissionState.Error.GenericTechnical
      }

      "a connector fails" in {
        forRepayments
          .extractStateFromAnswers(Repayments, ua.set(RepaymentsStatusPage, RepaymentsStatus.IncompleteDataError).success.value)
          .value mustBe SubmissionState.Error.Incomplete
      }
    }
  }

  "instance for rfm" should {
    import mapping.SubmissionAnswerLookup.Instances.forRfm

    behave like returnsSpecificAnswerNotFound(RFM, RfmStatusPage)

    "map completed" in {
      forRfm.extractStateFromAnswers(RFM, ua.set(RfmStatusPage, RfmStatus.SuccessfullyCompleted).success.value).value mustBe SubmissionState.Submitted
    }

    "map processing" in {
      forRfm.extractStateFromAnswers(RFM, ua.set(RfmStatusPage, RfmStatus.InProgress).success.value).value mustBe SubmissionState.Processing
    }

    "map errors" when {
      "we get an unexpected or internal issue error" in {
        forRfm
          .extractStateFromAnswers(RFM, ua.set(RfmStatusPage, RfmStatus.FailedInternalIssueError).success.value)
          .value mustBe SubmissionState.Error.GenericTechnical
      }

      "any details are missing during repayment submission" in {
        forRfm
          .extractStateFromAnswers(RFM, ua.set(RfmStatusPage, RfmStatus.FailException).success.value)
          .value mustBe SubmissionState.Error.Incomplete
      }
    }
  }

  def returnsSpecificAnswerNotFound[A <: LongRunningSubmission](
    submission:      A,
    expectedFailure: Gettable[?]
  )(implicit lookup: SubmissionAnswerLookup[A]): Assertion =
    lookup.extractStateFromAnswers(submission, UserAnswers("fake id")).left.value mustBe SpecificAnswerNotFound(expectedFailure)
}
