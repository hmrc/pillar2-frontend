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
import models.obligationsandsubmissions.ObligationStatus.Fulfilled
import models.obligationsandsubmissions.ObligationType.UKTR
import models.obligationsandsubmissions.SubmissionType.UKTR_CREATE
import models.obligationsandsubmissions.{AccountingPeriodDetails, Obligation, Submission}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.btn.BTNReturnSubmittedView

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZonedDateTime}

class BTNReturnSubmittedViewSpec extends ViewSpecBase {

  val accountingPeriodStartDate: LocalDate = LocalDate.now().minusYears(1)
  val accountingPeriodEndDate:   LocalDate = LocalDate.now()

  val accountingPeriodDetails: AccountingPeriodDetails = AccountingPeriodDetails(
    accountingPeriodStartDate,
    accountingPeriodEndDate,
    LocalDate.now().plusYears(1),
    underEnquiry = false,
    Seq(Obligation(UKTR, Fulfilled, canAmend = true, Seq(Submission(UKTR_CREATE, ZonedDateTime.now(), None))))
  )

  val formattedStartDate: String = accountingPeriodStartDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
  val formattedEndDate:   String = accountingPeriodEndDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))

  val page: BTNReturnSubmittedView = inject[BTNReturnSubmittedView]
  def view(isAgent: Boolean = false): Document = Jsoup.parse(page(isAgent, accountingPeriodDetails)(request, appConfig, messages).toString())

  "BTNAccountingPeriodView" when {
    "it's an organisation" should {
      "have a title" in {
        view().getElementsByTag("title").text must include(
          s"You’ve submitted a UK Tax Return for the accounting period $formattedStartDate - $formattedEndDate"
        )
      }

      "have a h1 heading" in {
        view().getElementsByTag("h1").text must include(
          s"You’ve submitted a UK Tax Return for the accounting period $formattedStartDate - $formattedEndDate"
        )
      }

      "have a paragraph" in {
        view().getElementsByClass("govuk-body").text must include(
          "By continuing, your UK Tax Return will be replaced for this period."
        )
      }

      "have an inset text" in {
        view().getElementsByClass("govuk-inset-text").text must include(
          "If you need to submit a UK Tax Return for this accounting period you do not qualify for a Below-Threshold Notification."
        )
      }

      "have a button" in {
        view().getElementsByClass("govuk-button").text must include("Continue")
      }

      "have a link" in {
        val link = view().getElementsByClass("govuk-body").last().getElementsByTag("a")
        link.text must include("Return to homepage")
        link.attr("href") must include(
          controllers.routes.DashboardController.onPageLoad.url
        )
      }

    }

    "it's an agent" should {
      "have a title" in {
        view(isAgent = true).getElementsByTag("title").text must include(
          s"The group has submitted a UK Tax Return for the accounting period $formattedStartDate - $formattedEndDate"
        )
      }

      "have a h1 heading" in {
        view(isAgent = true).getElementsByTag("h1").text must include(
          s"The group has submitted a UK Tax Return for the accounting period $formattedStartDate - $formattedEndDate"
        )
      }

      "have a paragraph" in {
        view(isAgent = true).getElementsByClass("govuk-body").text must include(
          "By continuing, the group’s UK Tax Return will be replaced for this period."
        )
      }

      "have an inset text" in {
        view(isAgent = true).getElementsByClass("govuk-inset-text").text must include(
          "If the group needs to submit a UK Tax Return for this accounting period they do not qualify for a Below-Threshold Notification."
        )
      }

      "have a button" in {
        view(isAgent = true).getElementsByClass("govuk-button").text must include("Continue")
      }

      "have a link" in {
        val link = view(true).getElementsByClass("govuk-body").last().getElementsByTag("a")
        link.text must include("Return to homepage")
        link.attr("href") must include(
          controllers.routes.DashboardController.onPageLoad.url
        )
      }
    }
  }
}
