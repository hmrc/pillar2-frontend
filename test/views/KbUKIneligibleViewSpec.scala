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
import views.html.KbUKIneligibleView

class KbUKIneligibleViewSpec extends ViewSpecBase {

  val page: KbUKIneligibleView = inject[KbUKIneligibleView]

  val view: Document = Jsoup.parse(page()(request, appConfig, messages).toString())

  "KbUK Ineligible View" should {

    "have a title" in {
      view.title() mustBe "Based on your answers, this group does not need to report Pillar 2 Top-up Taxes in the UK"
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(
        "Based on your answers, this group does not need to report Pillar 2 Top-up Taxes in the UK"
      )
    }

    "have paragraph content" in {
      view.getElementsByClass("govuk-body").get(0).text must include(
        "Pillar 2 Top-up Taxes may be collected when you have an entity located in the UK."
      )

      view.getElementsByClass("govuk-body").get(1).text must include(
        "If your group members are only located outside the UK, you should check where any liability may apply."
      )
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")
      link.text         must include("Find out more about who is eligible for Pillar 2 Top-up Taxes")
      link.attr("href") must include(appConfig.startPagePillar2Url)
    }

  }

}
