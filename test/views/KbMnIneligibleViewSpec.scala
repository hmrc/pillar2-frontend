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
import views.html.KbMnIneligibleView

class KbMnIneligibleViewSpec extends ViewSpecBase {

  lazy val page:      KbMnIneligibleView = inject[KbMnIneligibleView]
  lazy val view:      Document           = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle: String             = "Based on your answers, you cannot register this group to report Pillar 2 Top-up Taxes"

  "KbMn Ineligible View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have paragraph content" in {
      view.getElementsByClass("govuk-body").get(0).text must include(
        "Only the ultimate parent or nominated filing member for an eligible group can register to report Pillar 2 Top-up Taxes."
      )

      view.getElementsByClass("govuk-body").get(1).text must include(
        "This group may still need to register."
      )
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")
      link.text         must include("Find out more about who can use this service")
      link.attr("href") must include(appConfig.startPagePillar2Url)
    }

  }

}
