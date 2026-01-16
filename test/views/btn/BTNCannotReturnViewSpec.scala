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
import controllers.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.btn.BTNCannotReturnView

class BTNCannotReturnViewSpec extends ViewSpecBase {

  lazy val page:      BTNCannotReturnView = inject[BTNCannotReturnView]
  lazy val view:      Document            = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle: String              = "You have submitted a Below-Threshold Notification"

  "BTNCannotReturnView" should {
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

    "have no back link" in {
      view.getElementsByClass("govuk-back-link").size mustBe 0
    }

    "have a Return to Group Homepage link" in {
      val link: Element = view.getElementsByClass("govuk-body").last().getElementsByTag("a").first()

      link.text mustBe "Return to your group’s homepage"
      link.attr("href") mustBe controllers.routes.HomepageController.onPageLoad().url
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", view)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
