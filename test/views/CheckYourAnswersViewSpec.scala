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
import models.MneOrDomestic.UkAndOther
import models.grs.EntityType
import models.subscription.AccountingPeriod
import models.{NonUKAddress, UKAddress, UserAnswers}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.mockito.Mockito.when
import pages.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utils.DateTimeUtils.toDateFormat
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers.*
import viewmodels.govuk.summarylist.*
import views.behaviours.ViewScenario
import views.html.CheckYourAnswersView

import java.time.LocalDate

class CheckYourAnswersViewSpec extends ViewSpecBase {

  lazy val accountingPeriod: AccountingPeriod = AccountingPeriod(LocalDate.now, LocalDate.now.plusYears(1))

  lazy val countryCodeUK: String = "GB"
  lazy val countryUK:     String = "United Kingdom"

  lazy val countryOptions: CountryOptions = mock[CountryOptions]
  when(countryOptions.getCountryNameFromCode(countryCodeUK)).thenReturn(countryUK)

  lazy val upeRegisteredAddress: UKAddress = UKAddress(
    addressLine1 = "Address Line 1",
    addressLine2 = None,
    addressLine3 = "City",
    addressLine4 = None,
    postalCode = "EH5 5WY",
    countryCode = "GB"
  )

  lazy val contactAddress: NonUKAddress = NonUKAddress(
    addressLine1 = "Spanish Address Line 1",
    addressLine2 = None,
    addressLine3 = "Spanish City",
    addressLine4 = None,
    postalCode = None,
    countryCode = "ES"
  )

  lazy val userAnswers: UserAnswers = emptyUserAnswers
    .setOrException(UpeNameRegistrationPage, "Test UPE")
    .setOrException(UpeEntityTypePage, EntityType.Other)
    .setOrException(UpeRegisteredAddressPage, upeRegisteredAddress)
    .setOrException(UpeContactNamePage, "UPE Contact Name")
    .setOrException(UpeContactEmailPage, "testcontactupe@email.com")
    .setOrException(UpePhonePreferencePage, true)
    .setOrException(UpeCapturePhonePage, "0044 808 157 0192")
    .setOrException(NominateFilingMemberPage, false)
    .setOrException(SubMneOrDomesticPage, UkAndOther)
    .setOrException(SubAccountingPeriodPage, accountingPeriod)
    .setOrException(SubPrimaryContactNamePage, "UPE Contact Name")
    .setOrException(SubPrimaryEmailPage, "testcontactupe@email.com")
    .setOrException(SubPrimaryPhonePreferencePage, true)
    .setOrException(SubPrimaryCapturePhonePage, "01632 960 001")
    .setOrException(SubAddSecondaryContactPage, true)
    .setOrException(SubSecondaryContactNamePage, "Second Contact Name")
    .setOrException(SubSecondaryEmailPage, "secondcontact@email.com")
    .setOrException(SubSecondaryCapturePhonePage, "01632 960 002")
    .setOrException(SubRegisteredAddressPage, contactAddress)

  lazy val upeSummaryList: SummaryList =
    SummaryListViewModel(
      rows = Seq(
        UpeNameRegistrationSummary.row(userAnswers),
        UpeRegisteredAddressSummary.row(userAnswers, countryOptions),
        UpeContactNameSummary.row(userAnswers),
        UpeContactEmailSummary.row(userAnswers),
        UpePhonePreferenceSummary.row(userAnswers),
        UPEContactPhoneSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyNameUpeSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyRegUpeSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyUtrUpeSummary.row(userAnswers),
        EntityTypePartnershipCompanyNameUpeSummary.row(userAnswers),
        EntityTypePartnershipCompanyRegUpeSummary.row(userAnswers),
        EntityTypePartnershipCompanyUtrUpeSummary.row(userAnswers)
      ).flatten
    )

  lazy val nfmSummaryList: SummaryList =
    SummaryListViewModel(
      rows = Seq(
        NominateFilingMemberYesNoSummary.row(userAnswers),
        NfmNameRegistrationSummary.row(userAnswers),
        NfmRegisteredAddressSummary.row(userAnswers, countryOptions),
        NfmContactNameSummary.row(userAnswers),
        NfmEmailAddressSummary.row(userAnswers),
        NfmPhonePreferenceSummary.row(userAnswers),
        NfmContactPhoneSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyNameNfmSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyRegNfmSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyUtrNfmSummary.row(userAnswers),
        EntityTypePartnershipCompanyNameNfmSummary.row(userAnswers),
        EntityTypePartnershipCompanyRegNfmSummary.row(userAnswers),
        EntityTypePartnershipCompanyUtrNfmSummary.row(userAnswers)
      ).flatten
    )

