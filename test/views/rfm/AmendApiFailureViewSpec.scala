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
import views.html.rfm.AmendApiFailureView

class AmendApiFailureViewSpec extends ViewSpecBase {

  val page: AmendApiFailureView = inject[AmendApiFailureView]

  val view: Document = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Amend Api Failure View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("Sorry, there is a problem with the service")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("Sorry, there is a problem with the service")
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").get(0).text must include("Please try again later")
      view.getElementsByClass("govuk-body").get(1).text must
        include(
          "You can go back to replace the filing member for a Pillar 2 Top-up Taxes account to try again."
        )
    }
  }
}
