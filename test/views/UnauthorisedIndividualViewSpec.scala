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
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.UnauthorisedIndividualView

class UnauthorisedIndividualViewSpec extends ViewSpecBase {

  lazy val page:      UnauthorisedIndividualView = inject[UnauthorisedIndividualView]
  lazy val view:      Document                   = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle: String                     = "Register your group"

  "Unauthorised Individual View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "Sorry, you’re unable to use this service"
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").get(0).text mustBe
        "You’ve signed in with an individual account. Only users with an organisation account can register to use this service."
    }

    "have a paragraph with link" in {
      val paragraphLink = view.getElementsByClass("govuk-body").get(1).select("a")
      paragraphLink.text mustBe "If the group still needs to register, sign in to Government Gateway with an organisation account."
      paragraphLink.attr("href") mustBe appConfig.loginUrl
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")
      link.text mustBe "Find out more about who can use this service"
      link.attr("href") mustBe appConfig.plr2RegistrationGuidanceUrl
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", view)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }

}
