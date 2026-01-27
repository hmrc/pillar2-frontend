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
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import play.api.mvc.{AnyContent, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import views.behaviours.ViewScenario
import views.html.rfm.JourneyRecoveryView

class JourneyRecoveryViewSpec extends ViewSpecBase {

  lazy val rfmRequest: Request[AnyContent] =
    FakeRequest("GET", controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url).withCSRFToken
  lazy val page:      JourneyRecoveryView = inject[JourneyRecoveryView]
  lazy val view:      Document            = Jsoup.parse(page()(rfmRequest, appConfig, messages).toString())
  lazy val pageTitle: String              = "There has been an error"

  "Replace filing member journey recovery view" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a non-clickable banner" in {
      val serviceName = view.getElementsByClass("govuk-header__service-name").first()
      serviceName.text mustBe "Report Pillar 2 Top-up Taxes"
      serviceName.getElementsByTag("a") mustBe empty
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a paragraph with a link" in {
      val paragraph: Element = view.getElementsByClass("govuk-body").first()

      paragraph.text mustBe "You can go back to replace the filing member for a Pillar 2 Top-up Taxes account to try again."
      paragraph.getElementsByTag("a").text() mustBe "to replace the filing member for a Pillar 2 Top-up Taxes account to try again"
      paragraph.getElementsByTag("a").attr("href") mustBe appConfig.rfmGuidanceUrl
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", view)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }

}
