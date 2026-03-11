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

package views.subscriptionview.manageAccount

import base.ViewSpecBase
import controllers.routes
import forms.NewAccountingPeriodFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.subscriptionview.manageAccount.NewAccountingPeriodView

class NewAccountingPeriodViewSpec extends ViewSpecBase {

  lazy val formProvider: NewAccountingPeriodFormProvider = new NewAccountingPeriodFormProvider
  lazy val page:         NewAccountingPeriodView         = inject[NewAccountingPeriodView]
  lazy val pageTitle:    String                            = "What is the group’s new accounting period?"
  lazy val plrReference: String = "XMPLR0123456789"

  def view(isAgent: Boolean = false, orgName: Option[String] = None): Document =
    Jsoup.parse(page(formProvider(None, None), isAgent, orgName, plrReference, NormalMode)(request, appConfig, messages).toString())

  lazy val organisationView: Document = view()
  lazy val agentView:        Document = view(isAgent = true, orgName = Some("orgName"))
  lazy val agentViewNoOrg:   Document = view(isAgent = true)

  "NewAccountingPeriodView" when {
    "it's an organisation" must {
      "have a title" in {
        organisationView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = organisationView.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have a banner with a link to the Homepage" in {
        val className: String = "govuk-header__link govuk-header__service-name"
        organisationView.getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad().url
      }

      "have inset text" in {
        organisationView.getElementsByClass("govuk-inset-text").text mustBe
          "You are changing accounting period: STATIC 28 September 2021 to 27 September 2022"
        organisationView.getElementsByClass("govuk-inset-text").html must include("<br>")
      }

      "have the following paragraph content" in {
        organisationView.getElementsByClass("govuk-body").get(0).text mustBe
          "The accounting period is the period covered by the consolidated financial statements of the Ultimate Parent Entity."

        organisationView.getElementsByClass("govuk-body").get(1).text mustBe
          "Accounting periods are usually 12 months, but can be longer or shorter."

      }

      "have start and end date legends" in {
        val datesFieldsets:    Elements = organisationView.getElementsByClass("govuk-fieldset")
        val startDateFieldset: Element  = datesFieldsets.get(0)
        val endDateFieldset:   Element  = datesFieldsets.get(1)

        startDateFieldset.getElementsByClass("govuk-fieldset__legend").text mustBe "Start date"
        startDateFieldset.getElementById("startDate-hint").text mustBe "Enter a date after STATIC 28 September 2021, for example 29 05 25"

        startDateFieldset.getElementsByClass("govuk-date-input__item").get(0).text mustBe "Day"
        Option(startDateFieldset.getElementById("startDate.day")) mustBe defined
        startDateFieldset.getElementsByClass("govuk-date-input__item").get(1).text mustBe "Month"
        Option(startDateFieldset.getElementById("startDate.month")) mustBe defined
        startDateFieldset.getElementsByClass("govuk-date-input__item").get(2).text mustBe "Year"
        Option(startDateFieldset.getElementById("startDate.year")) mustBe defined

        endDateFieldset.getElementsByClass("govuk-fieldset__legend").text mustBe "End date"
        endDateFieldset.getElementById("endDate-hint").text mustBe "Enter a date before STATIC 27 September 2022, for example 26 03 26"

        endDateFieldset.getElementsByClass("govuk-date-input__item").get(0).text mustBe "Day"
        Option(endDateFieldset.getElementById("endDate.day")) mustBe defined
        endDateFieldset.getElementsByClass("govuk-date-input__item").get(1).text mustBe "Month"
        Option(endDateFieldset.getElementById("endDate.month")) mustBe defined
        endDateFieldset.getElementsByClass("govuk-date-input__item").get(2).text mustBe "Year"
        Option(endDateFieldset.getElementById("endDate.year")) mustBe defined
      }

      "have a 'Continue' button" in {
        organisationView.getElementsByClass("govuk-button").text mustBe "Continue"
      }
    }

    "it's an agent" must {
      "have a title" in {
        agentView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a caption" in {
        agentView.getElementsByClass("govuk-caption-m").text mustBe "Group: orgName ID: XMPLR0123456789"
        agentViewNoOrg.getElementsByClass("govuk-caption-m") mustBe empty
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = agentView.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have a banner with a link to the Homepage" in {
        val className: String = "govuk-header__link govuk-header__service-name"
        agentView.getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad().url
      }

      "have the following paragraph content" in {
        agentView.getElementsByClass("govuk-body").get(0).text mustBe
          "The accounting period is the period covered by the consolidated financial statements of the Ultimate Parent Entity."

        agentView.getElementsByClass("govuk-body").get(1).text mustBe
          "Accounting periods are usually 12 months, but can be longer or shorter."
      }

      "have start and end date legends" in {
        val datesFieldsets:    Elements = agentView.getElementsByClass("govuk-fieldset")
        val startDateFieldset: Element  = datesFieldsets.get(0)
        val endDateFieldset:   Element  = datesFieldsets.get(1)

        startDateFieldset.getElementsByClass("govuk-fieldset__legend").text mustBe "Start date"
        startDateFieldset.getElementById("startDate-hint").text mustBe "Enter a date after STATIC 28 September 2021, for example 29 05 25"

        startDateFieldset.getElementsByClass("govuk-date-input__item").get(0).text mustBe "Day"
        Option(startDateFieldset.getElementById("startDate.day")) mustBe defined
        startDateFieldset.getElementsByClass("govuk-date-input__item").get(1).text mustBe "Month"
        Option(startDateFieldset.getElementById("startDate.month")) mustBe defined
        startDateFieldset.getElementsByClass("govuk-date-input__item").get(2).text mustBe "Year"
        Option(startDateFieldset.getElementById("startDate.year")) mustBe defined

        endDateFieldset.getElementsByClass("govuk-fieldset__legend").text mustBe "End date"
        endDateFieldset.getElementById("endDate-hint").text mustBe "Enter a date before STATIC 27 September 2022, for example 26 03 26"

        endDateFieldset.getElementsByClass("govuk-date-input__item").get(0).text mustBe "Day"
        Option(endDateFieldset.getElementById("endDate.day")) mustBe defined
        endDateFieldset.getElementsByClass("govuk-date-input__item").get(1).text mustBe "Month"
        Option(endDateFieldset.getElementById("endDate.month")) mustBe defined
        endDateFieldset.getElementsByClass("govuk-date-input__item").get(2).text mustBe "Year"
        Option(endDateFieldset.getElementById("endDate.year")) mustBe defined
      }

      "have a 'Continue' button" in {
        agentView.getElementsByClass("govuk-button").text mustBe "Continue"
      }
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", organisationView),
        ViewScenario("agentView", agentView),
        ViewScenario("agentViewNoOrg", agentViewNoOrg)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
