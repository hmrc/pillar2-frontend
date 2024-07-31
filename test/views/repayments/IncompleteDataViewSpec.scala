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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.repayments.IncompleteDataView

class IncompleteDataViewSpec extends ViewSpecBase {

  val page: IncompleteDataView = inject[IncompleteDataView]

  val view: Document = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Repayments incomplete data view" should {
    "have a title" in {
      view.getElementsByTag("title").text must include("Refund request has missing information")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("Refund request has missing information")
    }

    "have a paragraph with the correct text" in {
      view.getElementsByTag("p").text must include("You need to")
    }

    "have a link with the correct text and url" in {
      val expectedLink = "/report-pillar2-top-up-taxes/repayment/before-you-start"
      val linkExists   = view.getElementsByAttributeValue("href", expectedLink).first() != null
      linkExists mustBe true

      view.getElementsByTag("a").text must include(
        "go back and complete all the required answers"
      )
    }

    "have a paragraph with the correct stop text" in {
      view.getElementsByTag("p").text must include("before submitting your refund request.")
    }

  }

}
