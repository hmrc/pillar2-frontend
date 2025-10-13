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
import views.html.btn.BTNUnderEnquiryWarningView

class BTNUnderEnquiryWarningViewSpec extends ViewSpecBase {

  lazy val page:      BTNUnderEnquiryWarningView = inject[BTNUnderEnquiryWarningView]
  lazy val view:      Document                   = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle: String                     = "Accounting period under enquiry - Below Threshold Notification - GOV.UK"

  "BTNUnderEnquiryWarningView" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "You have one or more returns under enquiry"
    }

    "have paragraph content" in {
      val paragraphs: Elements = view.getElementsByClass("govuk-body")
      paragraphs.get(0).text mustBe "You cannot add a Below-Threshold Notification to an accounting period that is currently under enquiry."
      paragraphs
        .get(1)
        .text mustBe "If you continue, the Below-Threshold Notification will still be processed but will not apply to any accounting periods under enquiry."
    }

    "have a continue button" in {
      view.getElementsByClass("govuk-button").text mustBe "Continue"
    }

    "have a return to homepage link" in {
      val link = view.select("a:contains(Return to home page)")
      link.size() mustBe 1
      link.attr("href") mustBe routes.DashboardController.onPageLoad.url
    }
  }
}
