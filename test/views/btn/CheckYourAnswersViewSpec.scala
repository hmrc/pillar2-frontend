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
import models.subscription.AccountingPeriod
import models.{CheckMode, UserAnswers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import pages.{EntitiesInsideOutsideUKPage, SubAccountingPeriodPage}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.{BTNEntitiesInsideOutsideUKSummary, SubAccountingPeriodSummary}
import viewmodels.govuk.all.{FluentSummaryList, SummaryListViewModel}
import views.html.btn.CheckYourAnswersView

import java.time.LocalDate

class CheckYourAnswersViewSpec extends ViewSpecBase {

  val accountingPeriod: AccountingPeriod = AccountingPeriod(LocalDate.of(2024, 10, 24), LocalDate.of(2025, 10, 24))
  val validBTNCyaUa: UserAnswers = UserAnswers("id")
    .setOrException(SubAccountingPeriodPage, accountingPeriod)
    .setOrException(EntitiesInsideOutsideUKPage, true)

  def summaryListCYA(multipleAccountingPeriods: Boolean = false, ukOnlyEntities: Boolean = false): SummaryList = SummaryListViewModel(
    rows = Seq(
      SubAccountingPeriodSummary.row(accountingPeriod, multipleAccountingPeriods = multipleAccountingPeriods),
      BTNEntitiesInsideOutsideUKSummary.row(validBTNCyaUa, ukOnly = ukOnlyEntities)
    ).flatten
  ).withCssClass("govuk-!-margin-bottom-9")

  val page: CheckYourAnswersView = inject[CheckYourAnswersView]
  def view(summaryList: SummaryList = summaryListCYA(), isAgent: Boolean = false): Document =
    Jsoup.parse(page(summaryList, isAgent, Some("orgName"))(request, appConfig, realMessagesApi.preferred(request)).toString())

  "CheckYourAnswersView" must {

    "have a title" in {
      view().getElementsByTag("title").get(0).text mustEqual "Check Your Answers - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a H1 heading" in {
      view().getElementsByTag("h1").text mustEqual "Check your answers to submit your Below-Threshold Notification"
    }

    "have a paragraph" in {
      view().getElementsByClass("govuk-body").get(0).text mustEqual
        "If you submit a Below-Threshold Notification for a previous accounting period, any return you have submitted this accounting period will be removed."
    }

    "have a paragraph with a H2 heading" in {
      val h2Headings = view().getElementsByTag("h2")
      h2Headings.get(0).text mustEqual "Submit your Below-Threshold Notification"
      h2Headings.get(0).hasClass("govuk-heading-s") mustEqual true
      view().getElementsByClass("govuk-body").get(1).text mustEqual
        "By submitting these details, you are confirming that the information is correct and complete to the best of your knowledge."
    }

    "have the correct summary list" when {
      "UK only entities" should {

        "have a summary list keys" in {
          view(summaryList = summaryListCYA(ukOnlyEntities = true))
            .getElementsByClass("govuk-summary-list__key")
            .get(0)
            .text mustEqual "Group’s accounting period"
          view(summaryList = summaryListCYA(ukOnlyEntities = true)).getElementsByClass("govuk-summary-list__key").get(1).text mustEqual
            "Are the entities still located only in the UK?"
        }

        "have a summary list items" in {
          view(summaryList = summaryListCYA(ukOnlyEntities = true))
            .getElementsByClass("govuk-summary-list__value")
            .get(0)
            .text mustEqual "Start date: 24 October 2024 End date: 24 October 2025"
          view(summaryList = summaryListCYA(ukOnlyEntities = true)).getElementsByClass("govuk-summary-list__value").get(1).text mustEqual "Yes"
        }

        "have a summary list actions" when {
          "single accounting period" in {
            view(summaryList = summaryListCYA(ukOnlyEntities = true))
              .getElementsByClass("govuk-summary-list__actions")
              .text must not include "Change group’s accounting period"

            view(summaryList = summaryListCYA(ukOnlyEntities = true))
              .getElementsByClass("govuk-summary-list__actions")
              .get(0)
              .text mustBe "Change are the entities still located only in the UK?"

            view(summaryList = summaryListCYA(ukOnlyEntities = true))
              .getElementsByClass("govuk-summary-list__actions")
              .get(0)
              .getElementsByTag("a")
              .attr("href") must include(
              controllers.btn.routes.BTNEntitiesInUKOnlyController.onPageLoad(CheckMode).url
            )
          }

          "multiple accounting periods" in {
            view(summaryList = summaryListCYA(ukOnlyEntities = true, multipleAccountingPeriods = true))
              .getElementsByClass("govuk-summary-list__actions")
              .get(0)
              .text must include(
              "Change group’s accounting period"
            )

            view(summaryList = summaryListCYA(ukOnlyEntities = true, multipleAccountingPeriods = true))
              .getElementsByClass("govuk-summary-list__actions")
              .get(0)
              .getElementsByTag("a")
              .attr("href") must include(
              controllers.btn.routes.BTNChooseAccountingPeriodController.onPageLoad(CheckMode).url
            )

            view(summaryList = summaryListCYA(ukOnlyEntities = true, multipleAccountingPeriods = true))
              .getElementsByClass("govuk-summary-list__actions")
              .get(1)
              .text must include(
              "Change are the entities still located only in the UK?"
            )

            view(summaryList = summaryListCYA(ukOnlyEntities = true, multipleAccountingPeriods = true))
              .getElementsByClass("govuk-summary-list__actions")
              .get(1)
              .getElementsByTag("a")
              .attr("href") must include(
              controllers.btn.routes.BTNEntitiesInUKOnlyController.onPageLoad(CheckMode).url
            )
          }
        }
      }

      "when inside and outside UK entities" should {
        "have a summary list keys" in {
          view().getElementsByClass("govuk-summary-list__key").get(0).text mustEqual "Group’s accounting period"
          view().getElementsByClass("govuk-summary-list__key").get(1).text mustEqual
            "Are the entities still located in both the UK and outside the UK?"
        }

        "have a summary list items" in {
          view().getElementsByClass("govuk-summary-list__value").get(0).text mustEqual "Start date: 24 October 2024 End date: 24 October 2025"
          view().getElementsByClass("govuk-summary-list__value").get(1).text mustEqual "Yes"
        }

        "have a summary list actions" when {
          "single accounting period" in {
            view()
              .getElementsByClass("govuk-summary-list__actions")
              .text must not include "Change group’s accounting period"

            view()
              .getElementsByClass("govuk-summary-list__actions")
              .get(0)
              .text mustBe "Change are the entities still located in both the UK and outside the UK?"

            view()
              .getElementsByClass("govuk-summary-list__actions")
              .get(0)
              .getElementsByTag("a")
              .attr("href") must include(
              controllers.btn.routes.BTNEntitiesInsideOutsideUKController.onPageLoad(CheckMode).url
            )
          }

          "multiple accounting periods" in {
            view(summaryList = summaryListCYA(multipleAccountingPeriods = true))
              .getElementsByClass("govuk-summary-list__actions")
              .get(0)
              .text must include(
              "Change group’s accounting period"
            )

            view(summaryList = summaryListCYA(multipleAccountingPeriods = true))
              .getElementsByClass("govuk-summary-list__actions")
              .get(0)
              .getElementsByTag("a")
              .attr("href") must include(
              controllers.btn.routes.BTNChooseAccountingPeriodController.onPageLoad(CheckMode).url
            )

            view(summaryList = summaryListCYA(multipleAccountingPeriods = true))
              .getElementsByClass("govuk-summary-list__actions")
              .get(1)
              .text must include(
              "Change are the entities still located in both the UK and outside the UK?"
            )

            view(summaryList = summaryListCYA(multipleAccountingPeriods = true))
              .getElementsByClass("govuk-summary-list__actions")
              .get(1)
              .getElementsByTag("a")
              .attr("href") must include(
              controllers.btn.routes.BTNEntitiesInsideOutsideUKController.onPageLoad(CheckMode).url
            )
          }
        }
      }
    }

    "have a 'Confirm and submit' button" in {
      view().getElementsByClass("govuk-button").text mustEqual "Confirm and submit"
    }

    "have a caption displaying the organisation name for an agent view" in {
      view(isAgent = true).getElementsByClass("govuk-caption-m").text must include("orgName")
    }
  }
}
