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
  lazy val pageTitle: String       = "You cannot make this change"

  lazy val view: Document =
    Jsoup.parse(page()(request, appConfig, messages).toString())

  lazy val paragraphs: Elements = view.getElementsByClass("govuk-body")

  "MneOrDomesticView" when {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a banner with a link to the Homepage" in {
      val className: String = "govuk-header__link govuk-header__service-name"
      view.getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url
    }

    "have the following paragraph content" in {
      paragraphs
        .get(0)
        .text mustBe "You have requested to change your group entity locations from 'in the UK and outside the UK' (multinational) to 'UK only' (domestic)."
      paragraphs
        .get(1)
        .text mustBe "You cannot make this change in this service. To make this change you need to de-register and re-register with the correct information."
      paragraphs.get(2).text mustBe "[COPY NEEDED - where and how to do this]" //TODO: Update with content when signed off
    }

    "have a guidance link" in {
      val guidanceLink = paragraphs.get(3).getElementsByClass("govuk-link")
      guidanceLink.get(0).text mustBe "Guidance link"
      guidanceLink.get(0).attr("href") mustBe "#" //TODO: Update with correct link when signed off
    }

    "have a back to homepage link" in {
      val homepageLink = paragraphs.get(4).getElementsByClass("govuk-link")
      homepageLink.get(0).text mustBe "Back to homepage"
      homepageLink.get(0).attr("href") mustBe controllers.routes.DashboardController.onPageLoad.url
    }
  }
}
