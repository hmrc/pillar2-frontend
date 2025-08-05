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

  lazy val page:       CouldNotConfirmDetailsView = inject[CouldNotConfirmDetailsView]
  lazy val view:       Document                   = Jsoup.parse(page(NormalMode)(request, appConfig, messages).toString())
  lazy val pageTitle:  String                     = "We could not confirm your bank details"
  lazy val paragraphs: Elements                   = view.getElementsByClass("govuk-body")

  "Could Not Confirm Details View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a paragraph body" in {
      paragraphs.first().text mustBe "We are unable to proceed with the account details you entered."
    }

    "have a paragraph with link" in {
      paragraphs.last().text mustBe "Please return to your bank details and try again."
      paragraphs.last().getElementsByTag("a").text mustBe "return to your bank details and try again"
      paragraphs.last().getElementsByTag("a").attr("href") mustBe
        controllers.repayments.routes.BankAccountDetailsController.onPageLoad(NormalMode).url
    }

  }
}
