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

package views.registrationview

import base.ViewSpecBase
import models.{CheckMode, UKAddress, UserAnswers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.mockito.Mockito.when
import pages._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.registrationview.UpeCheckYourAnswersView

class UpeCheckYourAnswersViewSpec extends ViewSpecBase {

  lazy val countryCode: String = "UAE"
  lazy val country:     String = "United Arab Emirates"

  lazy val upeRegisteredAddress: UKAddress = UKAddress(
    addressLine1 = "Address Line 1 UPE",
    addressLine2 = None,
    addressLine3 = "City UPE",
    addressLine4 = None,
    postalCode = "INVALID",
    countryCode = countryCode
  )

  lazy val userAnswers: UserAnswers = emptyUserAnswers
    .setOrException(UpeNameRegistrationPage, "Test UPE")
    .setOrException(UpeRegisteredAddressPage, upeRegisteredAddress)
    .setOrException(UpeContactNamePage, "Contact UPE")
    .setOrException(UpeContactEmailPage, "testcontactupe@email.com")
    .setOrException(UpePhonePreferencePage, true)
    .setOrException(UpeCapturePhonePage, "1234569")

  when(mockCountryOptions.getCountryNameFromCode(countryCode)).thenReturn(country)

  val summaryList: SummaryList = SummaryListViewModel(
    rows = Seq(
      UpeNameRegistrationSummary.row(userAnswers),
      UpeRegisteredAddressSummary.row(userAnswers, mockCountryOptions),
      UpeContactNameSummary.row(userAnswers),
      UpeContactEmailSummary.row(userAnswers),
      UpeTelephonePreferenceSummary.row(userAnswers),
      UPEContactTelephoneSummary.row(userAnswers)
    ).flatten
  )

  lazy val page: UpeCheckYourAnswersView = inject[UpeCheckYourAnswersView]
  lazy val view: Document = Jsoup.parse(
    page(summaryList)(request, appConfig, messages).toString()
  )
  lazy val pageTitle: String = "Check your answers for ultimate parent details"

  "Upe Check Your Answers View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have the correct section caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Group details"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size mustBe 1
      h1Elements.text mustBe pageTitle
    }

    "have a summary list keys" in {
      view.getElementsByClass("govuk-summary-list__key").get(0).text mustBe "Name"
      view.getElementsByClass("govuk-summary-list__key").get(1).text mustBe "Address"
      view.getElementsByClass("govuk-summary-list__key").get(2).text mustBe "Contact name"
      view.getElementsByClass("govuk-summary-list__key").get(3).text mustBe "Email address"
      view.getElementsByClass("govuk-summary-list__key").get(4).text mustBe "Can we contact by phone?"
      view.getElementsByClass("govuk-summary-list__key").get(5).text mustBe "Phone number"
    }

    "have a summary list items" in {
      view.getElementsByClass("govuk-summary-list__value").get(0).text mustBe "Test UPE"
      view.getElementsByClass("govuk-summary-list__value").get(1).text mustBe "Address Line 1 UPE City UPE INVALID United Arab Emirates"
      view.getElementsByClass("govuk-summary-list__value").get(2).text mustBe "Contact UPE"
      view.getElementsByClass("govuk-summary-list__value").get(3).text mustBe "testcontactupe@email.com"
      view.getElementsByClass("govuk-summary-list__value").get(4).text mustBe "Yes"
      view.getElementsByClass("govuk-summary-list__value").get(5).text mustBe "1234569"
    }

    "have a summary list links" in {
      view.getElementsByClass("govuk-summary-list__actions").get(0).text mustBe "Change the name of the Ultimate Parent Entity"
      view.getElementsByClass("govuk-summary-list__actions").get(0).getElementsByTag("a").attr("href") mustBe
        controllers.registration.routes.UpeNameRegistrationController.onPageLoad(CheckMode).url

      view.getElementsByClass("govuk-summary-list__actions").get(1).text mustBe "Change the registered office address of the Ultimate Parent Entity"
      view.getElementsByClass("govuk-summary-list__actions").get(1).getElementsByTag("a").attr("href") mustBe
        controllers.registration.routes.UpeRegisteredAddressController.onPageLoad(CheckMode).url

      view
        .getElementsByClass("govuk-summary-list__actions")
        .get(2)
        .text mustBe "Change the name of the person or team we should contact from the Ultimate Parent Entity"
      view.getElementsByClass("govuk-summary-list__actions").get(2).getElementsByTag("a").attr("href") mustBe
        controllers.registration.routes.UpeContactNameController.onPageLoad(CheckMode).url

      view.getElementsByClass("govuk-summary-list__actions").get(3).text mustBe "Change the email address for the Ultimate Parent Entity contact"
      view.getElementsByClass("govuk-summary-list__actions").get(3).getElementsByTag("a").attr("href") mustBe
        controllers.registration.routes.UpeContactEmailController.onPageLoad(CheckMode).url

      view.getElementsByClass("govuk-summary-list__actions").get(4).text mustBe "Change can we contact the Ultimate Parent Entity by phone"
      view.getElementsByClass("govuk-summary-list__actions").get(4).getElementsByTag("a").attr("href") mustBe
        controllers.registration.routes.ContactUPEByPhoneController.onPageLoad(CheckMode).url

      view.getElementsByClass("govuk-summary-list__actions").get(5).text mustBe "Change the phone number for the Ultimate Parent Entity contact"
      view.getElementsByClass("govuk-summary-list__actions").get(5).getElementsByTag("a").attr("href") mustBe
        controllers.registration.routes.CapturePhoneDetailsController.onPageLoad(CheckMode).url
    }
  }

  "have a button" in {
    view.getElementsByClass("govuk-button").text mustBe "Confirm and continue"
  }
}
