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

package views.subscriptionview

import base.ViewSpecBase
import forms.GroupAccountingPeriodFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.subscriptionview.GroupAccountingPeriodView

class GroupAccountingPeriodViewSpec extends ViewSpecBase {

  lazy val formProvider: GroupAccountingPeriodFormProvider = new GroupAccountingPeriodFormProvider
  lazy val page:         GroupAccountingPeriodView         = inject[GroupAccountingPeriodView]
  lazy val view:         Document                          = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())
  lazy val pageTitle:    String                            = "When did the group’s first accounting period start and end after 31 December 2023?"

  "GroupAccountingPeriodView" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Group details"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have the following paragraph content" in {
      view.getElementsByClass("govuk-body").get(0).text mustBe "This is the first accounting period the group uses " +
        "for their consolidated financial statements, following the implementation of Pillar 2 Top-up Taxes in " +
        "the UK, on or after 31 December 2023."
    }

    "have start and end date legends" in {
      val datesFieldsets:    Elements = view.getElementsByClass("govuk-fieldset")
      val startDateFieldset: Element  = datesFieldsets.get(0)
      val endDateFieldset:   Element  = datesFieldsets.get(1)

      startDateFieldset.getElementsByClass("govuk-fieldset__legend").text mustBe "Start date"
      startDateFieldset.getElementById("startDate-hint").text mustBe "For example 27 3 2024"

      startDateFieldset.getElementsByClass("govuk-date-input__item").get(0).text mustBe "Day"
      Option(startDateFieldset.getElementById("startDate.day")) mustBe defined
      startDateFieldset.getElementsByClass("govuk-date-input__item").get(1).text mustBe "Month"
      Option(startDateFieldset.getElementById("startDate.month")) mustBe defined
      startDateFieldset.getElementsByClass("govuk-date-input__item").get(2).text mustBe "Year"
      Option(startDateFieldset.getElementById("startDate.year")) mustBe defined

      endDateFieldset.getElementsByClass("govuk-fieldset__legend").text mustBe "End date"
      endDateFieldset.getElementById("endDate-hint").text mustBe "For example 28 3 2025"

      endDateFieldset.getElementsByClass("govuk-date-input__item").get(0).text mustBe "Day"
      Option(endDateFieldset.getElementById("endDate.day")) mustBe defined
      endDateFieldset.getElementsByClass("govuk-date-input__item").get(1).text mustBe "Month"
      Option(endDateFieldset.getElementById("endDate.month")) mustBe defined
      endDateFieldset.getElementsByClass("govuk-date-input__item").get(2).text mustBe "Year"
      Option(endDateFieldset.getElementById("endDate.year")) mustBe defined
    }

    "have a 'Save and continue' button" in {
      val continueButton: Element = view.getElementsByClass("govuk-button").first()
      continueButton.text mustBe "Save and continue"
      continueButton.attr("type") mustBe "submit"
    }
  }
}
