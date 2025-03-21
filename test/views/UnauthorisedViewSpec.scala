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
import org.jsoup.nodes.Document
import views.html.UnauthorisedView

class UnauthorisedViewSpec extends ViewSpecBase {

  val page: UnauthorisedView = inject[UnauthorisedView]

  val view: Document = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Unauthorised View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("You do not have access to this service")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("You do not have access to this service")
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").get(0).text must include(
        "You need to register to report Pillar 2 Top-up Taxes to access this page."
      )
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")
      link.text         must include("register to report Pillar 2 Top-up Taxes")
      link.attr("href") must include("/guidance/report-pillar-2-top-up-taxes")
    }

  }

}
