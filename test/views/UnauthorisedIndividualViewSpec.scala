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
import views.html.UnauthorisedIndividualView

class UnauthorisedIndividualViewSpec extends ViewSpecBase {

  val page: UnauthorisedIndividualView = inject[UnauthorisedIndividualView]

  val view: Document = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Unauthorised Individual View" should {

    "have a title" in {
      view.title() mustBe "Register your group - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("Sorry, you’re unable to use this service")
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").get(0).text must include(
        "You’ve signed in with an individual account. Only users with an organisation account can register to use this service."
      )
    }

    "have a paragraph with link" in {
      val paragraphWithLink = view.getElementsByClass("govuk-body").get(1)
      paragraphWithLink.text                     must include("If the group still needs to register,")
      paragraphWithLink.select("a").text         must include("sign in to Government Gateway with an organisation account.")
      paragraphWithLink.select("a").attr("href") must include(appConfig.loginUrl)
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")
      link.text         must include("Find out more about who can use this service")
      link.attr("href") must include(appConfig.startPagePillar2Url)
    }

  }

}
