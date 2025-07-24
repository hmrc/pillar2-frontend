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
import org.jsoup.nodes.Document
import views.html.UnauthorisedAgentView

class UnauthorisedAgentViewSpec extends ViewSpecBase {

  val page: UnauthorisedAgentView = inject[UnauthorisedAgentView]

  val view: Document = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Unauthorised Agent View" should {

    "have a title" in {
      view.title() mustBe "Register your group"
    }

    "display back link" in {
      view.getElementsByClass("govuk-back-link").size() mustBe 1
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("Sorry, you’re unable to use this service")
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").get(0).text must include(
        "You’ve signed in using an agent Government Gateway user ID. Only groups can register to use this service."
      )
    }

    "have list items with links" in {
      val listItem1 = view.getElementsByTag("li").get(0)
      listItem1.select("p").text must include(
        "if you are an agent that has been given authorisation to report Pillar 2 Top-up Taxes on behalf of a group, you must"
      )
      listItem1.select("a").text must include(
        "sign in via agent services"
      )
      listItem1.select("a").attr("href") must include(
        "https://www.gov.uk/guidance/sign-in-to-your-agent-services-account"
      )

      val listItem2 = view.getElementsByTag("li").get(1)
      listItem2.select("p").text must include(
        "if you need to request authorisation to report Pillar 2 Top-up Taxes, you must"
      )
      listItem2.select("a").text must include(
        "request authorisation via agent services"
      )
      listItem2.select("a").attr("href") must include(
        "https://www.gov.uk/guidance/sign-in-to-your-agent-services-account"
      )
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")
      link.text         must include("Find out more about who can use this service")
      link.attr("href") must include(appConfig.startPagePillar2Url)
    }

  }

}
