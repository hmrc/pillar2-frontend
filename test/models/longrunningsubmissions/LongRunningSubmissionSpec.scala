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

package models.longrunningsubmissions

import cats.syntax.either.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.EitherValues
import org.scalatest.matchers.must
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.JsPath
import play.api.mvc.PathBindable
import queries.{Gettable, Settable}

class LongRunningSubmissionSpec extends AnyWordSpec with must.Matchers with ScalaCheckPropertyChecks with EitherValues {

  case object DummyPage extends Gettable[Unit] with Settable[Unit] {
    override def path: JsPath = JsPath
  }

  val anySubmissionLookupError: Gen[SubmissionLookupError] = Gen.oneOf(
    SubmissionLookupError.UserAnswersNotFound("some-pillar-ref"),
    SubmissionLookupError.SpecificAnswerNotFound(DummyPage)
  )
  val anySubmissionError: Gen[SubmissionState.Error] = Gen.oneOf(
    SubmissionState.values.collect { case e: SubmissionState.Error => e }
  )
  val anyError: Gen[LongRunningSubmission.AnyError] =
    Gen.oneOf(anySubmissionLookupError.map(_.asLeft), anySubmissionError.map(_.asRight))

  "BTN" must {
    "redirect to the BTN confirmation screen on success" in {
      LongRunningSubmission.BTN.submittedPage mustBe controllers.btn.routes.BTNConfirmationController.onPageLoad
    }

    "redirect to the BTN technical issue page on any error" in forAll(anyError) { error =>
      LongRunningSubmission.BTN.errorPage(error) mustBe controllers.btn.routes.BTNProblemWithServiceController.onPageLoad
    }
  }

  "ManageContactDetails" must {
    "redirect to the homepage on success" in {
      LongRunningSubmission.ManageContactDetails.submittedPage mustBe controllers.routes.HomepageController.onPageLoad()
    }

    "redirect to the amend subscription failed page on any error" in forAll(anyError) { error =>
      LongRunningSubmission.ManageContactDetails.errorPage(error) mustBe controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad()
    }
  }

  "ManageGroupDetails" must {
    "redirect to the homepage on success" in {
      LongRunningSubmission.ManageGroupDetails.submittedPage mustBe controllers.routes.HomepageController.onPageLoad()
    }

    "redirect to the amend subscription failed page on any error" in forAll(anyError) { error =>
      LongRunningSubmission.ManageGroupDetails.errorPage(error) mustBe controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad()
    }
  }

  "Registration" must {
    "redirect to the registration confirmation screen on success" in {
      LongRunningSubmission.Registration.submittedPage mustBe controllers.routes.RegistrationConfirmationController.onPageLoad()
    }

    "redirect to the proper error page" when {
      "hitting a generic technical error" in {
        LongRunningSubmission.Registration.errorPage(
          SubmissionState.Error.GenericTechnical.asRight
        ) mustBe controllers.subscription.routes.SubscriptionFailedController.onPageLoad
      }

      "hitting a 422 or unrecoverable duplicate error" in forAll(
        Gen.oneOf(SubmissionState.Error.Duplicate.Unrecoverable.asRight, SubmissionState.Error.Unprocessable.asRight)
      ) { error =>
        LongRunningSubmission.Registration.errorPage(error) mustBe controllers.subscription.routes.SubscriptionFailureController.onPageLoad
      }

      "hitting a recoverable duplicate error" in {
        LongRunningSubmission.Registration.errorPage(
          SubmissionState.Error.Duplicate.Recoverable.asRight
        ) mustBe controllers.subscription.routes.DuplicateSafeIdController.onPageLoad()
      }

      "hitting any other case" in forAll(
        Gen.oneOf(anySubmissionLookupError.map(_.asLeft), Gen.const(SubmissionState.Error.Incomplete.asRight))
      ) { error =>
        LongRunningSubmission.Registration.errorPage(error) mustBe controllers.routes.JourneyRecoveryController.onPageLoad()
      }
    }
  }

  "Repayments" must {
    "redirect to the confirmation screen on success" in {
      LongRunningSubmission.Repayments.submittedPage mustBe controllers.repayments.routes.RepaymentConfirmationController.onPageLoad()
    }

    "redirect to the proper error page" when {
      "submission is incomplete" in {
        LongRunningSubmission.Repayments.errorPage(
          SubmissionState.Error.Incomplete.asRight
        ) mustBe controllers.repayments.routes.RepaymentsIncompleteDataController.onPageLoad
      }

      "any other error" in forAll(anyError.retryUntil {
        case Right(SubmissionState.Error.Incomplete) => false
        case _                                       => true
      }) { error =>
        LongRunningSubmission.Repayments.errorPage(
          error
        ) mustBe controllers.repayments.routes.RepaymentErrorController.onPageLoadRepaymentSubmissionFailed()
      }
    }
  }

  "RFM" must {
    "redirect to the confirmation screen on success" in {
      LongRunningSubmission.RFM.submittedPage mustBe controllers.rfm.routes.RfmConfirmationController.onPageLoad()
    }

    "redirect to the proper error page" when {
      "hitting a generic error" in {
        LongRunningSubmission.RFM.errorPage(
          SubmissionState.Error.GenericTechnical.asRight
        ) mustBe controllers.rfm.routes.AmendApiFailureController.onPageLoad
      }

      "any other error" in forAll(anyError.retryUntil {
        case Right(SubmissionState.Error.GenericTechnical) => false
        case _                                             => true
      }) { error =>
        LongRunningSubmission.RFM.errorPage(error) mustBe controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad
      }
    }
  }

  "path binding" must {
    "extract the proper waiting room config from the URL" in forAll(
      Table(
        "path fragment"                -> "expected config",
        "below-threshold-notification" -> LongRunningSubmission.BTN,
        "manage-contact"               -> LongRunningSubmission.ManageContactDetails,
        "manage-group"                 -> LongRunningSubmission.ManageGroupDetails,
        "registration"                 -> LongRunningSubmission.Registration,
        "repayment"                    -> LongRunningSubmission.Repayments,
        "replace-filing-member"        -> LongRunningSubmission.RFM
      )
    ) { (fragment, expectedConfig) =>
      LongRunningSubmission.pathBinder(using PathBindable.bindableString).bind("submission", fragment).value mustBe expectedConfig
    }

    "fail when no config is defined the url" in forAll(arbitrary[String].retryUntil(!LongRunningSubmission.values.map(_.pathSegment).contains(_))) {
      invalidPathSegment =>
        LongRunningSubmission
          .pathBinder(using PathBindable.bindableString)
          .bind("submission", invalidPathSegment)
          .left
          .value mustBe "No matching LongRunningSubmission for fragment."
    }
  }

}
