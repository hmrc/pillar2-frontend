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

package views.rfm

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.twirl.api.HtmlFormat
import utils.ViewHelpers
import views.html.rfm.RfmConfirmationView

class RfmConfirmationViewSpec extends ViewSpecBase {
  lazy val testPillar2ID: String              = "PLR2ID123"
  lazy val testDateTime:  String              = HtmlFormat.escape(ViewHelpers.getDateTimeGMT).toString
  lazy val page:          RfmConfirmationView = inject[RfmConfirmationView]
  lazy val pageTitle:     String              = "Replace filing member successful"
  lazy val paragraphs:    Elements            = view.getElementsByClass("govuk-body")

  lazy val view: Document =
    Jsoup.parse(page(testPillar2ID, testDateTime)(request, appConfig, messages).toString())

  "Rfm Confirmation View" should {
    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a panel with a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
      h1Elements.hasClass("govuk-panel__title") mustBe true
    }

    "have pillar 2 ID and date time confirmation paragraphs" in {
      paragraphs.get(0).text mustEqual s"Group Pillar 2 Top-up Taxes ID: $testPillar2ID"
      paragraphs.get(1).text mustEqual s"Your group’s filing member was replaced on $testDateTime"
    }

    "have an H2 heading for new filing member obligations" in {
      view.getElementsByTag("h2").first.text mustBe "As the new filing member, you have taken over the obligations to:"
    }

    "have a bullet list with filing member obligations" in {
      val bulletItems: Elements = view.getElementsByClass("govuk-list--bullet").select("li")

      bulletItems.get(0).text mustBe "act as HMRC’s primary contact in relation to the group’s Pillar 2 Top-up Taxes compliance"
      bulletItems.get(1).text mustBe "submit your group’s Pillar 2 Top-up Taxes returns"
      bulletItems.get(2).text mustBe "ensure your group’s Pillar 2 Top-up Taxes account accurately reflects their records."
    }

    "have paragraph for filing member obligations warning" in {
      paragraphs.get(3).text mustBe
        "If you fail to meet your obligations as a filing member, you may be liable for penalties."
    }

    "have an H2 heading for what happens next" in {
      view.getElementsByTag("h2").get(1).text mustBe "What happens next"
    }

    "have a paragraph with link" in {
      paragraphs.get(4).text mustBe "You can now report and manage your group's Pillar 2 Top-up Taxes on behalf of your group."
      val link = paragraphs.get(4).getElementsByTag("a")
      link.text mustBe "report and manage your group's Pillar 2 Top-up Taxes"
      link.attr("href") mustBe controllers.routes.DashboardController.onPageLoad.url
    }

    "display print this page link" in {
      val printLink = view.select("a:contains(Print this page)")
      printLink.size()         must be >= 1
      printLink.first().text() must include("Print this page")
    }
  }
}
