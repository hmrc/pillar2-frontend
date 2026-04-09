/*
 * Copyright 2026 HM Revenue & Customs
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

package views.subscriptionview.manageAccount

import base.ViewSpecBase
import controllers.routes
import forms.NewAccountingPeriodFormProvider
import models.NormalMode
import models.subscription.{AccountingPeriod, ChosenAccountingPeriod}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import utils.DateTimeUtils.toDateFormat
import views.behaviours.ViewScenario
import views.html.subscriptionview.manageAccount.NewAccountingPeriodView

import java.time.LocalDate

class NewAccountingPeriodViewSpec extends ViewSpecBase {

  lazy val formProvider: NewAccountingPeriodFormProvider = new NewAccountingPeriodFormProvider
  lazy val page:         NewAccountingPeriodView         = inject[NewAccountingPeriodView]
  lazy val pageTitle:    String                          = "What is the group’s new accounting period?"
  lazy val plrReference: String                          = "XMPLR0123456789"

  val startDate:        LocalDate        = LocalDate.of(2026, 3, 16)
  val endDate:          LocalDate        = LocalDate.of(2027, 3, 15)
  val accountingPeriod: AccountingPeriod = AccountingPeriod(startDate, endDate)

  val chosenAccountingPeriodData: ChosenAccountingPeriod = ChosenAccountingPeriod(
    accountingPeriod,
    None,
    None
  )

  def organisationView(chosenAccountingPeriod: ChosenAccountingPeriod = chosenAccountingPeriodData): Document =
    Jsoup.parse(
      page(formProvider(chosenAccountingPeriod), chosenAccountingPeriod, isAgent = false, Some("orgName"), plrReference, NormalMode)(
        request,
        appConfig,
        messages
      )
        .toString()
    )

  def agentView(chosenAccountingPeriod: ChosenAccountingPeriod = chosenAccountingPeriodData): Document =
    Jsoup.parse(
      page(formProvider(chosenAccountingPeriod), chosenAccountingPeriod, isAgent = true, Some("orgName"), plrReference, NormalMode)(
        request,
        appConfig,
        messages
      )
        .toString()
    )

  def agentNoOrgView(chosenAccountingPeriod: ChosenAccountingPeriod = chosenAccountingPeriodData): Document =
    Jsoup.parse(
      page(formProvider(chosenAccountingPeriod), chosenAccountingPeriod, isAgent = true, organisationName = None, plrReference, NormalMode)(
        request,
        appConfig,
        messages
      )
        .toString()
    )

  "NewAccountingPeriodView" when {
    "it's an organisation" must {
      "have a title" in {
        organisationView().title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = organisationView().getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have a banner with a link to the Homepage" in {
        val className: String = "govuk-header__link govuk-header__service-name"
        organisationView().getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad().url
      }

      "have inset text" in {
        organisationView().getElementsByClass("govuk-inset-text").text mustBe
          s"You are changing accounting period: ${startDate.toDateFormat} to ${endDate.toDateFormat}"
        organisationView().getElementsByClass("govuk-inset-text").html must include("<br>")
      }

      "have the following paragraph content" in {
        organisationView().getElementsByClass("govuk-body").get(0).text mustBe
          "The accounting period is the period covered by the consolidated financial statements of the Ultimate Parent Entity."

        organisationView().getElementsByClass("govuk-body").get(1).text mustBe
          "Accounting periods are usually 12 months, but can be longer or shorter."

      }

      "have start and end date legends" in {
        val datesFieldsets:    Elements = organisationView().getElementsByClass("govuk-fieldset")
        val startDateFieldset: Element  = datesFieldsets.get(0)
        val endDateFieldset:   Element  = datesFieldsets.get(1)

        startDateFieldset.getElementsByClass("govuk-fieldset__legend").text mustBe "Start date"
        startDateFieldset
          .getElementById("startDate-hint")
          .text mustBe s"Enter a date after 30 December 2023, for example 16 3 2026"

        startDateFieldset.getElementsByClass("govuk-date-input__item").get(0).text mustBe "Day"
        Option(startDateFieldset.getElementById("startDate.day")) mustBe defined
        startDateFieldset.getElementsByClass("govuk-date-input__item").get(1).text mustBe "Month"
        Option(startDateFieldset.getElementById("startDate.month")) mustBe defined
        startDateFieldset.getElementsByClass("govuk-date-input__item").get(2).text mustBe "Year"
        Option(startDateFieldset.getElementById("startDate.year")) mustBe defined

        endDateFieldset.getElementsByClass("govuk-fieldset__legend").text mustBe "End date"
        endDateFieldset
          .getElementById("endDate-hint")
          .text mustBe s"Enter a date, for example 15 3 2027"

        endDateFieldset.getElementsByClass("govuk-date-input__item").get(0).text mustBe "Day"
        Option(endDateFieldset.getElementById("endDate.day")) mustBe defined
        endDateFieldset.getElementsByClass("govuk-date-input__item").get(1).text mustBe "Month"
        Option(endDateFieldset.getElementById("endDate.month")) mustBe defined
        endDateFieldset.getElementsByClass("govuk-date-input__item").get(2).text mustBe "Year"
        Option(endDateFieldset.getElementById("endDate.year")) mustBe defined
      }

      "have dynamic hint text" when {
        "there are no boundaries" in {
          val chosenAccountingPeriod =
            ChosenAccountingPeriod(selectedAccountingPeriod = accountingPeriod, startDateBoundary = None, endDateBoundary = None)

          organisationView(chosenAccountingPeriod = chosenAccountingPeriod)
            .getElementById("startDate-hint")
            .text mustBe s"Enter a date after 30 December 2023, for example 16 3 2026"

          organisationView(chosenAccountingPeriod = chosenAccountingPeriod)
            .getElementById("endDate-hint")
            .text mustBe s"Enter a date, for example 15 3 2027"
        }

        "there are boundaries" in {
          val chosenAccountingPeriod =
            ChosenAccountingPeriod(
              selectedAccountingPeriod = AccountingPeriod(LocalDate.of(2024, 3, 16), LocalDate.of(2025, 3, 15)),
              startDateBoundary = Some(LocalDate.of(2024, 1, 1)),
              endDateBoundary = Some(LocalDate.of(2025, 12, 31))
            )

          organisationView(chosenAccountingPeriod = chosenAccountingPeriod)
            .getElementById("startDate-hint")
            .text mustBe s"Enter a date after 31 December 2023, for example 16 3 2024"

          organisationView(chosenAccountingPeriod = chosenAccountingPeriod)
            .getElementById("endDate-hint")
            .text mustBe s"Enter a date before 1 January 2026, for example 15 3 2025"
        }
      }

      "have a 'Continue' button" in {
        organisationView().getElementsByClass("govuk-button").text mustBe "Continue"
      }
    }

    "it's an agent" must {
      "have a title" in {
        agentView().title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a caption" in {
        agentView().getElementsByClass("govuk-caption-m").text mustBe "Group: orgName ID: XMPLR0123456789"
        agentNoOrgView().getElementsByClass("govuk-caption-m").text mustBe "ID: XMPLR0123456789"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = agentView().getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have a banner with a link to the Homepage" in {
        val className: String = "govuk-header__link govuk-header__service-name"
        agentView().getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad().url
      }

      "have the following paragraph content" in {
        agentView().getElementsByClass("govuk-body").get(0).text mustBe
          "The accounting period is the period covered by the consolidated financial statements of the Ultimate Parent Entity."

        agentView().getElementsByClass("govuk-body").get(1).text mustBe
          "Accounting periods are usually 12 months, but can be longer or shorter."
      }

      "have start and end date legends" in {
        val datesFieldsets:    Elements = agentView().getElementsByClass("govuk-fieldset")
        val startDateFieldset: Element  = datesFieldsets.get(0)
        val endDateFieldset:   Element  = datesFieldsets.get(1)

        startDateFieldset.getElementsByClass("govuk-fieldset__legend").text mustBe "Start date"
        startDateFieldset
          .getElementById("startDate-hint")
          .text mustBe s"Enter a date after 30 December 2023, for example 16 3 2026"

        startDateFieldset.getElementsByClass("govuk-date-input__item").get(0).text mustBe "Day"
        Option(startDateFieldset.getElementById("startDate.day")) mustBe defined
        startDateFieldset.getElementsByClass("govuk-date-input__item").get(1).text mustBe "Month"
        Option(startDateFieldset.getElementById("startDate.month")) mustBe defined
        startDateFieldset.getElementsByClass("govuk-date-input__item").get(2).text mustBe "Year"
        Option(startDateFieldset.getElementById("startDate.year")) mustBe defined

        endDateFieldset.getElementsByClass("govuk-fieldset__legend").text mustBe "End date"
        endDateFieldset
          .getElementById("endDate-hint")
          .text mustBe s"Enter a date, for example 15 3 2027"

        endDateFieldset.getElementsByClass("govuk-date-input__item").get(0).text mustBe "Day"
        Option(endDateFieldset.getElementById("endDate.day")) mustBe defined
        endDateFieldset.getElementsByClass("govuk-date-input__item").get(1).text mustBe "Month"
        Option(endDateFieldset.getElementById("endDate.month")) mustBe defined
        endDateFieldset.getElementsByClass("govuk-date-input__item").get(2).text mustBe "Year"
        Option(endDateFieldset.getElementById("endDate.year")) mustBe defined
      }

      "have dynamic hint text" when {
        "there are no boundaries" in {
          val chosenAccountingPeriod =
            ChosenAccountingPeriod(selectedAccountingPeriod = accountingPeriod, startDateBoundary = None, endDateBoundary = None)

          agentView(chosenAccountingPeriod = chosenAccountingPeriod)
            .getElementById("startDate-hint")
            .text mustBe s"Enter a date after 30 December 2023, for example 16 3 2026"

          agentView(chosenAccountingPeriod = chosenAccountingPeriod)
            .getElementById("endDate-hint")
            .text mustBe s"Enter a date, for example 15 3 2027"
        }

        "there are boundaries" in {
          val chosenAccountingPeriod =
            ChosenAccountingPeriod(
              selectedAccountingPeriod = AccountingPeriod(LocalDate.of(2024, 3, 16), LocalDate.of(2025, 3, 15)),
              startDateBoundary = Some(LocalDate.of(2024, 1, 1)),
              endDateBoundary = Some(LocalDate.of(2025, 12, 31))
            )

          agentView(chosenAccountingPeriod = chosenAccountingPeriod)
            .getElementById("startDate-hint")
            .text mustBe s"Enter a date after 31 December 2023, for example 16 3 2024"

          agentView(chosenAccountingPeriod = chosenAccountingPeriod)
            .getElementById("endDate-hint")
            .text mustBe s"Enter a date before 1 January 2026, for example 15 3 2025"
        }
      }

      "have a 'Continue' button" in {
        agentView().getElementsByClass("govuk-button").text mustBe "Continue"
      }
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", organisationView()),
        ViewScenario("agentView", agentView()),
        ViewScenario("agentViewNoOrg", agentNoOrgView())
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
