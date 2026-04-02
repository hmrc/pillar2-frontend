/*
 * Copyright 2025 HM Revenue & Customs
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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.btn.BTNUnderEnquiryWarningView

class BTNUnderEnquiryWarningViewSpec extends ViewSpecBase {

  lazy val page:         BTNUnderEnquiryWarningView = inject[BTNUnderEnquiryWarningView]
  lazy val plrReference: String                     = "XMPLR0123456789"
  lazy val pageTitle:    String                     = "You have one or more returns under enquiry"

  lazy val organisationView: Document = Jsoup.parse(page(plrReference, isAgent = false, Some("orgName"))(request, appConfig, messages).toString())
  lazy val agentView:        Document = Jsoup.parse(page(plrReference, isAgent = true, Some("orgName"))(request, appConfig, messages).toString())
  lazy val agentNoOrgView:   Document =
    Jsoup.parse(page(plrReference, isAgent = true, organisationName = None)(request, appConfig, messages).toString())

  "BTNUnderEnquiryWarningView" should {

    "have a title" in {
      organisationView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = organisationView.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have paragraph content" in {
      val paragraphs: Elements = organisationView.getElementsByClass("govuk-body")
      paragraphs.get(0).text mustBe "You cannot add a Below-Threshold Notification to an accounting period that is currently under enquiry."
      paragraphs
        .get(1)
        .text mustBe "If you continue, the Below-Threshold Notification will still be processed but will not apply to any accounting periods under enquiry."
    }

    "have a continue button" in {
      organisationView.getElementsByClass("govuk-button").text mustBe "Continue"
    }

    "have a return to homepage link" in {
      val link = organisationView.select("a:contains(Return to home page)")
      link.size() mustBe 1
      link.attr("href") mustBe routes.HomepageController.onPageLoad().url
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
