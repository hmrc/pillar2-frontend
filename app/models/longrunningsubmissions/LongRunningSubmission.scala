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

import enumeratum.values.{StringEnum, StringEnumEntry}
import models.longrunningsubmissions.LongRunningSubmission.AnyError
import play.api.mvc.{Call, PathBindable}

sealed abstract class LongRunningSubmission extends StringEnumEntry {
  def value: String

  def configKey:     String
  def pathSegment:   String = value
  def submittedPage: Call
  def errorPage:     AnyError => Call
}

object LongRunningSubmission extends StringEnum[LongRunningSubmission] {

  type AnyError = Either[SubmissionLookupError, SubmissionState.Error]

  case object BTN extends LongRunningSubmission {
    override val value:              String           = "below-threshold-notification"
    override val configKey:          String           = "btn"
    override lazy val submittedPage: Call             = controllers.btn.routes.BTNConfirmationController.onPageLoad
    override val errorPage:          AnyError => Call = _ => controllers.btn.routes.BTNProblemWithServiceController.onPageLoad
  }

  case object ManageContactDetails extends LongRunningSubmission {
    override val value:              String           = "manage-contact"
    override val configKey:          String           = "contact"
    override lazy val submittedPage: Call             = controllers.routes.HomepageController.onPageLoad()
    override val errorPage:          AnyError => Call = _ => controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad()
  }

  case object ManageGroupDetails extends LongRunningSubmission {
    override val value:              String           = "manage-group"
    override val configKey:          String           = "group"
    override lazy val submittedPage: Call             = controllers.routes.HomepageController.onPageLoad()
    override val errorPage:          AnyError => Call = _ => controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad()
  }

  case object Registration extends LongRunningSubmission {
    override val value:              String           = "registration"
    override val configKey:          String           = "registration"
    override lazy val submittedPage: Call             = controllers.routes.RegistrationConfirmationController.onPageLoad()
    override val errorPage:          AnyError => Call = {
      case Right(SubmissionState.Error.GenericTechnical) => controllers.subscription.routes.SubscriptionFailedController.onPageLoad
      case Right(SubmissionState.Error.Unprocessable | SubmissionState.Error.Duplicate.Unrecoverable) =>
        controllers.subscription.routes.SubscriptionFailureController.onPageLoad
      case Right(SubmissionState.Error.Duplicate.Recoverable) => controllers.subscription.routes.DuplicateSafeIdController.onPageLoad()
      case Left(
            SubmissionLookupError.SpecificAnswerNotFound(_) | SubmissionLookupError.UserAnswersNotFound(_)
          ) | Right(SubmissionState.Error.Incomplete) =>
        controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  }

  case object Repayments extends LongRunningSubmission {
    override val value:              String           = "repayment"
    override val configKey:          String           = "repayments"
    override lazy val submittedPage: Call             = controllers.repayments.routes.RepaymentConfirmationController.onPageLoad()
    override val errorPage:          AnyError => Call = {
      case Right(SubmissionState.Error.Incomplete)   => controllers.repayments.routes.RepaymentsIncompleteDataController.onPageLoad
      case Right(_: SubmissionState.Error) | Left(_) => controllers.repayments.routes.RepaymentErrorController.onPageLoadRepaymentSubmissionFailed()
    }
  }

  case object RFM extends LongRunningSubmission {
    override val value:              String           = "replace-filing-member"
    override val configKey:          String           = "rfm"
    override lazy val submittedPage: Call             = controllers.rfm.routes.RfmConfirmationController.onPageLoad()
    override val errorPage:          AnyError => Call = {
      case Right(SubmissionState.Error.GenericTechnical) => controllers.rfm.routes.AmendApiFailureController.onPageLoad
      case Right(_: SubmissionState.Error) | Left(_)     => controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad
    }
  }

  val values: IndexedSeq[LongRunningSubmission] = findValues

  given pathBinder(using stringBinder: PathBindable[String]): PathBindable[LongRunningSubmission] =
    new PathBindable[LongRunningSubmission] {
      override def bind(key: String, value: String): Either[String, LongRunningSubmission] = for {
        pathSegment      <- stringBinder.bind(key, value)
        submissionConfig <- LongRunningSubmission.values.find(_.pathSegment == pathSegment).toRight("No matching LongRunningSubmission for fragment.")
      } yield submissionConfig

      override def unbind(key: String, value: LongRunningSubmission): String = value.pathSegment
    }
}
