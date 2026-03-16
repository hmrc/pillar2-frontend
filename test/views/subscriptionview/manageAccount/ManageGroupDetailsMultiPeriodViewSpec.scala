/*
 * Copyright 2026 HM Revenue & Customs
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

package views.subscriptionview.manageAccount

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.subscriptionview.manageAccount.ManageGroupDetailsMultiPeriodView

class ManageGroupDetailsMultiPeriodViewSpec extends ViewSpecBase {

  private val page = inject[ManageGroupDetailsMultiPeriodView]

  "ManageGroupDetailsMultiPeriodView" when {

    "rendering amendable period cards" must {
      "show cards and change links" in {
        val periodCards = Seq(
          ("Current period", "1 January 2025", "31 December 2025", Some("/manage-account/account-details/select-period/0")),
          ("Previous period", "1 January 2024", "31 December 2024", Some("/manage-account/account-details/select-period/1"))
        )

        val view: Document = Jsoup.parse(
          page(
            locationMessageKey = "mneOrDomestic.ukAndOther",
            periodCards = periodCards,
            isEmpty = false,
            isAgent = false,
            organisationName = None,
            plrReference = "XMPLR0123456789"
          )(request, appConfig, messages).toString()
        )

        view.title() mustBe "Manage group details - Report Pillar 2 Top-up Taxes - GOV.UK"
        view.getElementsByClass("govuk-summary-card").size() mustBe 2
        view.text() must include("In the UK and outside the UK")
        view.text() must include("Current period")
        view.text() must include("Previous period")
        view.text() must include("1 January 2025")
        view.text() must include("31 December 2025")
        view.text() must include("Change")
      }
    }

    "rendering empty state" must {
      "show no periods message" in {
        val view: Document = Jsoup.parse(
          page(
            locationMessageKey = "mneOrDomestic.uk",
            periodCards = Seq.empty,
            isEmpty = true,
            isAgent = false,
            organisationName = None,
            plrReference = "XMPLR0123456789"
          )(request, appConfig, messages).toString()
        )

        view.text() must include("There are no accounting periods available to amend")
      }
    }

    "rendering agent view" must {
      "show group header with name and ID" in {
        val view: Document = Jsoup.parse(
          page(
            locationMessageKey = "mneOrDomestic.uk",
            periodCards = Seq.empty,
            isEmpty = true,
            isAgent = true,
            organisationName = Some("ABC Group"),
            plrReference = "XMPLR0123456789"
          )(request, appConfig, messages).toString()
        )

        view.text() must include("Group: ABC Group ID: XMPLR0123456789")
      }
    }
  }
}
