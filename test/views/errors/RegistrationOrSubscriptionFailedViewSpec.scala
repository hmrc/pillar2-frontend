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

package views.errors

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.errors.RegistrationOrSubscriptionFailedView

class RegistrationOrSubscriptionFailedViewSpec extends ViewSpecBase {

  lazy val page:       RegistrationOrSubscriptionFailedView = inject[RegistrationOrSubscriptionFailedView]
  lazy val view:       Document                             = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle:  String                               = "Sorry, there is a problem with the service"
  lazy val paragraphs: Elements                             = view.getElementsByClass("govuk-body")

  "Agent Error View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have paragraph contents" in {
      paragraphs.get(0).text mustBe "You must still register, please try again later."
      paragraphs.get(1).text mustBe
        "We have saved your answers and they will be available for 28 days. After that time you will need to enter all of the information again."
    }

    "have a link" in {
      val link: Element = paragraphs.last().getElementsByTag("a").first()

      link.text mustBe "Return to registration to try again"
      link.attr("href") mustBe controllers.routes.TaskListController.onPageLoad.url
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", view)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
