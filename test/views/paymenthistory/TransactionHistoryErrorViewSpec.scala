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

package views.paymenthistory

import base.ViewSpecBase
import controllers.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.paymenthistory.TransactionHistoryErrorView

class TransactionHistoryErrorViewSpec extends ViewSpecBase {

  lazy val page:      TransactionHistoryErrorView = inject[TransactionHistoryErrorView]
  lazy val view:      Document                    = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle: String                      = "Sorry, there is a problem with the service"

  "Transaction History Error View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a banner with a link to the Homepage" in {
      val className: String = "govuk-header__link govuk-header__service-name"
      view.getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad.url
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").first().text mustBe "We cannot retrieve your details at this time."
      view.getElementsByClass("govuk-body").get(1).text mustBe "Please try again later."
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").get(2).getElementsByTag("a")

      link.text mustBe "Return to your account homepage"
      link.attr("href") mustBe routes.HomepageController.onPageLoad.url
    }

  }
}
