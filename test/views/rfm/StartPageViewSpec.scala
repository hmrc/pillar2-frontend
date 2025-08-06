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
import views.html.rfm.StartPageView

class StartPageViewSpec extends ViewSpecBase {

  lazy val page:      StartPageView = inject[StartPageView]
  lazy val view:      Document      = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle: String        = "Replace the filing member for a Pillar 2 Top-up Taxes account"

  "Start Page View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Replace filing member"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have sub headings" in {
      val mSubheadings: Elements = view.getElementsByClass("govuk-heading-m")

      mSubheadings.get(0).text mustBe "Tell HMRC when you have replaced your filing member"
      mSubheadings.get(1).text mustBe "Who can replace a filing member"
      mSubheadings.get(2).text mustBe "Obligations as the filing member"
      mSubheadings.get(3).text mustBe "What you will need"
      view.getElementsByClass("govuk-heading-s").get(0).text mustBe
        "By continuing you confirm you are able to act as a new filing member for your group"
    }

    "have paragraphs" in {
      val paragraphs: Elements = view.getElementsByClass("govuk-body")

      paragraphs.get(0).text mustBe
        "Use this service to replace the filing member for an existing Pillar 2 Top-up Taxes account."

      paragraphs.get(1).text mustBe
        "It is a legal requirement to replace your filing member’s details within 6 months of the change occurring " +
        "in your group."

      paragraphs.get(2).text mustBe
        "If your group has not yet registered, you will need to register to report Pillar 2 Top-up Taxes. You can " +
        "choose to nominate a filing member during registration."

      paragraphs.get(3).text mustBe
        "Only the new filing member can use this service. This can either be the Ultimate Parent Entity or another " +
        "company member which has been nominated by the Ultimate Parent Entity."

      paragraphs.get(4).text mustBe
        "As the new filing member, you will take over the obligations to:"

      paragraphs.get(5).text mustBe
        "If you fail to meet your obligations as a filing member, you may be liable for penalties."

      paragraphs.get(6).text mustBe
        "To replace the filing member, you’ll need to provide the Government Gateway user ID for the new filing member."

      paragraphs.get(7).text mustBe
        "If the new filing member is a UK limited company, or limited liability partnership, you must also provide " +
        "the company registration number, and Unique Taxpayer Reference."

      paragraphs.get(8).text mustBe
        "You’ll also need to tell us:"
    }

    "have bullet lists" in {
      val listItems: Elements = view.getElementsByTag("li")

      listItems.get(0).text mustBe
        "act as HMRC’s primary contact in relation to the group’s Pillar 2 Top-up Taxes compliance"

      listItems.get(1).text mustBe
        "submit your group’s Pillar 2 Top-up Taxes returns"

      listItems.get(2).text mustBe
        "ensure your group’s Pillar 2 Top-up Taxes account accurately reflects their records"

      listItems.get(3).text mustBe
        "the group’s Pillar 2 Top-up Taxes ID"

      listItems.get(4).text mustBe
        "the date the group first registered to report their Pillar 2 Top-up Taxes in the UK"

      listItems.get(5).text mustBe
        "contact details and preferences, for one or 2 individuals or teams in the group"

      listItems.get(6).text mustBe
        "a contact postal address for the group"

    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Confirm and continue"
    }
  }
}
