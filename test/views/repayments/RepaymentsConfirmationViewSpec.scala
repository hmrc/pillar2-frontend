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
import controllers.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.HtmlFormat
import utils.ViewHelpers
import views.html.repayments.RepaymentsConfirmationView

class RepaymentsConfirmationViewSpec extends ViewSpecBase {

  val page: RepaymentsConfirmationView = inject[RepaymentsConfirmationView]
  val testPillar2Ref = "XMPLR0012345674"
  val dateHelper     = new ViewHelpers()

  "Repayments confirmation view" should {
    val currentDate = HtmlFormat.escape(dateHelper.getDateTimeGMT)
    val view: Document =
      Jsoup.parse(page(currentDate.toString())(request, appConfig, messages).toString())

    "have a page title" in {
      view.title() mustBe "Repayment request submitted - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have the correct header link to Pillar 2 home" in {
      val link = view.getElementsByClass("govuk-header__content").last().getElementsByTag("a")
      link.attr("href") must include(routes.DashboardController.onPageLoad.url)
      link.text         must include("Report Pillar 2 Top-up Taxes")
    }

    "have one H1 banner-panel heading" in {
      val heading = view.getElementsByTag("h1")

      heading.size() mustBe 1
      heading.hasClass("govuk-panel__title") mustBe true
      heading.text() mustBe "Repayment request submitted"
    }

    "have a confirmation message" in {
      view.getElementsByClass("govuk-body").text must include(
        s"You have successfully submitted your repayment request on ${currentDate.toString()}."
      )
    }

    "have a 'What happens next' heading" in {
      view.getElementsByTag("h2").first().text mustBe "What happens next"
    }

    "have a paragraph" in {
      view.getElementsByClass("govuk-body").text must include(
        "We may need more information to complete the repayment. If we do, we’ll contact the relevant person or team from the information you provided."
      )
    }

    "have a return link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")
      link.attr("href") must include(routes.DashboardController.onPageLoad.url)
      link.text         must include("Back to group homepage")
    }

  }
}
