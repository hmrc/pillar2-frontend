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
import views.html.rfm.RfmSaveProgressInformView

class RfmSaveProgressViewSpec extends ViewSpecBase {

  val page: RfmSaveProgressInformView = inject[RfmSaveProgressInformView]

  val view: Document = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Rfm Save Progress inform View" should {

    "have a title" in {
      view.title() mustBe "Saving progress - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "Saving progress"
    }

    "have a p1 " in {
      view.getElementById("save-p1").text must include(
        "From this point, the information you enter will be saved as you progress. If you sign out," +
          " the information you have already entered will be saved for 28 days. After that time you will need to enter all of the information again."
      )
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Continue")
    }

  }

}
