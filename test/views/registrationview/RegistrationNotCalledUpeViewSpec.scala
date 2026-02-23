/*
 * Copyright 2025 HM Revenue & Customs
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

package views.registrationview

import base.ViewSpecBase
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.registrationview.RegistrationNotCalledUpeView

class RegistrationNotCalledUpeViewSpec extends ViewSpecBase {

  lazy val page:       RegistrationNotCalledUpeView = inject[RegistrationNotCalledUpeView]
  lazy val view:       Document                     = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle:  String                       = "Sorry, there is a problem with the service"
  lazy val paragraphs: Elements                     = view.getElementsByClass("govuk-body")

  "Registration Not Called Upe View" should {

    "have a title" in {
      view.title() mustBe "Register your group - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have paragraph contents" in {
      paragraphs.get(0).text mustBe "Try again later."
      paragraphs.get(1).text mustBe "Your company details could not be confirmed."
    }

    "have a paragraph with a link" in {
      val paragraphWithLink = paragraphs.get(2)
      paragraphWithLink.text mustBe "Go back to select the entity type to try again."

      val link = paragraphWithLink.getElementsByTag("a")
      link.text mustBe "Go back to select the entity type"
      link.attr("href") mustBe controllers.registration.routes.EntityTypeController.onPageLoad(NormalMode).url
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", view)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
