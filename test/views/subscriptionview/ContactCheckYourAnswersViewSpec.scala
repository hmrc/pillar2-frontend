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

package views.subscriptionview

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.subscriptionview.ContactCheckYourAnswersView
import viewmodels.govuk.SummaryListFluency
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

class ContactCheckYourAnswersViewSpec extends ViewSpecBase with SummaryListFluency {

  private val primaryContactList   = SummaryList(rows = Seq.empty)
  private val secondaryContactList = SummaryList(rows = Seq.empty)
  private val addressList          = SummaryList(rows = Seq.empty)

  lazy val page: ContactCheckYourAnswersView = inject[ContactCheckYourAnswersView]
  lazy val view:      Document = Jsoup.parse(page(primaryContactList, secondaryContactList, addressList)(request, appConfig, messages).toString())
  lazy val pageTitle: String   = "Check your answers"

  "ContactCheckYourAnswersView" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Contact details"
    }

    "have a continue button" in {
      view.getElementsByClass("govuk-button").text mustBe "Continue"
    }
  }
}
