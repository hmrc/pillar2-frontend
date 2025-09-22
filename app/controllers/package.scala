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

import models.obligationsandsubmissions.AccountingPeriodDetails
import models.requests.ObligationsAndSubmissionsSuccessDataRequest

import java.time.LocalDate

package object controllers {
  private val now = LocalDate.now

  /** @param accountingDetails
    *   is a sequence of AccountingPeriodDetails
    * @return
    *   a filtered sequence of AccountingPeriodDetails, sorted in reverse chronological order, ensuring that we remove any periods where the start
    *   date is after today
    */
  def filteredAccountingPeriodDetails(implicit request: ObligationsAndSubmissionsSuccessDataRequest[_]): Seq[AccountingPeriodDetails] =
    request.obligationsAndSubmissionsSuccessData.accountingPeriodDetails
      .filterNot(_.startDate.isAfter(now))
      .sortBy(_.startDate)
      .reverse
}
