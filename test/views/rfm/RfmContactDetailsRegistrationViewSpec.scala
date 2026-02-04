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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.mvc.{AnyContent, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import views.behaviours.ViewScenario
import views.html.rfm.RfmContactDetailsRegistrationView

class RfmContactDetailsRegistrationViewSpec extends ViewSpecBase {
  lazy val page:       RfmContactDetailsRegistrationView = inject[RfmContactDetailsRegistrationView]
  lazy val pageTitle:  String                            = "We need contact details for your Pillar 2 Top-up Taxes account"
  lazy val rfmRequest: Request[AnyContent]               =
    FakeRequest("GET", controllers.rfm.routes.RfmContactDetailsRegistrationController.onPageLoad().url).withCSRFToken

  lazy val view: Document =
    Jsoup.parse(page()(rfmRequest, appConfig, messages).toString())

  "Rfm Contact Details Registration View" should {
    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a non-clickable banner" in {
      val serviceName = view.getElementsByClass("govuk-header__service-name").first()
      serviceName.text mustBe "Report Pillar 2 Top-up Taxes"
      serviceName.getElementsByTag("a") mustBe empty
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Contact details"
    }

    "have a panel with a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
      h1Elements.hasClass("govuk-heading-l") mustBe true
    }

    "have a paragraph" in {
      view.getElementsByClass("govuk-body").get(0).text mustBe
        "We need information about the filing member of this group so we can contact the right person or team when reviewing your Pillar 2 Top-up Taxes compliance."
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Continue"
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", view)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
