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
import forms.BTNEntitiesInUKOnlyFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.btn.BTNEntitiesInUKOnlyView

class BTNEntitiesInUKOnlyViewSpec extends ViewSpecBase {
  lazy val plrReference: String                          = "XMPLR0123456789"
  lazy val formProvider: BTNEntitiesInUKOnlyFormProvider = new BTNEntitiesInUKOnlyFormProvider
  lazy val page:         BTNEntitiesInUKOnlyView         = inject[BTNEntitiesInUKOnlyView]
  lazy val pageTitle:    String                          = "Does the group still have entities located only in the UK?"

  lazy val organisationView: Document =
    Jsoup.parse(page(formProvider(), plrReference, isAgent = false, Some("orgName"), NormalMode)(request, appConfig, messages).toString())

  lazy val agentView: Document =
    Jsoup.parse(page(formProvider(), plrReference, isAgent = true, Some("orgName"), NormalMode)(request, appConfig, messages).toString())

  lazy val agentNoOrgView: Document =
    Jsoup.parse(page(formProvider(), plrReference, isAgent = true, organisationName = None, NormalMode)(request, appConfig, messages).toString())

  "BTNEntitiesInUKOnlyView" should {
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
      agentView.getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad().url
    }

    "have radio items" in {
      val radioButtons: Elements = organisationView.getElementsByClass("govuk-label govuk-radios__label")

      radioButtons.size() mustBe 2
      radioButtons.get(0).text mustBe "Yes"
      radioButtons.get(1).text mustBe "No"
    }

    "have a 'Continue' button" in {
      val continueButton: Element = organisationView.getElementsByClass("govuk-button").first()
      continueButton.text mustBe "Continue"
      continueButton.attr("type") mustBe "submit"
    }

    "have a caption for agent view" in {
      agentView.getElementsByClass("govuk-caption-m").text mustBe "Group: orgName ID: XMPLR0123456789"
      agentNoOrgView.getElementsByClass("govuk-caption-m").text mustBe "ID: XMPLR0123456789"
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("organisationView", organisationView),
        ViewScenario("agentView", agentView),
        ViewScenario("agentNoOrgView", agentNoOrgView)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
