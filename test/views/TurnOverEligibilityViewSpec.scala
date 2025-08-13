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
import forms.TurnOverEligibilityFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import play.api.data.Form
import views.html.TurnOverEligibilityView

class TurnOverEligibilityViewSpec extends ViewSpecBase {

  lazy val formProvider: Form[Boolean]           = new TurnOverEligibilityFormProvider()()
  lazy val page:         TurnOverEligibilityView = inject[TurnOverEligibilityView]
  lazy val view:         Document                = Jsoup.parse(page(formProvider)(request, appConfig, messages).toString())
  lazy val pageTitle: String =
    "Does the group have consolidated annual revenues of €750 million or more in at least 2 of the previous 4 accounting periods?"

  "Turn Over Eligibility View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a caption" in {
      view.getElementsByTag("h2").get(0).text mustBe "Check if you need to report Pillar 2 Top-up Taxes"
    }

    "have a legend with heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
      h1Elements.first().parent().hasClass("govuk-fieldset__legend") mustBe true
    }

    "have a banner with a link to pillar 2 guidance" in {
      val className: String = "govuk-header__link govuk-header__service-name"
      view.getElementsByClass(className).attr("href") mustBe "https://www.gov.uk/guidance/report-pillar-2-top-up-taxes"
    }

    "have a hint" in {
      view.getElementsByClass("govuk-hint").get(0).text mustBe
        "If the group’s accounting period is not 365 days, you can calculate the threshold by multiplying €750 million " +
        "by the number of days in your accounting period and dividing it by 365."
    }

    "have radio items" in {
      val radioButtons: Elements = view.getElementsByClass("govuk-label govuk-radios__label")

      radioButtons.size() mustBe 2
      radioButtons.get(0).text mustBe "Yes"
      radioButtons.get(1).text mustBe "No"
    }

    "have a 'Continue' button" in {
      val continueButton: Element = view.getElementsByClass("govuk-button").first()
      continueButton.text mustBe "Continue"
      continueButton.attr("type") mustBe "submit"
    }

  }

}
