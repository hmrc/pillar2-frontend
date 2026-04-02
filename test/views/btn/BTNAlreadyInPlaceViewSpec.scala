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
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.btn.BTNAlreadyInPlaceView

class BTNAlreadyInPlaceViewSpec extends ViewSpecBase {

  lazy val page:         BTNAlreadyInPlaceView = inject[BTNAlreadyInPlaceView]
  lazy val plrReference: String                = "XMPLR0123456789"
  lazy val pageTitle:    String                = "The group has already submitted a Below-Threshold Notification for this accounting period"

  lazy val organisationView: Document = Jsoup.parse(page(plrReference, isAgent = false, Some("orgName"))(request, appConfig, messages).toString())
  lazy val agentView:        Document = Jsoup.parse(page(plrReference, isAgent = true, Some("orgName"))(request, appConfig, messages).toString())
  lazy val agentNoOrgView:   Document = Jsoup.parse(page(plrReference, isAgent = true, None)(request, appConfig, messages).toString())

  "BTNAlreadyInPlaceView" should {
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

    "have a paragraph" in {
      organisationView.getElementsByClass("govuk-body").get(0).text mustBe "You cannot submit two notifications for the same period."
    }

    "have a Return to Homepage link" in {
      val returnLink: Element = organisationView.getElementsByClass("govuk-body").last().getElementsByTag("a").first()

      returnLink.text mustBe "Return to homepage"
      returnLink.attr("href") mustBe controllers.routes.HomepageController.onPageLoad().url
      returnLink.attr("target") mustBe "_self"
    }

    "have a caption for an agent view" in {
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
