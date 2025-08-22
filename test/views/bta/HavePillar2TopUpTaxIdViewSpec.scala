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
import views.html.bta.HavePillar2TopUpTaxIdView

class HavePillar2TopUpTaxIdViewSpec extends ViewSpecBase {

  lazy val formProvider = new HavePillar2TopUpTaxIdFormProvider
  lazy val page: HavePillar2TopUpTaxIdView = inject[HavePillar2TopUpTaxIdView]
  lazy val view: Document                  = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())

  "HavePillar2TopUpTaxIdView" should {

    "have a title" in {
      view.getElementsByTag("title").text mustBe "Do you have a Pillar 2 Top-up Taxes ID?"
    }

    "have the correct page title" in {
      view.getElementsByTag("title").text mustBe "Do you have a Pillar 2 Top-up Taxes ID? - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a heading" in {
      view.getElementsByTag("h1").text mustBe "Do you have a Pillar 2 Top-up Taxes ID?"
    }

    "have hint text" in {
      view.getElementsByClass("govuk-hint").text mustBe "This is 15 characters, for example, XMPLR0123456789."
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Continue"
    }
  }
}
