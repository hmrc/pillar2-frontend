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

package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.IncompleteSubscriptionDataView

class IncompleteSubscriptionDataViewSpec extends ViewSpecBase {

  lazy val page:      IncompleteSubscriptionDataView = inject[IncompleteSubscriptionDataView]
  lazy val view:      Document                       = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle: String                         = "You have one or more incomplete registration tasks"

  "Incomplete Subscription Data view" should {
    "have a title" in {
      view
        .title() mustBe s"Register your group - Report Pillar 2 Top-up Taxes - GOV.UK" //TODO: Different title/H1 - raising a ticket to resolve later
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a paragraph with a link" in {
      val paragraph: Element = view.getElementsByClass("govuk-body").first()

      paragraph.text mustBe "You must go back to register your group and complete any in progress tasks."
      paragraph.getElementsByTag("a").text() mustBe "go back to register your group and complete any in progress tasks."
      paragraph.getElementsByTag("a").attr("href") mustBe
        controllers.routes.TaskListController.onPageLoad.url
    }

  }

}
