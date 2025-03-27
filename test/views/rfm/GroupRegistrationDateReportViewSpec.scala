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
import forms.GroupRegistrationDateReportFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.rfm.GroupRegistrationDateReportView

class GroupRegistrationDateReportViewSpec extends ViewSpecBase {

  val formProvider = new GroupRegistrationDateReportFormProvider
  val page: GroupRegistrationDateReportView = inject[GroupRegistrationDateReportView]

  val view: Document = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())

  "Group Registration Date Report View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include(
        "Enter the group’s registration date to the Report Pillar 2 " +
          "Top-up Taxes service"
      )
    }

    "have a caption" in {
      view.getElementById("section-header").text must include("Replace filing member")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(
        "Enter the group’s registration date to the Report Pillar 2 " +
          "Top-up Taxes service"
      )
    }

    "have a hint description" in {
      view.getElementsByClass("govuk-hint").get(0).text must include(
        "This will be the date when your group first " +
          "registered to report their Pillar 2 Top-up Taxes in the UK."
      )
    }

    "have a registration date hint" in {
      view.getElementsByClass("govuk-hint").get(1).text must include("For example, 27 3 2026")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Continue")
    }
  }
}
