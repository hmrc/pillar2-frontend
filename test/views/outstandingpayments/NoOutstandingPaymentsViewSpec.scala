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

package views.outstandingpayments

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.outstandingpayments.NoOutstandingPaymentsView

class NoOutstandingPaymentsViewSpec extends ViewSpecBase {

  lazy val page: NoOutstandingPaymentsView = inject[NoOutstandingPaymentsView]
  private val plrReference = "XMPLR0123456789"
  lazy val groupView: Document = Jsoup.parse(page(plrReference)(request, appConfig, messages, isAgent = false).toString())
  lazy val agentView: Document = Jsoup.parse(page(plrReference)(request, appConfig, messages, isAgent = true).toString())
  lazy val pageTitle: String   = "Outstanding payments"

  "No Outstanding Payments View" should {
    "have a title" in {
      groupView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements = groupView.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "show 'No payments due' under details of outstanding payments" in {
      groupView.text() must include("No payments due.")
    }

    "show the Pillar 2 reference in 'Other ways to pay' section for a group" in {
      groupView.text() must include(s"Your Pillar 2 reference: $plrReference")
    }

    "show the Pillar 2 reference in 'Other ways to pay' section for an agent" in {
      agentView.text() must include(s"Pillar 2 reference: $plrReference")
    }

    "have all required sections" in {
      groupView.text() must include("Other ways to pay")
      groupView.text() must include("Details of outstanding payments")
      groupView.text() must include("Transaction history")
      groupView.text() must include("Penalties and interest charges")
    }
  }
}
