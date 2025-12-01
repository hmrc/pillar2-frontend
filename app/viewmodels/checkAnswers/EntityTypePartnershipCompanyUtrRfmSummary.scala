/*
 * Copyright 2024 HM Revenue & Customs
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

package viewmodels.checkAnswers

import models.UserAnswers
import pages.RfmGRSUkPartnershipPage
import play.api.i18n.Messages
import scala.language.implicitConversions
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.given

object EntityTypePartnershipCompanyUtrRfmSummary {

  def row(answers: UserAnswers)(using messages: Messages): Option[SummaryListRow] =
    answers.get(RfmGRSUkPartnershipPage).flatMap { partnershipEntityRegistrationData =>
      partnershipEntityRegistrationData.sautr.map { sautr =>
        SummaryListRowViewModel(
          key = "entityType.companyUtr.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(sautr))
        )
      }
    }
}
