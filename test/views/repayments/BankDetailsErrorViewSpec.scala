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
import views.html.repayments.BankDetailsErrorView

class BankDetailsErrorViewSpec extends ViewSpecBase {

  val page = inject[BankDetailsErrorView]

  val view = Jsoup.parse(page(clientPillar2Id = None, NormalMode)(request, appConfig, messages).toString())

  "Bank Details Error View" should {

    "have a title" in {
      val title = "We could not confirm your bank details - Report Pillar 2 top-up taxes - GOV.UK"
      view.getElementsByTag("title").text mustBe title
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("We could not confirm your bank details")
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").first().text must include("We are unable to proceed with the account details you entered.")
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")

      link.text         must include("try again with a different business bank account")
      link.attr("href") must include("/report-pillar2-top-up-taxes/repayment/uk-details")
    }

  }
}
