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

package views.repayments

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.repayments.RepaymentsConfirmationView
import controllers.routes
import play.twirl.api.HtmlFormat
import utils.ViewHelpers

class RepaymentsConfirmationViewSpec extends ViewSpecBase {

  val page: RepaymentsConfirmationView = inject[RepaymentsConfirmationView]
  val testPillar2Ref = "XMPLR0012345674"
  val dateHelper     = new ViewHelpers()

  "Repayments confirmation view" should {
    val currentDate = HtmlFormat.escape(dateHelper.getDateTimeGMT)
    val view: Document =
      Jsoup.parse(page(currentDate.toString())(request, appConfig, messages).toString())

    "have a panel" in {
      view.getElementsByClass("govuk-panel__title").text must include("Refund request submitted")
    }

    "have a title" in {
      view.getElementsByTag("title").text must include(
        "Refund request submitted" +
          " - Report Pillar 2 top-up taxes - GOV.UK"
      )
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(
        "Refund request submitted"
      )
    }

    "have a paragraphs" in {
      view.getElementsByClass("govuk-body").text must include(
        s"You have successfully submitted your refund request on ${currentDate.toString()}"
      )
      view.getElementsByClass("govuk-body").text must include(
        "What happens next If we require more information relating to your refund request, we will get in touch."
      )
      view.getElementsByClass("govuk-body").text must include(
        "You can return to manage your Pillar 2 top-up taxes ."
      )
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")
      link.attr("href") must include(routes.DashboardController.onPageLoad.url)
      link.text         must include("manage your Pillar 2 top-up taxes")
    }

    "have the correct banner link" in {
      val link = view.getElementsByClass("govuk-header__content").last().getElementsByTag("a")
      link.attr("href") must include(routes.DashboardController.onPageLoad.url)
      link.text         must include("Report Pillar 2 top-up taxes")
    }

  }
}
