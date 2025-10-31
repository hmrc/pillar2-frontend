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
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.rfm.CheckNewFilingMemberView

class CheckNewFilingMemberViewSpec extends ViewSpecBase {

  lazy val page:      CheckNewFilingMemberView = inject[CheckNewFilingMemberView]
  lazy val view:      Document                 = Jsoup.parse(page(NormalMode)(request, appConfig, messages).toString())
  lazy val pageTitle: String                   = "We need to match the details of the new nominated filing member to HMRC records"

  "Check New Filing Member View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a caption" in {
      view.getElementById("section-header").text mustBe "Group details"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a paragraph body" in {
      val paragraphs: Elements = view.getElementsByClass("govuk-body")
      paragraphs.get(0).text mustBe
        "If the new filing member is registered in the UK, we will ask you for identifying information so we can " +
        "best match it with our records."

      paragraphs.get(1).text mustBe
        "If the new filing member is registered outside of the UK or if they are not a listed entity type, " +
        "we will ask you for identifying information so we can create a new HMRC record."
    }

    "have a 'Continue' link-button" in {
      val continueButton: Element = view.getElementsByClass("govuk-button").first()

      continueButton.text mustBe "Continue"
      continueButton.attr("href") mustBe controllers.rfm.routes.UkBasedFilingMemberController.onPageLoad(NormalMode).url
      continueButton.attr("role") mustBe "button"
    }
  }
}
