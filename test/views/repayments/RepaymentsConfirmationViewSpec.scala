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

package views.repayments

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import utils.DateTimeUtils.ZonedDateTimeOps
import views.html.repayments.RepaymentsConfirmationView

import java.time.ZonedDateTime

class RepaymentsConfirmationViewSpec extends ViewSpecBase {

  lazy val page:               RepaymentsConfirmationView = inject[RepaymentsConfirmationView]
  lazy val testPillar2Ref:     String                     = "XMPLR0012345674"
  lazy val pageTitle:          String                     = "Repayment request submitted"
  lazy val currentDateTimeGMT: String                     = ZonedDateTime.now().toDateTimeGmtFormat
  lazy val view:               Document                   = Jsoup.parse(page(currentDateTimeGMT)(request, appConfig, messages).toString())
  lazy val paragraphs:         Elements                   = view.getElementsByClass("govuk-body")

  "Repayments confirmation view" should {
    "have a page title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a panel with a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
      h1Elements.hasClass("govuk-panel__title") mustBe true
    }

    "have a banner with a link to the Homepage" in {
      val headerLink: Element = view.getElementsByClass("govuk-header__content").first().getElementsByTag("a").first()

      headerLink.text mustBe "Report Pillar 2 Top-up Taxes"
      headerLink.attr("href") mustBe controllers.routes.DashboardController.onPageLoad.url
    }

    "have a confirmation message" in {
      paragraphs.get(0).text mustBe s"You have successfully submitted your repayment request on $currentDateTimeGMT."
    }

    "have a 'What happens next' heading" in {
      view.getElementsByTag("h2").first().text mustBe "What happens next"
    }

    "have a paragraph" in {
      paragraphs.get(2).text mustBe "We may need more information to complete the repayment. If we do, we’ll " +
        "contact the relevant person or team from the information you provided."
    }

    "have a return link" in {
      val link: Element = paragraphs.last().getElementsByTag("a").first()

      link.text mustBe "Back to group homepage"
      link.attr("href") mustBe controllers.routes.DashboardController.onPageLoad.url
    }

    "must have a 'Print this page' link" in {
      val printElement: Element = view.getElementById("print-this-page")
      printElement.text() mustBe "Print this page"
    }

  }
}
