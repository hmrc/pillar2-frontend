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
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.rfm.RfmCannotReturnAfterConfirmationView

class RfmCannotReturnAfterConfirmationViewSpec extends ViewSpecBase {

  lazy val page:       RfmCannotReturnAfterConfirmationView = inject[RfmCannotReturnAfterConfirmationView]
  lazy val view:       Document                             = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle:  String                               = "Register your group"
  lazy val paragraphs: Elements                             = view.getElementsByClass("govuk-body")

  "Rfm Cannot Return After Confirmation View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "You cannot return, you have replaced the filing member"
    }

    "have a paragraph body" in {
      paragraphs.get(0).text mustBe
        "You have successfully replaced the filing member for your Pillar 2 Top-up Taxes account."
    }

    "have a paragraph with a link" in {
      paragraphs.get(1).text() mustBe "You can now report and manage your Pillar 2 Top-up Taxes."
      paragraphs.get(1).getElementsByTag("a").text() mustBe "report and manage your Pillar 2 Top-up Taxes"
      paragraphs.get(1).getElementsByTag("a").attr("href") mustBe controllers.routes.HomepageController.onPageLoad().url
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", view)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }

}
