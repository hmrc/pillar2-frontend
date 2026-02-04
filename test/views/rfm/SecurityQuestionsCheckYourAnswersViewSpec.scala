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
import models.{CheckMode, NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import pages.{RfmPillar2ReferencePage, RfmRegistrationDatePage}
import play.api.mvc.{AnyContent, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utils.DateTimeUtils.*
import viewmodels.checkAnswers.{RfmRegistrationDateSummary, RfmSecurityCheckSummary}
import viewmodels.govuk.summarylist.*
import views.behaviours.ViewScenario
import views.html.rfm.SecurityQuestionsCheckYourAnswersView

class SecurityQuestionsCheckYourAnswersViewSpec extends ViewSpecBase {

  lazy val plrReference: String = "XE1111123456789"

  lazy val userAnswer: UserAnswers = emptyUserAnswers
    .setOrException(RfmPillar2ReferencePage, plrReference)
    .setOrException(RfmRegistrationDatePage, registrationDate)

  lazy val list: SummaryList = SummaryListViewModel(
    rows = Seq(
      RfmSecurityCheckSummary.row(userAnswer)(using messages),
      RfmRegistrationDateSummary.row(userAnswer)(using messages)
    ).flatten
  )

  lazy val rfmRequest: Request[AnyContent] =
    FakeRequest("GET", controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onPageLoad(NormalMode).url).withCSRFToken
  lazy val page:      SecurityQuestionsCheckYourAnswersView = inject[SecurityQuestionsCheckYourAnswersView]
  lazy val view:      Document                              = Jsoup.parse(page(NormalMode, list)(rfmRequest, appConfig, messages).toString())
  lazy val pageTitle: String                                = "Check your answers"

  "Security Questions Check Your Answers View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a non-clickable banner" in {
      val serviceName = view.getElementsByClass("govuk-header__service-name").first()
      serviceName.text mustBe "Report Pillar 2 Top-up Taxes"
      serviceName.getElementsByTag("a") mustBe empty
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Replace filing member"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a summary list keys" in {
      val summaryListKeys: Elements = view.getElementsByClass("govuk-summary-list__key")

      summaryListKeys.get(0).text mustBe "Pillar 2 Top-up Taxes ID"
      summaryListKeys.get(1).text mustBe "Registration date"
    }

    "have a summary list items" in {
      val summaryListItems: Elements = view.getElementsByClass("govuk-summary-list__value")

      summaryListItems.get(0).text mustBe plrReference
      summaryListItems.get(1).text mustBe registrationDate.toDateFormat
    }

    "have a summary list links" in {
      val summaryListLinks: Elements = view.getElementsByClass("govuk-summary-list__actions")

      summaryListLinks.get(0).text mustBe "Change The group’s Pillar 2 Top-up Taxes ID"
      summaryListLinks.get(0).getElementsByTag("a").attr("href") mustBe
        controllers.rfm.routes.SecurityCheckController.onPageLoad(CheckMode).url

      summaryListLinks.get(1).text mustBe "Change The group’s registration date"
      summaryListLinks.get(1).getElementsByTag("a").attr("href") mustBe
        controllers.rfm.routes.GroupRegistrationDateReportController.onPageLoad(CheckMode).url
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
