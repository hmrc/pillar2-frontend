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
import controllers.routes
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import views.html.btn.BTNBeforeStartView

class BTNBeforeStartViewSpec extends ViewSpecBase {

  lazy val page:      BTNBeforeStartView = inject[BTNBeforeStartView]
  lazy val pageTitle: String             = "Below-Threshold Notification (BTN)"

  def view(isAgent: Boolean = false, hasMultipleAccountPeriods: Boolean = false): Document =
    Jsoup.parse(page(isAgent, hasMultipleAccountPeriods, NormalMode)(request, appConfig, messages).toString())

  "BTNBeforeStartView" should {
    "have a title" in {
      view().title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view().getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a banner with a link to the Homepage" in {
      val className: String = "govuk-header__link govuk-header__service-name"
      view().getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url
    }

    "have two h2 headings" in {
      val h2Elements: Elements = view().getElementsByTag("h2")
      h2Elements.get(0).text mustBe "Who can submit a Below-Threshold Notification"
      h2Elements.get(0).hasClass("govuk-heading-m") mustBe true
      h2Elements.get(1).text mustBe "Before you start"
      h2Elements.get(1).hasClass("govuk-heading-m") mustBe true
    }

    "have group specific content" in {
      val paragraphs: Elements = view().getElementsByClass("govuk-body")

      paragraphs.get(0).text mustBe
        "The Below-Threshold Notification satisfies your group’s obligation to submit a UK Tax Return for the " +
        "current and future accounting periods. HMRC will not expect to receive an information return while your " +
        "group remains below-threshold."
      paragraphs.get(1).text mustBe "You can submit a Below-Threshold Notification if the group:"

      view().getElementsByClass("govuk-inset-text").text mustBe
        "If you need to submit a UK tax return for this accounting period you do not qualify for a Below-Threshold Notification."
    }

    "have agent specific content" in {
      val paragraphs: Elements = view(isAgent = true).getElementsByClass("govuk-body")

      paragraphs.get(0).text mustBe
        "The Below-Threshold Notification satisfies the group’s obligation to submit a UK Tax Return for the " +
        "current and future accounting periods. HMRC will not expect to receive an information return while the " +
        "group remains below-threshold."

      paragraphs.get(1).text mustBe "The group can submit a Below-Threshold Notification if it:"

      view(isAgent = true).getElementsByClass("govuk-inset-text").text mustBe
        "If your client needs to submit a UK tax return for this accounting period they do not qualify for a Below-Threshold Notification."
    }

    "have the following common content" in {
      val paragraphs: Elements = view().getElementsByClass("govuk-body")
      val listItems:  Elements = view().getElementsByTag("li")

      paragraphs.get(2).text() mustBe "To submit a Below-Threshold Notification you’ll need to tell us:"

      listItems.get(0).text mustBe "does not have consolidated annual revenues of €750 million or more in at " +
        "least 2 of the previous 4 accounting periods"
      listItems.get(1).text mustBe "is not expected to make consolidated annual revenues of €750 million or more " +
        "within the next 2 accounting periods"
      listItems.get(2).text mustBe "the start and end date of the group’s accounting period"
      listItems.get(3).text mustBe "whether the group has entities located in the UK"

      paragraphs.get(3).text() mustBe "If you submit a Below-Threshold Notification, this will replace any returns " +
        "you’ve submitted for that period. It will also replace any returns you have already submitted for your " +
        "most recent account periods."
    }

    "have a button" that {
      "links to the accounting period page when there is only one accounting period present" in {
        val button: Element = view().getElementsByClass("govuk-button").first()

        button.text mustBe "Continue"
        button.attr("href") mustBe controllers.btn.routes.BTNAccountingPeriodController.onPageLoad(NormalMode).url
      }

      "links to the choose accounting period page when there are multiple accounting periods present" in {
        val button: Element = view(hasMultipleAccountPeriods = true).getElementsByClass("govuk-button").first()

        button.text mustBe "Continue"
        button.attr("href") mustBe controllers.btn.routes.BTNChooseAccountingPeriodController.onPageLoad(NormalMode).url
      }
    }

  }
}
