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
import views.html.rfm.AlreadyEnrolledView

class AlreadyEnrolledViewSpec extends ViewSpecBase {

  lazy val page:      AlreadyEnrolledView = inject[AlreadyEnrolledView]
  lazy val view:      Document            = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle: String              = "You cannot replace the current filing member for this group"

  "Already Enrolled View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have paragraph contents" in {
      val paragraphs = view.getElementsByClass("govuk-body")
      paragraphs.get(0).text mustBe
        "The Government Gateway user ID you entered as the replacement is currently registered as the nominated filing member for this group."
      paragraphs.get(1).text mustBe
        "To replace the nominated filing member for this group, the new nominated filing member will need to try again with their Government Gateway user ID."
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")
      link.text mustBe "Find out more about who can use this service"
      link.attr("href") mustBe controllers.rfm.routes.StartPageController.onPageLoad.url
    }

  }

}
