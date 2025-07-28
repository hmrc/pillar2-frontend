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

package views.btn

import base.ViewSpecBase
import forms.BTNChooseAccountingPeriodFormProvider
import models.NormalMode
import models.obligationsandsubmissions.AccountingPeriodDetails
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.btn.BTNChooseAccountingPeriodView

import java.time.LocalDate

class BTNChooseAccountingPeriodViewSpec extends ViewSpecBase {
  val accountingPeriodDetails: Seq[(AccountingPeriodDetails, Int)] = Seq(
    AccountingPeriodDetails(LocalDate.now, LocalDate.now(), LocalDate.now(), underEnquiry = false, Seq.empty),
    AccountingPeriodDetails(LocalDate.now.plusYears(1), LocalDate.now.plusYears(1), LocalDate.now.plusYears(1), underEnquiry = false, Seq.empty)
  ).zipWithIndex

  val formProvider = new BTNChooseAccountingPeriodFormProvider
  val page: BTNChooseAccountingPeriodView = inject[BTNChooseAccountingPeriodView]
  def view(isAgent: Boolean = false): Document =
    Jsoup.parse(page(formProvider(), NormalMode, isAgent, Some("orgName"), accountingPeriodDetails)(request, appConfig, messages).toString())

  "BTNChooseAccountingPeriodView" should {

    "have a caption for an agent view" in {
      view(isAgent = true).getElementsByClass("govuk-caption-m").text must include("orgName")
    }

    "not have a caption for organisation view" in {
      view().getElementsByClass("govuk-caption-m").text mustNot include("orgName")
    }

    "have a title" in {
      view().getElementsByTag("title").text must include("Which accounting period would you like to register a Below-Threshold Notification for?")
    }

    "have a h1 heading" in {
      view().getElementsByTag("h1").text must include("Which accounting period would you like to register a Below-Threshold Notification for?")
    }

    "have a paragraph" in {
      view().getElementsByTag("p").text must include("We only list the current and previous accounting periods.")
    }

    "have radio items" in {
      view().getElementsByClass("govuk-label govuk-radios__label").get(0).text must include(s"${accountingPeriodDetails.head._1.formattedDates}")
      view().getElementsByClass("govuk-label govuk-radios__label").get(1).text must include(s"${accountingPeriodDetails.last._1.formattedDates}")
    }

    "have a button" in {
      view().getElementsByClass("govuk-button").text must include("Continue")
    }
  }
}
