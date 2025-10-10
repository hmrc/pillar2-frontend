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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.btn.BTNConfirmationView

class BTNConfirmationViewSpec extends ViewSpecBase {

  lazy val submissionDate:            String              = "10 November 2024"
  lazy val accountingPeriodStartDate: String              = "11 November 2024"
  lazy val companyName:               String              = "Test Company"
  lazy val page:                      BTNConfirmationView = inject[BTNConfirmationView]
  lazy val pageTitle:                 String              = "Below-Threshold Notification successful"

  def groupView: Document =
    Jsoup.parse(page(Some(companyName), submissionDate, accountingPeriodStartDate, isAgent = false)(request, appConfig, messages).toString())
  def agentView: Document =
    Jsoup.parse(page(Some(companyName), submissionDate, accountingPeriodStartDate, isAgent = true)(request, appConfig, messages).toString())

  "BTNConfirmationView" should {

    "have a title" in {
      groupView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = groupView.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a banner with a link to the Homepage" in {
      val className: String = "govuk-header__link govuk-header__service-name"
      groupView.getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url
      agentView.getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url
    }

    "have an H2 heading" in {
      val h2Elements: Elements = groupView.getElementsByTag("h2")
      h2Elements.get(0).text() mustBe "What happens next"
      h2Elements.get(0).hasClass("govuk-heading-m") mustBe true
    }

    "have no back link" in {
      groupView.getElementsByClass("govuk-back-link").size mustBe 0
    }

    "have paragraph content and a link when in a group flow" in {
      val paragraphs: Elements = groupView.getElementsByClass("govuk-body")

      paragraphs.get(0).text() mustBe s"You have submitted a Below-Threshold Notification on $submissionDate."
      paragraphs.get(1).text() mustBe
        s"This is effective from the start of the accounting period you selected, $accountingPeriodStartDate."
      paragraphs.get(2).text() mustBe "The Below-Threshold Notification satisfies the group’s obligation to submit " +
        "a UK Tax Return for the current and future accounting periods. HMRC will not expect to receive an " +
        "information return while the group remains below-threshold."
      paragraphs.get(3).text() mustBe
        "The group must submit a UK Tax Return if your group meets the threshold conditions in the future."

      paragraphs.get(4).getElementsByTag("a").text() mustBe "Back to group’s homepage"
      paragraphs.get(4).getElementsByTag("a").attr("href") mustBe controllers.routes.DashboardController.onPageLoad.url
    }

    "have paragraph content (containing company name) and a link when in an agent flow" in {
      val paragraphs: Elements = agentView.getElementsByClass("govuk-body")

      paragraphs.get(0).text() mustBe
        s"You have submitted a Below-Threshold Notification for $companyName on $submissionDate."
      paragraphs.get(1).text() mustBe
        s"This is effective from the start of the accounting period you selected, $accountingPeriodStartDate."
      paragraphs.get(2).text() mustBe "The Below-Threshold Notification satisfies the group’s obligation to submit " +
        "a UK Tax Return for the current and future accounting periods. HMRC will not expect to receive an " +
        "information return while the group remains below-threshold."
      paragraphs.get(3).text() mustBe
        "The group must submit a UK Tax Return if your group meets the threshold conditions in the future."

      paragraphs.get(4).getElementsByTag("a").text() mustBe "Back to group’s homepage"
      paragraphs.get(4).getElementsByTag("a").attr("href") mustBe controllers.routes.DashboardController.onPageLoad.url
    }
  }
}
