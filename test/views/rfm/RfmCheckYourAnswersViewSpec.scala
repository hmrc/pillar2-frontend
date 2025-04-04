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
import org.mockito.Mockito.when
import pages.{RfmNameRegistrationPage, RfmRegisteredAddressPage}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.{RfmNameRegistrationSummary, RfmRegisteredAddressSummary}
import viewmodels.govuk.summarylist._
import views.html.rfm.RfmCheckYourAnswersView

class RfmCheckYourAnswersViewSpec extends ViewSpecBase {
  val userName    = "John Doe"
  val countryCode = "US"
  val country     = "United States"

  val userAnswer: UserAnswers = emptyUserAnswers
    .setOrException(RfmNameRegistrationPage, userName)
    .setOrException(RfmRegisteredAddressPage, nonUkAddress)

  when(mockCountryOptions.getCountryNameFromCode(countryCode)).thenReturn(country)

  val list: SummaryList = SummaryListViewModel(
    rows = Seq(
      RfmNameRegistrationSummary.row(userAnswer)(messages),
      RfmRegisteredAddressSummary.row(userAnswer, mockCountryOptions)(messages)
    ).flatten
  )

  val page: RfmCheckYourAnswersView = inject[RfmCheckYourAnswersView]

  val view: Document = Jsoup.parse(page(NormalMode, list)(request, appConfig, messages).toString())

  "Rfm Check Your Answers View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("Check your answers for filing member details")
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Group details")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("Check your answers for filing member details")
    }

    "have a summary list keys" in {
      view.getElementsByClass("govuk-summary-list__key").get(0).text must include("Name")
      view.getElementsByClass("govuk-summary-list__key").get(1).text must include("Address")
    }

    "have a summary list items" in {
      view.getElementsByClass("govuk-summary-list__value").get(0).text must include(userName)
      view.getElementsByClass("govuk-summary-list__value").get(1).text must include(nonUkAddress.addressLine1)
      view.getElementsByClass("govuk-summary-list__value").get(1).text must include(nonUkAddress.addressLine3)
      view.getElementsByClass("govuk-summary-list__value").get(1).text must include(country)
    }

    "have a summary list links" in {
      view.getElementsByClass("govuk-summary-list__actions").get(0).text must include("Change")
      view.getElementsByClass("govuk-summary-list__actions").get(1).text must include("Change")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Confirm and continue")
    }
  }
}