  lazy val groupDetailSummaryList: SummaryList =
    SummaryListViewModel(
      rows = Seq(
        MneOrDomesticSummary.row(userAnswers),
        GroupAccountingPeriodSummary.row(userAnswers),
        GroupAccountingPeriodStartDateSummary.row(userAnswers),
        GroupAccountingPeriodEndDateSummary.row(userAnswers)
      ).flatten
    )

  lazy val primaryContactSummaryList: SummaryList =
    SummaryListViewModel(
      rows = Seq(
        ContactNameComplianceSummary.row(userAnswers),
        ContactEmailAddressSummary.row(userAnswers),
        ContactByPhoneSummary.row(userAnswers),
        ContactCapturePhoneDetailsSummary.row(userAnswers)
      ).flatten
    )

  lazy val secondaryContactSummaryList: SummaryList =
    SummaryListViewModel(
      rows = Seq(
        AddSecondaryContactSummary.row(userAnswers),
        SecondaryContactNameSummary.row(userAnswers),
        SecondaryContactEmailSummary.row(userAnswers),
        SecondaryPhonePreferenceSummary.row(userAnswers),
        SecondaryPhoneSummary.row(userAnswers)
      ).flatten
    )

  lazy val addressSummaryList: SummaryList = {
    lazy val countryCodeES: String = "ES"
    lazy val countryES:     String = "Spain"
    when(countryOptions.getCountryNameFromCode(countryCodeES)).thenReturn(countryES)

    SummaryListViewModel(
      rows = Seq(ContactCorrespondenceAddressSummary.row(userAnswers, countryOptions)).flatten
    )
  }

  lazy val page: CheckYourAnswersView = inject[CheckYourAnswersView]
  lazy val view: Document             = Jsoup.parse(
    page(upeSummaryList, nfmSummaryList, groupDetailSummaryList, primaryContactSummaryList, secondaryContactSummaryList, addressSummaryList)(
      request,
      appConfig,
      messages
    ).toString()
  )
  lazy val pageTitle:        String   = "Check your answers before submitting your registration"
  lazy val h2Elements:       Elements = view.getElementsByTag("h2")
  lazy val summaryListKeys:  Elements = view.getElementsByClass("govuk-summary-list__key")
  lazy val summaryListItems: Elements = view.getElementsByClass("govuk-summary-list__value")
  lazy val summaryListLinks: Elements = view.getElementsByClass("govuk-summary-list__actions")

