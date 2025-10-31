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
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.repayments.RequestRefundBeforeStartView

class RequestRefundBeforeStartViewSpec extends ViewSpecBase {

  lazy val page:      RequestRefundBeforeStartView = inject[RequestRefundBeforeStartView]
  lazy val view:      Document                     = Jsoup.parse(page(agentView = false)(request, appConfig, messages).toString())
  lazy val agentView: Document                     = Jsoup.parse(page(agentView = true)(request, appConfig, messages).toString())
  lazy val pageTitle: String                       = "Request a repayment"

  "Request Repayment Before Start View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a h1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a banner with a link to the Homepage" in {
      val className: String = "govuk-header__link govuk-header__service-name"
      view.getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url
      agentView.getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url
    }

    "have an H2 heading" in {
      view.getElementsByTag("h2").first().text mustBe "Before you start"
    }

    "have following contents" in {

      val listItems: Elements = view.getElementsByTag("li")

      view.getElementsByClass("govuk-body").first().text mustBe
        "You can use this service to request a repayment. You can only make a request if there are funds in your " +
        "group’s Pillar 2 account."

      agentView.getElementsByClass("govuk-body").first().text mustBe
        "You can use this service to request a repayment on behalf of your client. You can only make a request if there " +
        "are funds in your group’s Pillar 2 account."

      view.getElementsByClass("govuk-body").get(1).text mustBe "You’ll need to provide:"

      listItems.get(0).text mustBe "repayment amount"
      listItems.get(1).text mustBe "reason for your repayment request"
      listItems.get(2).text mustBe "bank account details"
      listItems.get(3).text mustBe "contact details for someone we can contact about this request"
    }

    "have a 'Request a repayment' link-button" in {
      val requestRepaymentButton: Element = view.getElementsByClass("govuk-button").first()

      requestRepaymentButton.text mustBe "Request a repayment"
      requestRepaymentButton.attr("href") mustBe controllers.repayments.routes.RequestRepaymentAmountController.onSubmit(NormalMode).url
      requestRepaymentButton.attr("role") mustBe "button"
    }

  }
}
