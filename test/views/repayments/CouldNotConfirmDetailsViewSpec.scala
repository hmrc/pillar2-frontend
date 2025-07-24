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
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.repayments.CouldNotConfirmDetailsView

class CouldNotConfirmDetailsViewSpec extends ViewSpecBase {

  val page: CouldNotConfirmDetailsView = inject[CouldNotConfirmDetailsView]

  val view: Document = Jsoup.parse(page(NormalMode)(request, appConfig, messages).toString())

  "Could Not Confirm Details View" should {

    "have a title" in {
      val title = "We could not confirm your bank details - Report Pillar 2 Top-up Taxes - GOV.UK"
      view.title() mustBe title
    }

    "have a heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "We could not confirm your bank details"
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").first().text must include("We are unable to proceed with the account details you entered.")
      view.getElementsByClass("govuk-body").last.text    must include("Please")
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")

      link.text         must include("return to your bank details and try again")
      link.attr("href") must include("/report-pillar2-top-up-taxes/repayment/uk-details")
    }

  }
}
