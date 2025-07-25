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
import views.html.rfm.SecurityCheckErrorView

class SecurityCheckErrorViewSpec extends ViewSpecBase {

  val page: SecurityCheckErrorView = inject[SecurityCheckErrorView]

  val view: Document = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Security Check Error View" should {

    "have a title" in {
      val title = "You cannot replace the current filing member for this group - Report Pillar 2 Top-up Taxes - GOV.UK"
      view.title() mustBe title
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "You cannot replace the current filing member for this group"
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").first().text must include(
        "This service is for new nominated filing members to takeover the responsibilities from the current filing member."
      )
    }

    "have a link" in {
      val paragraphMessageWithLink = view.getElementsByClass("govuk-body").last()
      val link                     = paragraphMessageWithLink.getElementsByTag("a")

      paragraphMessageWithLink.text() must include(
        "If you need to manage who can access your Pillar 2 Top-up Taxes returns, go to your business tax account."
      )
      link.text must include("go to your business tax account")
      link.attr("href") mustBe "https://www.gov.uk/guidance/sign-in-to-your-hmrc-business-tax-account"
    }

  }
}
