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

package views.bta

import base.ViewSpecBase
import forms.HavePillar2TopUpTaxIdFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.bta.HavePillar2TopUpTaxIdView

class HavePillar2TopUpTaxIdViewSpec extends ViewSpecBase {

  lazy val formProvider: HavePillar2TopUpTaxIdFormProvider = new HavePillar2TopUpTaxIdFormProvider
  lazy val page:         HavePillar2TopUpTaxIdView         = inject[HavePillar2TopUpTaxIdView]
  lazy val view:         Document                          = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())
  lazy val pageTitle:    String                            = "Do you have a Pillar 2 Top-up Taxes ID?"

  "HavePillar2TopUpTaxIdView" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have hint text" in {
      view.getElementsByClass("govuk-hint").text mustBe "This is 15 characters, for example, XMPLR0123456789."
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Continue"
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", view)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
