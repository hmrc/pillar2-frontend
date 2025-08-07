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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.btn.BTNProblemWithServiceView

class BTNProblemWithServiceViewSpec extends ViewSpecBase {

  lazy val page:      BTNProblemWithServiceView = inject[BTNProblemWithServiceView]
  lazy val view:      Document                  = Jsoup.parse(page()(request, appConfig, messages).toString)
  lazy val pageTitle: String                    = "Sorry, there is a problem with the service"

  "BTNProblemWithServiceView" should {
    // FIXME: "have the correct page title" in {
    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    // FIXME: we are using `select` here
    "display the error heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "display a try again later message" in {
      view.select("p.govuk-body").text() must include("You can try again later when the service is available.")
    }

    "have a return to home page link with the correct URL" in {
      val link = view.select("a[href*='pillar2-top-up-tax-home']").first()
      link.text() must include("Return to your account homepage to submit a Below-Threshold Notification again")
      link.attr("href") mustBe controllers.routes.DashboardController.onPageLoad.url
    }

  }
}
