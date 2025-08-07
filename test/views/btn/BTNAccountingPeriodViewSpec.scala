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
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
import views.html.btn.BTNAccountingPeriodView

class BTNAccountingPeriodViewSpec extends ViewSpecBase {
  val list: SummaryList = SummaryListViewModel(
    rows = Seq(
      SummaryListRowViewModel("btn.accountingPeriod.startAccountDate", value = ValueViewModel(HtmlContent(HtmlFormat.escape("7 January 2024")))),
      SummaryListRowViewModel(
        "btn.accountingPeriod.endAccountDate",
        value = ValueViewModel(HtmlContent(HtmlFormat.escape("7 January 2025").toString))
      )
    )
  )

  val page: BTNAccountingPeriodView = inject[BTNAccountingPeriodView]

  def view(isAgent: Boolean = false, hasMultipleAccountingPeriods: Boolean = false, currentAP: Boolean = true): Document =
    Jsoup.parse(
      page(list, NormalMode, isAgent, Some("orgName"), hasMultipleAccountingPeriods, currentAP)(request, appConfig, messages).toString()
    )

  lazy val pageTitle: String   = "Confirm account period for Below-Threshold Notification"
  lazy val headings:  Elements = view().select("h1, h2, h3, h4")
  println(headings)

  //def validateHeadings(headingElements: Elements) = ???

  "BTNAccountingPeriodView" when {
    "it's an organisation" should {
      "have a title" in {
        view().title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a h1 heading" in {
        val h1Elements: Elements = view().getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

//      "have a Title that matches the H1 heading" in {
//        view().title without the servicePrefix+GOVUK mustEqual view().getElementsByTag("h1").text
//      }

      "have following contents" in {
        view().getElementsByClass("govuk-body").text must include(
          "Your group will keep below-threshold status from this accounting period onwards, unless you file a UK tax return."
        )
        view().getElementsByClass("govuk-summary-list__key").text must include(
          "Start date of accounting period"
        )
        view().getElementsByClass("govuk-summary-list__key").text must include(
          "End date of accounting period"
        )
      }

      "have a link for selecting a different accounting period when they have multiple accounting periods" in {
        val link = view(hasMultipleAccountingPeriods = true).getElementsByClass("govuk-body").get(1).getElementsByTag("a")
        link.text must include("Select different accounting period")
        link.attr("href") must include(
          controllers.btn.routes.BTNChooseAccountingPeriodController.onPageLoad(NormalMode).url
        )
      }

      "have a paragraph with link if it's the current accounting period" in {
        val link = view().getElementsByClass("govuk-body").last().getElementsByTag("a")
        view().getElementsByTag("p").text must include(
          "If the accounting period dates are wrong,"
        )
        link.text         must include("update your group’s accounting period dates")
        link.attr("href") must include(controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url)
        view().getElementsByTag("p").text must include(
          "before continuing."
        )
      }

      "not have a paragraph with link if it's a previous accounting period" in {
        val link = view(hasMultipleAccountingPeriods = true, currentAP = false).getElementsByClass("govuk-body").last().getElementsByTag("a")
        view(hasMultipleAccountingPeriods = true, currentAP = false).getElementsByTag("p").text mustNot include(
          "If the accounting period dates are wrong,"
        )
        link.text mustNot include("update your group’s accounting period dates")
        link.attr("href") mustNot include(controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url)
        view(hasMultipleAccountingPeriods = true, currentAP = false).getElementsByTag("p").text mustNot include(
          "before continuing."
        )
      }

      "have a button" in {
        view().getElementsByClass("govuk-button").text must include("Continue")
      }

    }

    "it's an agent" should {
      "have a caption" in {
        view(isAgent = true).getElementsByClass("govuk-caption-m").text must include("orgName")
      }

      "have a title" in {
        view(isAgent = true).getElementsByTag("title").text must include("Confirm account period for Below-Threshold Notification")
      }

      "have a h1 heading" in {
        view(isAgent = true).getElementsByTag("h1").text must include("Confirm account period for Below-Threshold Notification")
      }

      "have following contents" in {
        view(isAgent = true).getElementsByClass("govuk-body").text must include(
          "The group will keep below-threshold status from this accounting period onwards, unless a UK Tax Return is filed."
        )
        view(isAgent = true).getElementsByClass("govuk-summary-list__key").text must include(
          "Start date of accounting period"
        )
        view(isAgent = true).getElementsByClass("govuk-summary-list__key").text must include(
          "End date of accounting period"
        )
      }

      "have a link for selecting a different accounting period when they have multiple accounting periods" in {
        val link = view(isAgent = true, hasMultipleAccountingPeriods = true).getElementsByClass("govuk-body").get(1).getElementsByTag("a")
        link.text must include("Select different accounting period")
        link.attr("href") must include(
          controllers.btn.routes.BTNChooseAccountingPeriodController.onPageLoad(NormalMode).url
        )
      }

      "have a paragraph with link if it's the current accounting period" in {
        val link = view(isAgent = true).getElementsByClass("govuk-body").last().getElementsByTag("a")
        view(isAgent = true).getElementsByTag("p").text must include(
          "If the accounting period dates are wrong,"
        )
        link.text         must include("update the group’s accounting period dates")
        link.attr("href") must include(controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url)
        view(isAgent = true).getElementsByTag("p").text must include(
          "before continuing."
        )
      }

      "not have a paragraph with link if it's a previous accounting period" in {
        val link =
          view(isAgent = true, hasMultipleAccountingPeriods = true, currentAP = false).getElementsByClass("govuk-body").last().getElementsByTag("a")
        view(isAgent = true, hasMultipleAccountingPeriods = true, currentAP = false).getElementsByTag("p").text mustNot include(
          "If the accounting period dates are wrong,"
        )
        link.text mustNot include("update the group’s accounting period dates")
        link.attr("href") mustNot include(controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url)
        view(isAgent = true, hasMultipleAccountingPeriods = true, currentAP = false).getElementsByTag("p").text mustNot include(
          "before continuing."
        )
      }

      "have a button" in {
        view(isAgent = true).getElementsByClass("govuk-button").text must include("Continue")
      }

    }
  }
}
