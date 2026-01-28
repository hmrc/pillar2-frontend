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
import views.html.KbUKIneligibleView

class KbUKIneligibleViewSpec extends ViewSpecBase {

  lazy val page:      KbUKIneligibleView = inject[KbUKIneligibleView]
  lazy val view:      Document           = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle: String             = "Based on your answers, this group does not need to report Pillar 2 Top-up Taxes in the UK"

  "KbUK Ineligible View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a banner with a link to pillar 2 guidance" in {
      val className: String = "govuk-header__link govuk-header__service-name"
      view.getElementsByClass(className).attr("href") mustBe "https://www.gov.uk/guidance/report-pillar-2-top-up-taxes"
    }

    "have paragraph content" in {
      val paragraphs = view.getElementsByClass("govuk-body")

      paragraphs.get(0).text mustBe
        "Pillar 2 Top-up Taxes may be collected when you have an entity located in the UK."
      paragraphs.get(1).text mustBe
        "If your group members are only located outside the UK, you should check where any liability may apply."
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")
      link.text mustBe "Find out more about who is eligible for Pillar 2 Top-up Taxes"
      link.attr("href") mustBe appConfig.plr2RegistrationGuidanceUrl
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", view)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }

}
