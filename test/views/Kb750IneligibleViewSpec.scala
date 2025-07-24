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
import views.html.Kb750IneligibleView

class Kb750IneligibleViewSpec extends ViewSpecBase {

  val page: Kb750IneligibleView = inject[Kb750IneligibleView]

  val view: Document = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Kb750 Ineligible View" should {

    "have a title" in {
      view.title() mustBe "Based on your answers, this group does not need to report Pillar 2 Top-up Taxes - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(
        "Based on your answers, this group does not need to report Pillar 2 Top-up Taxes"
      )
    }

    "have paragraph content" in {
      view.getElementsByClass("govuk-body").get(0).text must include(
        "Pillar 2 Top-up Taxes apply to groups that have consolidated global revenues of €750 million or more in at least 2 of the previous 4 accounting periods."
      )

      view.getElementsByClass("govuk-body").get(1).text must include(
        "You may need to report Pillar 2 Top-up Taxes if the global turnover meets the €750 million threshold in future accounting periods."
      )
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")
      link.text         must include("Find out more about who is eligible for Pillar 2 Top-up Taxes")
      link.attr("href") must include(appConfig.startPagePillar2Url)
    }

  }

}
