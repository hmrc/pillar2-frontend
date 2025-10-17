/*
 * Copyright 2025 HM Revenue & Customs
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

package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.RegistrationInProgressView

class RegistrationInProgressViewSpec extends ViewSpecBase {
  lazy val page:   RegistrationInProgressView = inject[RegistrationInProgressView]
  lazy val plrRef: String                     = "XMPLR0012345678"

  lazy val view: Document = Jsoup.parse(page(plrRef)(request, messages, appConfig).toString())

  lazy val pageTitle:  String   = "Your registration is in progress"
  lazy val banners:    Elements = view.getElementsByClass("govuk-notification-banner")
  lazy val paragraphs: Elements = view.getElementsByTag("p")

  "RegistrationInProgressView" should {

    "have a title" in {
      view.title() mustBe "Registration in progress - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByClass("govuk-heading-l")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "Your Pillar 2 Top-up Taxes account"
    }

    "have a 'Registration in Progress' notification banner" in {
      banners.size() mustBe 1

      val banner: Element = banners.first()

      val importantLabel: Element = view.getElementsByClass("govuk-notification-banner__title").first()
      importantLabel.text() mustBe "Important"

      val bannerHeading: Element = banner.getElementsByClass("govuk-notification-banner__heading").first()
      bannerHeading.text() mustBe "Your registration is in progress"

      val bannerMessage: Element = banner.getElementsByClass("govuk-body").first()
      bannerMessage.text() mustBe "We are processing your registration. You can check this page in one hour to see your full group account homepage."
    }

    "have the correct paragraphs" in {
      paragraphs.get(2).text() mustBe "Group’s Pillar 2 Top-up Taxes ID: XMPLR0012345678"
      paragraphs.get(3).text() mustBe "Your group must submit your Pillar 2 Top-up Taxes returns no later than:"

      val listItems: Elements = view.getElementsByClass("govuk-list").get(0).getElementsByTag("li")
      listItems.get(0).text() mustBe "18 months after the last day of the group's accounting period, if the first accounting " +
        "period you reported for Pillar 2 Top-up Taxes ended after 31 December 2024"
      listItems.get(1).text() mustBe "30 June 2026, if the first accounting period you reported for Pillar 2 Top-up Taxes " +
        "ended on or before 31 December 2024"
    }

    "not have a back link" in {
      view.getElementsByClass("govuk-back-link").size() mustBe 0
    }

  }
}
