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
import models.{NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import pages.{RfmPillar2ReferencePage, RfmRegistrationDatePage}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.RfmRegistrationDateSummary.dateHelper
import viewmodels.checkAnswers.{RfmRegistrationDateSummary, RfmSecurityCheckSummary}
import viewmodels.govuk.summarylist._
import views.html.rfm.SecurityQuestionsCheckYourAnswersView

class SecurityQuestionsCheckYourAnswersViewSpec extends ViewSpecBase {

  lazy val plrReference: String = "XE1111123456789"

  lazy val userAnswer: UserAnswers = emptyUserAnswers
    .setOrException(RfmPillar2ReferencePage, plrReference)
    .setOrException(RfmRegistrationDatePage, registrationDate)

  lazy val list: SummaryList = SummaryListViewModel(
    rows = Seq(
      RfmSecurityCheckSummary.row(userAnswer)(messages),
      RfmRegistrationDateSummary.row(userAnswer)(messages)
    ).flatten
  )

  lazy val page:      SecurityQuestionsCheckYourAnswersView = inject[SecurityQuestionsCheckYourAnswersView]
  lazy val view:      Document                              = Jsoup.parse(page(NormalMode, list)(request, appConfig, messages).toString())
  lazy val pageTitle: String                                = "Check Your Answers"

  "Security Questions Check Your Answers View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Replace filing member")
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a summary list keys" in {
      view.getElementsByClass("govuk-summary-list__key").get(0).text must include("Pillar 2 Top-up Taxes ID")
      view.getElementsByClass("govuk-summary-list__key").get(1).text must include("Registration date")
    }

    "have a summary list items" in {
      view.getElementsByClass("govuk-summary-list__value").get(0).text must include(plrReference)
      view.getElementsByClass("govuk-summary-list__value").get(1).text must include(dateHelper.formatDateGDS(registrationDate))
    }

    "have a summary list links" in {
      view.getElementsByClass("govuk-summary-list__actions").get(0).text must include("Change")
      view.getElementsByClass("govuk-summary-list__actions").get(1).text must include("Change")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Confirm and continue")
    }
  }

}
