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

package models.subscription.journeys

import cats.data.EitherNec
import cats.implicits.*
import models.{MneOrDomestic, UserAnswers}
import pages.{SubAccountingPeriodPage, SubMneOrDomesticPage}
import play.api.i18n.Messages
import queries.Query
import utils.DateTimeUtils.*

final case class GroupJourney(
  mneOrDomestic:                  MneOrDomestic,
  groupAccountingPeriodStartDate: String,
  groupAccountingPeriodEndDate:   String
)

object GroupJourney {

  def from(answers: UserAnswers)(using messages: Messages): EitherNec[Query, GroupJourney] =
    (
      answers.getEither(SubMneOrDomesticPage),
      answers.getEither(SubAccountingPeriodPage).map(accountingPeriod => accountingPeriod.startDate.toDateFormat),
      answers.getEither(SubAccountingPeriodPage).map(accountingPeriod => accountingPeriod.endDate.toDateFormat)
    ).parMapN {
      (
        mneOrDomestic,
        apStartDate,
        apEndDate
      ) =>
        GroupJourney(
          mneOrDomestic,
          apStartDate,
          apEndDate
        )
    }
}
