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
import models.{NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.mockito.Mockito.when
import pages.{RfmNameRegistrationPage, RfmRegisteredAddressPage}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.{RfmNameRegistrationSummary, RfmRegisteredAddressSummary}
import viewmodels.govuk.summarylist._
import views.html.rfm.RfmCheckYourAnswersView

class RfmCheckYourAnswersViewSpec extends ViewSpecBase {
  lazy val userName:    String = "John Doe"
  lazy val countryCode: String = "US"
  lazy val country:     String = "United States"

  lazy val userAnswer: UserAnswers = emptyUserAnswers
    .setOrException(RfmNameRegistrationPage, userName)
    .setOrException(RfmRegisteredAddressPage, nonUkAddress)

  when(mockCountryOptions.getCountryNameFromCode(countryCode)).thenReturn(country)

  lazy val list: SummaryList = SummaryListViewModel(
    rows = Seq(
      RfmNameRegistrationSummary.row(userAnswer)(messages),
      RfmRegisteredAddressSummary.row(userAnswer, mockCountryOptions)(messages)
    ).flatten
  )

  lazy val page:      RfmCheckYourAnswersView = inject[RfmCheckYourAnswersView]
  val view:           Document                = Jsoup.parse(page(NormalMode, list)(request, appConfig, messages).toString())
  lazy val pageTitle: String                  = "Check your answers for filing member details"

  "Rfm Check Your Answers View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Group details")
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a summary list keys" in {
      val summaryListKeys: Elements = view.getElementsByClass("govuk-summary-list__key")
      summaryListKeys.get(0).text must include("Name")
      summaryListKeys.get(1).text must include("Address")
    }

    "have a summary list items" in {
      val summaryListItems: Elements = view.getElementsByClass("govuk-summary-list__value")
      summaryListItems.get(0).text must include(userName)
      summaryListItems.get(1).text must include(nonUkAddress.addressLine1)
      summaryListItems.get(1).text must include(nonUkAddress.addressLine3)
      summaryListItems.get(1).text must include(country)
    }

    "have a summary list links" in {
      val summaryListActions: Elements = view.getElementsByClass("govuk-summary-list__actions")
      summaryListActions.get(0).text must include("Change")
      summaryListActions.get(1).text must include("Change")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Confirm and continue")
    }
  }
}
