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

package views.fmview

import base.ViewSpecBase
import helpers.SubscriptionLocalDataFixture
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import viewmodels.govuk.all.SummaryListViewModel
import views.behaviours.ViewScenario
import views.html.fmview.FilingMemberCheckYourAnswersView

class FilingMemberCheckYourAnswersViewSpec extends ViewSpecBase with SubscriptionLocalDataFixture {

  lazy val page:      FilingMemberCheckYourAnswersView = inject[FilingMemberCheckYourAnswersView]
  lazy val pageTitle: String                           = "Check your answers for filing member details"
  lazy val view:      Document                         = Jsoup.parse(page(SummaryListViewModel(Seq.empty))(request, appConfig, messages).toString())

  "FilingMemberCheckYourAnswersView" should {
    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Group details"
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Confirm and continue"
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", view)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
