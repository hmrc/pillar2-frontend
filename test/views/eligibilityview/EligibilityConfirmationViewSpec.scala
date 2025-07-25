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

package views.eligibilityview

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.eligibilityview.EligibilityConfirmationView

class EligibilityConfirmationViewSpec extends ViewSpecBase {

  val page: EligibilityConfirmationView = inject[EligibilityConfirmationView]

  val view: Document = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Eligibility Confirmation View" should {

    "have a title" in {
      view.title() mustBe "You need to register this group to report Pillar 2 Top-up Taxes - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "You need to register this group to report Pillar 2 Top-up Taxes"
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").get(0).text must include(
        "You now need to sign in with a Government Gateway user ID associated with the filing member."
      )
    }

    "have a inset text" in {
      view.getElementsByClass("govuk-inset-text").text must include(
        "You cannot use an individual or agent Government Gateway user ID to register."
      )
    }

    "have a continue button" in {
      view.getElementsByClass("govuk-button").text must include("Continue")
    }

  }

}
