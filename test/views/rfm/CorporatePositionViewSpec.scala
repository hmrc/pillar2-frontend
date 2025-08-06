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
import forms.RfmCorporatePositionFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.rfm.CorporatePositionView

class CorporatePositionViewSpec extends ViewSpecBase {

  lazy val formProvider: RfmCorporatePositionFormProvider = new RfmCorporatePositionFormProvider
  lazy val page:         CorporatePositionView            = inject[CorporatePositionView]
  lazy val view:         Document                         = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())
  lazy val pageTitle:    String                           = "What is your position in the corporate structure of the group?"

  "Corporate Position View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Replace filing member"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have radio items" in {
      val radioItems: Elements = view.getElementsByClass("govuk-label govuk-radios__label")
      radioItems.get(0).text mustBe "New nominated filing member"
      radioItems.get(1).text mustBe "Ultimate Parent Entity (UPE)"
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Save and continue"
    }
  }
}
