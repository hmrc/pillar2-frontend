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

  lazy val page:      SecurityCheckErrorView = inject[SecurityCheckErrorView]
  lazy val view:      Document               = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle: String                 = "You cannot replace the current filing member for this group"

  "Security Check Error View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").first().text mustBe
        "This service is for new nominated filing members to takeover the responsibilities from the current filing member."
    }

    "have a link" in {
      val paragraphMessageWithLink = view.getElementsByClass("govuk-body").last()
      val link                     = paragraphMessageWithLink.getElementsByTag("a")

      paragraphMessageWithLink.text() mustBe
        "If you need to manage who can access your Pillar 2 Top-up Taxes returns, go to your business tax account."
      link.text mustBe "go to your business tax account"
      link.attr("href") mustBe "https://www.gov.uk/guidance/sign-in-to-your-hmrc-business-tax-account"
    }

  }
}
