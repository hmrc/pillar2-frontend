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

package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.AgentOrganisationErrorView

class AgentOrganisationErrorViewSpec extends ViewSpecBase {

  lazy val page:       AgentOrganisationErrorView = inject[AgentOrganisationErrorView]
  lazy val view:       Document                   = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle:  String                     = "Sorry, you’re unable to use this service"
  lazy val paragraphs: Elements                   = view.getElementsByClass("govuk-body")

  "Agent Organisation Error View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have paragraphs" in {
      paragraphs.get(0).text mustBe "You’ve signed in with an organisations account."
      paragraphs.get(1).text mustBe "Only users with an agent services account can use this service."
    }

    "have a list of links" in {
      val listItems: Elements = view.getElementsByClass("govuk-list").first().getElementsByTag("li")

      listItems.get(0).text mustBe
        "if you are an agent that has been given authorisation to report Pillar 2 Top-up Taxes on behalf of a group, you must sign in via agent services."
      listItems.get(0).getElementsByTag("a").attr("href") mustBe
        "https://www.gov.uk/guidance/sign-in-to-your-agent-services-account"
      listItems.get(0).getElementsByTag("a").attr("target") mustBe "_self"
      // FIXME: this seems wrong. A link to gov.uk should open in a new tab???

      listItems.get(1).text mustBe
        "if you need to request authorisation to report Pillar 2 Top-up Taxes, you must request authorisation on agent services."
      listItems.get(1).getElementsByTag("a").attr("href") mustBe
        "https://www.gov.uk/guidance/how-to-use-the-online-agent-authorisation-to-get-authorised-as-a-tax-agent"
      //listItems.get(1).getElementsByTag("a").attr("target") mustBe "_blank" // FIXME: should this open in new page? if yes, it should have target _blank
      //listItems.get(1).getElementsByTag("a").attr("rel") mustBe "noopener noreferrer" // FIXME: should this open in new tab? if yes, it should have this attribute - reverse tabnabbing
    }

    "have a paragraph with a link" in {
      val link: Element = paragraphs.get(4).getElementsByTag("a").get(0)

      link.text() mustBe "Find out more about who can report for Pillar 2 Top-up Taxes"
      link.attr("href") mustBe "https://www.gov.uk/guidance/report-pillar-2-top-up-taxes"
      //link.attr("target") mustBe "_blank" // FIXME: should this open in new page? if yes, it should have target _blank
      //link.attr("rel") mustBe "noopener noreferrer" // FIXME: should this open in new tab? if yes, it should have this attribute - reverse tabnabbing
    }
  }
}
