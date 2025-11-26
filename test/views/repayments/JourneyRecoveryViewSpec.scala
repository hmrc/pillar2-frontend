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
import views.html.repayments.JourneyRecoveryView

class JourneyRecoveryViewSpec extends ViewSpecBase {

  lazy val page:       JourneyRecoveryView = inject[JourneyRecoveryView]
  lazy val view:       Document            = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle:  String              = "Sorry, there is a problem with the service"
  lazy val paragraphs: Elements            = view.getElementsByClass("govuk-body")

  "Repayments journey recovery view" should {

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
      view.getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad().url
    }

    "have the first paragraph with the correct text" in {
      paragraphs.get(0).text mustBe "Your answers were not saved."
    }

    "have the second paragraph with the correct text" in {
      paragraphs.get(1).text mustBe "Please try again later when the service is available."
    }

    "have a paragraph with a link" in {
      paragraphs.get(2).getElementsByTag("a").text() mustBe "Return to account homepage"
      paragraphs.get(2).getElementsByTag("a").attr("href") mustBe
        controllers.routes.HomepageController.onPageLoad().url
    }
  }
}
