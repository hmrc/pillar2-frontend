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
import views.html.btn.BTNConfirmationView

class BTNConfirmationViewSpec extends ViewSpecBase {

  private val currentDate = "10 November 2024"
  private val startDate   = "11 November 2024"
  private val companyName = "Test Company"

  val page: BTNConfirmationView = inject[BTNConfirmationView]

  def groupView: Document = Jsoup.parse(page(Some(companyName), currentDate, startDate, false)(request, appConfig, messages).toString())
  def agentView: Document = Jsoup.parse(page(Some(companyName), currentDate, startDate, true)(request, appConfig, messages).toString())

  "BTNConfirmationView" should {

    "have a title" in {
      groupView.getElementsByTag("title").text must include("Below-Threshold Notification successful")
    }

    "have no back link" in {
      groupView.getElementsByClass("govuk-back-link").size mustBe 0
    }

    "have a h1 heading" in {
      groupView.getElementsByTag("h1").text must include("Below-Threshold Notification successful")
    }

    "have a h2 heading" in {
      groupView.getElementsByTag("h2").text() must include("What happens next")
      groupView.getElementsByTag("h2").get(0).hasClass("govuk-heading-m") mustEqual true
    }

    "in a group flow have paragraph content and a link" in {
      groupView.text() must include(
        s"You have submitted a Below-Threshold Notification on $currentDate."
      )

      groupView.text() must include(
        s"This is effective from the start of the accounting period you selected, $startDate."
      )

      groupView.text() must include(
        "The Below-Threshold Notification satisfies the group’s obligation"
      )

      groupView.text() must include(
        "The group must submit a UK Tax Return if your group meets the threshold"
      )

      groupView.text()   must include("Back to group’s homepage")
      groupView.toString must include(controllers.routes.DashboardController.onPageLoad.url)
    }

    "in an agent flow have paragraph content (containing company name) and a link" in {
      agentView.text() must include(
        s"You have submitted a Below-Threshold Notification for $companyName on $currentDate."
      )

      agentView.text() must include(
        s"This is effective from the start of the accounting period you selected, $startDate."
      )

      agentView.text()   must include("Back to group’s homepage")
      agentView.toString must include(controllers.routes.DashboardController.onPageLoad.url)
    }
  }
}
