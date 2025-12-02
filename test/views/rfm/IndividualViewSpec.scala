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
import views.html.rfm.IndividualView

class IndividualViewSpec extends ViewSpecBase {

  lazy val page:       IndividualView = inject[IndividualView]
  lazy val view:       Document       = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle:  String         = "Sorry, you’re unable to use this service"
  lazy val paragraphs: Elements       = view.getElementsByClass("govuk-body")

  "Individual View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a paragraph body" in {
      paragraphs.get(0).text mustBe
        "You’ve signed in with an individual account. Only users with an organisation account can replace their nominated filing member."
    }

    "have a paragraph with link" in {
      paragraphs.get(1).text mustBe "If you need to replace a nominated filing member, sign in to Government Gateway with an organisation account."
      val paragraphLink = paragraphs.get(1).select("a")
      paragraphLink.text mustBe "sign in to Government Gateway with an organisation account"
      paragraphLink.attr("href") mustBe s"${appConfig.loginUrl}" + "?continue=" + s"${appConfig.rfmLoginContinueUrl}"
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")
      link.text mustBe "Find out more about who can use this service"
      link.attr("href") mustBe appConfig.rfmGuidanceUrl
    }

  }

}
