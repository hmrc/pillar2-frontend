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
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.repayments.RepaymentsErrorReturnView

class RepaymentsErrorReturnViewSpec extends ViewSpecBase {

  lazy val page:           RepaymentsErrorReturnView = inject[RepaymentsErrorReturnView]
  lazy val view:           Document                  = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val testPillar2Ref: String                    = "XMPLR0012345674"
  lazy val pageTitle:      String                    = "You cannot return, your repayment request is complete"

  "Repayments error return view" should {
    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a paragraph" in {
      view.getElementsByClass("govuk-body").text mustBe
        "You have successfully submitted your repayment request. You can return to report and manage your " +
        "Pillar 2 Top-up Taxes ."
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")
      link.attr("href") mustBe routes.HomepageController.onPageLoad().url
      link.text mustBe "report and manage your Pillar 2 Top-up Taxes"
    }

    "have the correct banner link" in {
      val link: Elements = view.getElementsByClass("govuk-header__link").last().getElementsByTag("a")
      link.attr("href") mustBe routes.HomepageController.onPageLoad().url
      link.text mustBe "Report Pillar 2 Top-up Taxes"
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", view)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
