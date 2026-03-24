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

package utils

import play.api.i18n.Messages

import java.time.{LocalDate, Period}

object AmendAccountingPeriodDurationFormatter {

  def formatInclusivePeriod(start: LocalDate, end: LocalDate)(using messages: Messages): String =
    formatPeriod(Period.between(start, end.plusDays(1)))

  private[utils] def formatPeriod(period: Period)(using messages: Messages): String = {
    val parts = Seq(
      Option.when(period.getYears > 0)(yearLabel(period.getYears)),
      Option.when(period.getMonths > 0)(monthLabel(period.getMonths)),
      Option.when(period.getDays > 0)(dayLabel(period.getDays))
    ).flatten

    joinParts(parts)
  }

  private def yearLabel(n: Int)(using messages: Messages): String =
    if n == 1 then messages("amendAccountingPeriodCYA.duration.year", n)
    else messages("amendAccountingPeriodCYA.duration.years", n)

  private def monthLabel(n: Int)(using messages: Messages): String =
    if n == 1 then messages("amendAccountingPeriodCYA.duration.month", n)
    else messages("amendAccountingPeriodCYA.duration.months", n)

  private def dayLabel(n: Int)(using messages: Messages): String =
    if n == 1 then messages("amendAccountingPeriodCYA.duration.day", n)
    else messages("amendAccountingPeriodCYA.duration.days", n)

  private def joinParts(parts: Seq[String]): String =
    parts match
      case Seq()     => ""
      case Seq(one)  => one
      case p :+ last =>
        p.mkString(", ") + " and " + last
}
