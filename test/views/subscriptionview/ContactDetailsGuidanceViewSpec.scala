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

package views.subscriptionview

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.subscriptionview.ContentView

class ContactDetailsGuidanceViewSpec extends ViewSpecBase {

  lazy val page: ContentView = inject[ContentView]
  lazy val view: Document = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle: String = "We need contact details for the filing member"

  "ContentView" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Contact details"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have the correct body content" in {
      val paragraphs: Elements = view.getElementsByClass("govuk-body")
      paragraphs.get(0).text mustBe "We need the contact details for the filing member of this group so we can contact the right person or team when about compliance for Pillar 2 Top-up Taxes."
      paragraphs.get(1).text mustBe "These may be different to any contact details you have already provided during this registration."
    }

    "have a continue button" in {
      view.getElementsByClass("govuk-button").text mustBe "Continue"
    }
  }
}
