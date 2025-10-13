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

package views.btn

import base.ViewSpecBase
import controllers.routes
import models.subscription.AccountingPeriod
import models.{CheckMode, UserAnswers}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import pages.{EntitiesInsideOutsideUKPage, SubAccountingPeriodPage}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.{BTNEntitiesInsideOutsideUKSummary, SubAccountingPeriodSummary}
import viewmodels.govuk.all.{FluentSummaryList, SummaryListViewModel}
import views.html.btn.CheckYourAnswersView

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CheckYourAnswersViewSpec extends ViewSpecBase {

  lazy val startDate:         LocalDate            = LocalDate.of(2024, 10, 24)
  lazy val endDate:           LocalDate            = LocalDate.of(2025, 10, 24)
  lazy val dateTimeFormatter: DateTimeFormatter    = DateTimeFormatter.ofPattern("d MMMM yyyy")
  lazy val accountingPeriod:  AccountingPeriod     = AccountingPeriod(startDate, endDate)
  lazy val page:              CheckYourAnswersView = inject[CheckYourAnswersView]
  lazy val pageTitle:         String               = "Check your answers to submit your Below-Threshold Notification"
  lazy val validBTNCyaUa: UserAnswers = UserAnswers("id")
    .setOrException(SubAccountingPeriodPage, accountingPeriod)
    .setOrException(EntitiesInsideOutsideUKPage, true)

  def summaryListCYA(multipleAccountingPeriods: Boolean = false, ukOnlyEntities: Boolean = false): SummaryList =
    SummaryListViewModel(
      rows = Seq(
        SubAccountingPeriodSummary.row(accountingPeriod, multipleAccountingPeriods = multipleAccountingPeriods),
        BTNEntitiesInsideOutsideUKSummary.row(validBTNCyaUa, ukOnly = ukOnlyEntities)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  def view(summaryList: SummaryList = summaryListCYA(), isAgent: Boolean = false): Document =
    Jsoup.parse(page(summaryList, isAgent, Some("orgName"))(request, appConfig, realMessagesApi.preferred(request)).toString())

  def getSummaryListActions(doc: Document): Elements = doc.getElementsByClass("govuk-summary-list__actions")

  "CheckYourAnswersView" must {

    "have a title" in {
      view().title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view().getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a banner with a link to the Homepage" in {
      val className: String = "govuk-header__link govuk-header__service-name"
      view().getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url
      view(isAgent = true).getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url
    }

    "have a paragraph" in {
      view().getElementsByClass("govuk-body").get(0).text mustBe "If you submit a Below-Threshold Notification for " +
        "a previous accounting period, any return you have submitted this accounting period will be removed."
    }

    "have an H2 heading" in {
      val h2Elements: Elements = view().getElementsByTag("h2")
      h2Elements.get(0).text() mustBe "Submit your Below-Threshold Notification"
      h2Elements.get(0).hasClass("govuk-heading-s") mustBe true
    }

    "have a second paragraph" in {
      view().getElementsByClass("govuk-body").get(1).text mustBe "By submitting these details, you are confirming " +
        "that the information is correct and complete to the best of your knowledge."
    }

    "have the correct summary list" when {

      "UK only entities" should {

        "have a summary list" in {
          val ukOnlyEntitiesView:  Document = view(summaryList = summaryListCYA(ukOnlyEntities = true))
          val summaryListElements: Elements = ukOnlyEntitiesView.getElementsByClass("govuk-summary-list")
          val summaryListKeys:     Elements = ukOnlyEntitiesView.getElementsByClass("govuk-summary-list__key")
          val summaryListItems:    Elements = ukOnlyEntitiesView.getElementsByClass("govuk-summary-list__value")

          summaryListElements.size() mustBe 1

          summaryListKeys.get(0).text() mustBe "Group’s accounting period"
          summaryListItems.get(0).text() mustBe s"Start date: ${startDate.format(dateTimeFormatter)} End date: ${endDate.format(dateTimeFormatter)}"

          summaryListKeys.get(1).text() mustBe "Are the entities still located only in the UK?"
          summaryListItems.get(1).text() mustBe "Yes"
        }

        "have the correct summary list actions" when {

          "single accounting period" in {
            val summaryListActions: Elements = getSummaryListActions(view(summaryList = summaryListCYA(ukOnlyEntities = true)))

            summaryListActions.get(0).text mustBe "Change are the entities still located only in the UK?"
            summaryListActions.get(0).getElementsByTag("a").attr("href") mustBe
              controllers.btn.routes.BTNEntitiesInUKOnlyController.onPageLoad(CheckMode).url
          }

          "multiple accounting periods" in {
            val summaryListActions: Elements =
              getSummaryListActions(view(summaryList = summaryListCYA(ukOnlyEntities = true, multipleAccountingPeriods = true)))

            summaryListActions.get(0).text mustBe "Change group’s accounting period"
            summaryListActions.get(0).getElementsByTag("a").attr("href") mustBe
              controllers.btn.routes.BTNChooseAccountingPeriodController.onPageLoad(CheckMode).url

            summaryListActions.get(1).text mustBe "Change are the entities still located only in the UK?"
            summaryListActions.get(1).getElementsByTag("a").attr("href") mustBe
              controllers.btn.routes.BTNEntitiesInUKOnlyController.onPageLoad(CheckMode).url
          }
        }
      }

      "when inside and outside UK entities" should {

        "have a summary list" in {
          val ukAndOtherEntities:  Document = view(summaryList = summaryListCYA(ukOnlyEntities = false))
          val summaryListElements: Elements = ukAndOtherEntities.getElementsByClass("govuk-summary-list")
          val summaryListKeys:     Elements = ukAndOtherEntities.getElementsByClass("govuk-summary-list__key")
          val summaryListItems:    Elements = ukAndOtherEntities.getElementsByClass("govuk-summary-list__value")

          summaryListElements.size() mustBe 1

          summaryListKeys.get(0).text() mustBe "Group’s accounting period"
          summaryListItems.get(0).text() mustBe s"Start date: ${startDate.format(dateTimeFormatter)} End date: ${endDate.format(dateTimeFormatter)}"

          summaryListKeys.get(1).text() mustBe "Are the entities still located in both the UK and outside the UK?"
          summaryListItems.get(1).text() mustBe "Yes"
        }

        "have a summary list actions" when {
          "single accounting period" in {
            val summaryListActions: Elements = getSummaryListActions(view())

            summaryListActions.get(0).text mustBe "Change are the entities still located in both the UK and outside the UK?"
            summaryListActions.get(0).getElementsByTag("a").attr("href") mustBe
              controllers.btn.routes.BTNEntitiesInsideOutsideUKController.onPageLoad(CheckMode).url
          }

          "multiple accounting periods" in {
            val summaryListActions: Elements = getSummaryListActions(view(summaryList = summaryListCYA(multipleAccountingPeriods = true)))

            summaryListActions.get(0).text mustBe "Change group’s accounting period"
            summaryListActions.get(0).getElementsByTag("a").attr("href") mustBe
              controllers.btn.routes.BTNChooseAccountingPeriodController.onPageLoad(CheckMode).url

            summaryListActions.get(1).text mustBe "Change are the entities still located in both the UK and outside the UK?"
            summaryListActions.get(1).getElementsByTag("a").attr("href") mustBe
              controllers.btn.routes.BTNEntitiesInsideOutsideUKController.onPageLoad(CheckMode).url
          }
        }
      }
    }

    "have a 'Confirm and submit' button" in {
      val continueButton: Element = view().getElementsByClass("govuk-button").first()
      continueButton.text mustBe "Confirm and submit"
      continueButton.attr("type") mustBe "submit"
    }

    "have a caption displaying the organisation name for an agent view" in {
      view(isAgent = true).getElementsByClass("govuk-caption-m").text mustBe "orgName"
    }
  }
}
