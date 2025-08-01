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
import models.subscription.AccountingPeriod
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.subscriptionview.GroupAccountingPeriodView

import java.time.LocalDate

class GroupAccountingPeriodViewSpec extends ViewSpecBase {

  val formProvider = new GroupAccountingPeriodFormProvider
  val page: GroupAccountingPeriodView = inject[GroupAccountingPeriodView]

  val view: Document =
    Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())

  "GroupAccountingPeriodView" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("When did the group\u2019s first accounting period start and end after 31 December 2023?")
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Group details"
    }

    "have a heading" in {
      view.getElementsByTag("h1").get(0).text mustBe "When did the group\u2019s first accounting period start and end after 31 December 2023?"
    }

    "have the following paragraph content" in {
      view.getElementsByClass("govuk-body").get(0).text mustBe
        "This is the first accounting period the group uses for their consolidated financial statements, following the implementation of Pillar 2 Top-up Taxes in the UK, on or after 31 December 2023."
    }

    "have start and end date legends" in {
      view.getElementsByClass("govuk-fieldset__legend").get(0).text mustBe "Start date"
      view.getElementById("startDate-hint").text mustBe "For example 27 3 2024"
      view.getElementsByClass("govuk-date-input__item").get(0).text mustBe "Day"
      Option(view.getElementById("startDate.day")) mustBe defined
      view.getElementsByClass("govuk-date-input__item").get(1).text mustBe "Month"
      Option(view.getElementById("startDate.month")) mustBe defined
      view.getElementsByClass("govuk-date-input__item").get(2).text mustBe "Year"
      Option(view.getElementById("startDate.year")) mustBe defined

      view.getElementsByClass("govuk-fieldset__legend").get(1).text mustBe "End date"
      view.getElementById("endDate-hint").text mustBe "For example 28 3 2025"
      view.getElementsByClass("govuk-date-input__item").get(3).text mustBe "Day"
      Option(view.getElementById("endDate.day")) mustBe defined
      view.getElementsByClass("govuk-date-input__item").get(4).text mustBe "Month"
      Option(view.getElementById("endDate.month")) mustBe defined
      view.getElementsByClass("govuk-date-input__item").get(5).text mustBe "Year"
      Option(view.getElementById("endDate.year")) mustBe defined
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Save and continue"
    }

    "show pre-populated values when form is filled with existing data" in {
      val accountingPeriod = AccountingPeriod(LocalDate.of(2024, 1, 15), LocalDate.of(2025, 1, 15))
      val filledForm       = formProvider().fill(accountingPeriod)
      val viewWithData     = Jsoup.parse(page(filledForm, NormalMode)(request, appConfig, messages).toString())

      viewWithData.getElementById("startDate.day").attr("value") mustBe "15"
      viewWithData.getElementById("startDate.month").attr("value") mustBe "1"
      viewWithData.getElementById("startDate.year").attr("value") mustBe "2024"
      viewWithData.getElementById("endDate.day").attr("value") mustBe "15"
      viewWithData.getElementById("endDate.month").attr("value") mustBe "1"
      viewWithData.getElementById("endDate.year").attr("value") mustBe "2025"
    }
  }
}
