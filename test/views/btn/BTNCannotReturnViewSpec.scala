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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.btn.BTNCannotReturnView

class BTNCannotReturnViewSpec extends ViewSpecBase {

  lazy val page:      BTNCannotReturnView = inject[BTNCannotReturnView]
  lazy val view:      Document            = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle: String              = "You have submitted a Below-Threshold Notification"

  "BTNCannotReturnView" should {

    "have a title" in {
      // FIXME: inconsistent Title and H1
      // Title: Submission successful - Report Pillar 2 Top-up Taxes - GOV.UK
      // H1: "You have submitted a Below-Threshold Notification"
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique h1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "You have submitted a Below-Threshold Notification"
    }

    "have no back link" in {
      view.getElementsByClass("govuk-back-link").size mustBe 0
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")

      link.text must include("Return to your group’s homepage")
      link.attr("href") mustEqual controllers.routes.DashboardController.onPageLoad.url
    }
  }
}
