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
import views.html.errors.ErrorTemplate

class ErrorTemplateSpec extends ViewSpecBase {

  lazy val page: ErrorTemplate = inject[ErrorTemplate]
  val heading = "This page can't be found"
  val message = "Please check that you have entered the correct web address"
  lazy val view: Document = Jsoup.parse(page(pageTitle = heading, heading = heading, message = message)(request, appConfig, messages).toString())

  "Error Template" should {

    "have a title" in {
      view.title() mustBe s"$heading - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe heading
    }

    "have a paragraph" in {
      view.getElementsByClass("govuk-body").get(0).text mustBe message
    }

    "have a paragraph with a link" in {
      val paragraphText: Element = view.getElementsByClass("govuk-body").get(1)

      paragraphText.text mustBe "You must return to your Pillar 2 Top-up Taxes registration and complete the required tasks."
      paragraphText.getElementsByTag("a").text() mustBe "return to your Pillar 2 Top-up Taxes registration"
      paragraphText.getElementsByTag("a").attr("href") mustBe
        controllers.routes.TaskListController.onPageLoad.url
    }

  }
}
