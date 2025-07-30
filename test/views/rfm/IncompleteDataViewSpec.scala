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
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.rfm.IncompleteDataView

class IncompleteDataViewSpec extends ViewSpecBase {

  lazy val formProvider: AgentClientPillar2ReferenceFormProvider = new AgentClientPillar2ReferenceFormProvider
  lazy val page:         IncompleteDataView                      = inject[IncompleteDataView]
  lazy val view:         Document                                = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle:    String                                  = "You have an incomplete task"

  "Replace filing member incomplete data view" should {
    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a paragraph with a link" in {
      val paragraph: Element = view.getElementsByClass("govuk-body").first()

      paragraph.text mustBe "You can go back to replace the filing member for a Pillar 2 Top-up Taxes account to try again."
      paragraph.getElementsByTag("a").text() mustBe "replace the filing member for a Pillar 2 Top-up Taxes account to try again"
      paragraph.getElementsByTag("a").attr("href") mustBe
        controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url
    }

  }

}