  "Check Your Answers View" should {
    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Review and submit"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have the correct H2 headings" in {
      val h2Elements: Elements = view.getElementsByTag("h2")

      h2Elements.get(1).text mustBe "Ultimate parent"
      h2Elements.get(2).text mustBe "Nominated filing member"
      h2Elements.get(3).text mustBe "Further group details"
      h2Elements.get(4).text mustBe "Primary contact"
      h2Elements.get(5).text mustBe "Secondary contact"
      h2Elements.get(6).text mustBe "Filing member contact address"
      h2Elements.get(7).text mustBe "Do you need to keep a record of your answers?"
      h2Elements.get(8).text mustBe "Now submit your registration to report Pillar 2 Top-up Taxes"
    }

    "have a summary list keys" in {
      summaryListKeys.get(0).text mustBe "Name"
      summaryListKeys.get(1).text mustBe "Address"
      summaryListKeys.get(2).text mustBe "Contact name"
      summaryListKeys.get(3).text mustBe "Email address"
      summaryListKeys.get(4).text mustBe "Can we contact by phone?"
      summaryListKeys.get(5).text mustBe "Phone number"
      summaryListKeys.get(6).text mustBe "Is there a nominated filing member"
      summaryListKeys.get(7).text mustBe "Where are the entities in your group located?"
      summaryListKeys.get(8).text mustBe "Group’s consolidated accounting period"
      summaryListKeys.get(9).text mustBe "Start date"
      summaryListKeys.get(10).text mustBe "End date"
      summaryListKeys.get(11).text mustBe "Contact name"
      summaryListKeys.get(12).text mustBe "Email address"
      summaryListKeys.get(13).text mustBe "Can we contact the primary contact by phone?"
      summaryListKeys.get(14).text mustBe "Primary phone number"
      summaryListKeys.get(15).text mustBe "Do you have a second contact?"
      summaryListKeys.get(16).text mustBe "Second contact name"
      summaryListKeys.get(17).text mustBe "Second contact email address"
      summaryListKeys.get(18).text mustBe "Second contact phone number"
      summaryListKeys.get(19).text mustBe "Address"
    }

    "have a summary list items" in {
      summaryListItems.get(0).text mustBe "Test UPE"
      summaryListItems.get(1).text mustBe "Address Line 1 City EH5 5WY United Kingdom"
      summaryListItems.get(2).text mustBe "UPE Contact Name"
      summaryListItems.get(3).text mustBe "testcontactupe@email.com"
      summaryListItems.get(4).text mustBe "Yes"
      summaryListItems.get(5).text mustBe "0044 808 157 0192"
      summaryListItems.get(6).text mustBe "No"
      summaryListItems.get(7).text mustBe "In the UK and outside the UK"
      summaryListItems.get(8).text mustBe ""
      summaryListItems.get(9).text mustBe LocalDate.now.toDateFormat
      summaryListItems.get(10).text mustBe LocalDate.now.plusYears(1).toDateFormat
      summaryListItems.get(11).text mustBe "UPE Contact Name"
      summaryListItems.get(12).text mustBe "testcontactupe@email.com"
      summaryListItems.get(13).text mustBe "Yes"
      summaryListItems.get(14).text mustBe "01632 960 001"
      summaryListItems.get(15).text mustBe "Yes"
      summaryListItems.get(16).text mustBe "Second Contact Name"
      summaryListItems.get(17).text mustBe "secondcontact@email.com"
      summaryListItems.get(18).text mustBe "01632 960 002"
      summaryListItems.get(19).text mustBe "Spanish Address Line 1 Spanish City Spain"
    }

    "have a summary list links" in {
      summaryListLinks.get(0).text mustBe "Change the name of the Ultimate Parent Entity"
      summaryListLinks.get(1).text mustBe "Change the registered office address of the Ultimate Parent Entity"
      summaryListLinks.get(2).text mustBe "Change the name of the person or team we should contact from the Ultimate Parent Entity"
      summaryListLinks.get(3).text mustBe "Change the email address for the Ultimate Parent Entity contact"
      summaryListLinks.get(4).text mustBe "Change can we contact the Ultimate Parent Entity by phone"
      summaryListLinks.get(5).text mustBe "Change the phone number for the Ultimate Parent Entity contact"
      summaryListLinks.get(6).text mustBe "Change is there a nominated filing member?"
      summaryListLinks.get(7).text mustBe "Change where are the entities in your group located"
      summaryListLinks.get(8).text mustBe "Change the dates of the group’s consolidated accounting period"
      summaryListLinks.get(9).text mustBe "Change the primary contact name"
      summaryListLinks.get(10).text mustBe "Change the primary contact email address"
      summaryListLinks.get(11).text mustBe "Change can we contact the primary contact by phone?"
      summaryListLinks.get(12).text mustBe "Change the phone number for the primary contact"
      summaryListLinks.get(13).text mustBe "Change do you have a secondary contact?"
      summaryListLinks.get(14).text mustBe "Change the secondary contact name"
      summaryListLinks.get(15).text mustBe "Change the secondary contact email address"
      summaryListLinks.get(16).text mustBe "Change the phone number for the secondary contact"
      summaryListLinks.get(17).text mustBe "Change the contact address"
    }

    "have paragraph content" in {
      val paragraphs: Elements = view.getElementsByClass("govuk-body")
      paragraphs.get(0).text mustBe "You can print or save a copy of your answers using the 'Print this page' link."
      paragraphs.get(1).text mustBe "By submitting these details, you are confirming that you are able to act as a " +
        "new filing member for your group and the information is correct and complete to the best of your knowledge."
    }

    "have a 'Print this page' link" in {
      val printPageElement: Element = view.getElementById("print-this-page")
      printPageElement.getElementsByTag("a").text() mustBe "Print this page"
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Confirm and send"
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", view)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
