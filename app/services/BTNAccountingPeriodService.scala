/*
 * Copyright 2026 HM Revenue & Customs
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

import models.UserAnswers
import models.obligationsandsubmissions.ObligationType.UKTR
import models.obligationsandsubmissions.{AccountingPeriodDetails, SubmissionType}
import pages.BTNChooseAccountingPeriodPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utils.DateTimeUtils.*
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.given

import javax.inject.{Inject, Singleton}
import scala.language.implicitConversions

object BTNAccountingPeriodService {
  sealed trait Outcome
  object Outcome {
    case object BtnAlreadySubmitted extends Outcome
    case object UktrReturnAlreadySubmitted extends Outcome
    final case class ShowAccountingPeriod(summaryList: SummaryList, hasMultipleAccountingPeriods: Boolean, currentAP: Boolean) extends Outcome
  }
}

@Singleton
class BTNAccountingPeriodService @Inject() () {

  import BTNAccountingPeriodService.*

  def selectAccountingPeriod(userAnswers: UserAnswers, availablePeriods: Seq[AccountingPeriodDetails]): AccountingPeriodDetails =
    userAnswers.get(BTNChooseAccountingPeriodPage) match {
      case Some(details) =>
        details
      case None =>
        availablePeriods match {
          case singleAccountingPeriod :: Nil =>
            singleAccountingPeriod
          case e =>
            throw new RuntimeException(s"Expected one single accounting period but received: $e")
        }
    }

  def outcome(
    userAnswers:     UserAnswers,
    selectedPeriod:  AccountingPeriodDetails,
    availablePeriod: Seq[AccountingPeriodDetails],
    btnTypes:        Set[SubmissionType],
    uktrTypes:       Set[SubmissionType]
  )(using messages: Messages): Outcome =
    if lastSubmissionType(selectedPeriod, btnTypes) then {
      Outcome.BtnAlreadySubmitted
    } else if lastSubmissionType(selectedPeriod, uktrTypes) then {
      Outcome.UktrReturnAlreadySubmitted
    } else {
      val currentAP = availablePeriod match {
        case head :: _ if head == selectedPeriod => true
        case _                                   => false
      }

      Outcome.ShowAccountingPeriod(
        summaryList = summaryList(selectedPeriod),
        hasMultipleAccountingPeriods = availablePeriod.size > 1,
        currentAP = currentAP
      )
    }

  private def summaryList(period: AccountingPeriodDetails)(using messages: Messages): SummaryList =
    SummaryListViewModel(
      rows = Seq(
        SummaryListRowViewModel(
          key = "btn.returnSubmitted.startAccountDate",
          value = ValueViewModel(period.startDate.toDateFormat)
        ),
        SummaryListRowViewModel(
          key = "btn.returnSubmitted.endAccountDate",
          value = ValueViewModel(period.endDate.toDateFormat)
        )
      )
    )

  private def lastSubmissionType(period: AccountingPeriodDetails, submissionTypes: Set[SubmissionType]): Boolean =
    period.obligations
      .find(_.obligationType == UKTR)
      .flatMap(_.submissions.sortBy(_.receivedDate).lastOption)
      .exists(submission => submissionTypes.contains(submission.submissionType))
}
