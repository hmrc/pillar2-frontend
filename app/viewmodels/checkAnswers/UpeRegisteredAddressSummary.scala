/*
 * Copyright 2023 HM Revenue & Customs
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

import models.{CheckMode, UserAnswers}
import pages.RegistrationPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object UpeRegisteredAddressSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers
      .get(RegistrationPage)
      .flatMap { reg =>
        reg.withoutIdRegData.map { withoutId =>
          withoutId.upeRegisteredAddress.map { answer =>
            val field1      = HtmlFormat.escape(answer.addressLine1).toString + "<br>"
            val field2      = if (answer.addressLine2.isDefined) HtmlFormat.escape(answer.addressLine2.mkString("")) + "<br>" else ""
            val field3      = HtmlFormat.escape(answer.addressLine3).toString + "<br>"
            val field4      = if (answer.addressLine4.isDefined) HtmlFormat.escape(answer.addressLine4.mkString("")) + "<br>" else ""
            val postcode    = if (answer.postalCode.isDefined) HtmlFormat.escape(answer.postalCode.mkString("")) + "<br>" else ""
            val countryCode = HtmlFormat.escape(answer.countryCode)
            val value       = field1 + field2 + field3 + field4 + postcode + countryCode

            SummaryListRowViewModel(
              key = "upe-registered-address.checkYourAnswersLabel",
              value = ValueViewModel(HtmlContent(value)),
              actions = Seq(
                ActionItemViewModel("site.change", controllers.registration.routes.UpeRegisteredAddressController.onPageLoad(CheckMode).url)
                  .withVisuallyHiddenText(messages("upe-registered-address.change.hidden"))
              )
            )
          }
        }
      }
      .flatten
}
