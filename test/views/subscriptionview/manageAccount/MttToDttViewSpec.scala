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

package views.subscriptionview.manageAccount

import base.ViewSpecBase
import controllers.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.subscriptionview.manageAccount.MttToDttView

class MttToDttViewSpec extends ViewSpecBase {

  lazy val page:      MttToDttView = inject[MttToDttView]
  lazy val pageTitle: String       = "You cannot make this change online"

  def groupView: Document =
    Jsoup.parse(page(isAgent = false)(request, appConfig, messages).toString())

  def agentView: Document =
    Jsoup.parse(page(isAgent = true)(request, appConfig, messages).toString())

  lazy val paragraphs:      Elements = groupView.getElementsByClass("govuk-body")
  lazy val agentParagraphs: Elements = agentView.getElementsByClass("govuk-body")

  "MttToDttView" when {

    "it's an organisation" must {

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
      }

      "have the following paragraph content and link" in {
        paragraphs
          .get(0)
          .text mustBe "You have requested to change your group entity locations from 'in the UK and outside the UK' (multinational) to 'only in the UK' (domestic)."
        paragraphs
          .get(1)
          .text mustBe "You cannot make this change in this service. You can request this change in writing by emailing pillar2mailbox@hmrc.gov.uk."

        val paragraphLink = paragraphs.get(1).getElementsByClass("govuk-link")
        paragraphLink.text() mustBe "pillar2mailbox@hmrc.gov.uk"
        paragraphLink.attr("href") mustBe "mailto:pillar2mailbox@hmrc.gov.uk"
      }

      "have a back to homepage link" in {
        val homepageLink = paragraphs.get(2).getElementsByClass("govuk-link")
        homepageLink.get(0).text mustBe "Back to homepage"
        homepageLink.get(0).attr("href") mustBe controllers.routes.DashboardController.onPageLoad.url
      }
    }
  }

  "it's an agent" must {

    "have a title" in {
      agentView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = agentView.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a banner with a link to the Homepage" in {
      val className: String = "govuk-header__link govuk-header__service-name"
      agentView.getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url
    }

    "have the following paragraph content and link" in {
      agentParagraphs
        .get(0)
        .text mustBe "You have requested to change the group entity locations from 'in the UK and outside the UK' (multinational) to 'only in the UK' (domestic)."
      agentParagraphs
        .get(1)
        .text mustBe "You cannot make this change in this service. You can request this change in writing by emailing pillar2mailbox@hmrc.gov.uk."

      val paragraphLink = agentParagraphs.get(1).getElementsByClass("govuk-link")
      paragraphLink.text() mustBe "pillar2mailbox@hmrc.gov.uk"
      paragraphLink.attr("href") mustBe "mailto:pillar2mailbox@hmrc.gov.uk"
    }

    "have a back to homepage link" in {
      val homepageLink = agentParagraphs.get(2).getElementsByClass("govuk-link")
      homepageLink.get(0).text mustBe "Back to homepage"
      homepageLink.get(0).attr("href") mustBe controllers.routes.DashboardController.onPageLoad.url
    }
  }
}
