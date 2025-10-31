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
import views.html.UnauthorisedAgentView

class UnauthorisedAgentViewSpec extends ViewSpecBase {

  lazy val page:             UnauthorisedAgentView = inject[UnauthorisedAgentView]
  lazy val view:             Document              = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle:        String                = "Register your group"
  lazy val signInToAgentUrl: String                = "https://www.gov.uk/guidance/sign-in-to-your-agent-services-account"

  "Unauthorised Agent View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "display back link" in {
      view.getElementsByClass("govuk-back-link").size() mustBe 1
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "Sorry, you’re unable to use this service"
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").get(0).text mustBe
        "You’ve signed in using an agent Government Gateway user ID. Only groups can register to use this service."
    }

    "have list items with links" in {
      val listItems: Elements = view.getElementsByTag("li")
      val link1:     Element  = listItems.get(0).getElementsByTag("a").get(0)
      val link2:     Element  = listItems.get(1).getElementsByTag("a").get(0)

      link1.text mustBe "if you are an agent that has been given authorisation to report Pillar 2 Top-up Taxes " +
        "on behalf of a group, you must sign in via agent services"
      link1.attr("href") mustBe signInToAgentUrl
      //link1.attr("target") mustBe "_blank" // FIXME: should this open in new page? if yes, it should have target _blank
      //link1.attr("rel") mustBe "noopener noreferrer" // FIXME: should this open in new tab? if yes, it should have this attribute - reverse tabnabbing

      link2.text mustBe "if you need to request authorisation to report Pillar 2 Top-up Taxes, you must request " +
        "authorisation via agent services"
      link2.attr("href") mustBe signInToAgentUrl
      //link2.attr("target") mustBe "_blank" // FIXME: should this open in new page? if yes, it should have target _blank
      //link2.attr("rel") mustBe "noopener noreferrer" // FIXME: should this open in new tab? if yes, it should have this attribute - reverse tabnabbing
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")
      link.text mustBe "Find out more about who can use this service"
      link.attr("href") mustBe appConfig.plr2RegistrationGuidanceUrl
    }

  }

}
