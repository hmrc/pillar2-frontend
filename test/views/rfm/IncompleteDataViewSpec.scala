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
import forms.AgentClientPillar2ReferenceFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.rfm.IncompleteDataView

class IncompleteDataViewSpec extends ViewSpecBase {

  val formProvider = new AgentClientPillar2ReferenceFormProvider
  val page: IncompleteDataView = inject[IncompleteDataView]

  val view: Document = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Replace filing member incomplete data view" should {
    "have a title" in {
      view.title() mustBe "You have an incomplete task - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "You have an incomplete task"
    }

    "have a link with the correct text and url" in {
      val expectedLink = "/report-pillar2-top-up-taxes/replace-filing-member/review-submit/check-answers"

      val linkExists = Option(view.getElementsByAttributeValue("href", expectedLink).first()).isDefined
      linkExists mustBe true

      view.getElementsByTag("p").text must include(
        "You can go back to replace the filing member for a Pillar 2 Top-up Taxes account to try again"
      )
    }

  }

}
