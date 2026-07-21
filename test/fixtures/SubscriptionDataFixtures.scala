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

package fixtures

import models.EnrolmentRequest.AllocateEnrolmentParameters
import models.requests.SubscriptionDataRequest
import models.rfm.CorporatePosition
import models.subscription.*
import models.subscription.AccountStatus.ActiveAccount
import models.subscription.responses.SubscriptionCreateResponse
import models.{MneOrDomestic, NonUKAddress, Verifier}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers.manageAccount.*
import viewmodels.govuk.all.SummaryListViewModel

import java.time.LocalDate

trait SubscriptionDataFixtures {

  val createSubscriptionPath: String = "/report-pillar2-top-up-taxes/subscription/create-subscription"

  val readCachedSubscriptionPath: String = "/report-pillar2-top-up-taxes/user-cache/read-subscription"

  val readSubscriptionPath:   String = "/report-pillar2-top-up-taxes/subscription/read-subscription"
  val readSubscriptionV2Path: String = "/report-pillar2-top-up-taxes/subscription/v2/read-subscription"

  val amendSubscriptionPath:   String = "/report-pillar2-top-up-taxes/subscription/amend-subscription"
  val amendSubscriptionV2Path: String = "/report-pillar2-top-up-taxes/subscription/v2/amend-subscription"

  val testFormBundleNumber: String = "123456789012"

  val testLocalDate: LocalDate = LocalDate.of(2025, 7, 18)

  private val upeDetails: UpeDetails =
    UpeDetails(
      safeId = None,
      customerIdentification1 = None,
      customerIdentification2 = None,
      organisationName = "UK Only Organisation Ltd",
      registrationDate = LocalDate.of(2024, 1, 31),
      domesticOnly = true,
      filingMember = false
    )

  private val upeDetailsAmend: UpeDetailsAmend =
    UpeDetailsAmend(
      plrReference = "plrReference",
      customerIdentification1 = None,
      customerIdentification2 = None,
      organisationName = "UK Only Organisation Ltd",
      registrationDate = LocalDate.of(2024, 1, 31),
      domesticOnly = true,
      filingMember = false
    )

  private val upeCorrespAddressDetails: UpeCorrespAddressDetails =
    UpeCorrespAddressDetails(
      addressLine1 = "1 High Street",
      addressLine2 = None,
      addressLine3 = None,
      addressLine4 = None,
      postCode = None,
      countryCode = "GB"
    )

  private val primaryContactDetails: ContactDetailsType =
    ContactDetailsType(
      name = "John Doe",
      phone = None,
      emailAddress = "john.doe@example.com"
    )

  private val secondaryContactDetails: ContactDetailsType =
    ContactDetailsType(
      name = "Jane Smith",
      phone = None,
      emailAddress = "jane.smith@example.com"
    )

  val amendableAccountingPeriod: AccountingPeriodDisplay =
    AccountingPeriodDisplay(
      startDate = Some(LocalDate.of(2024, 1, 6)),
      endDate = Some(LocalDate.of(2025, 4, 6)),
      dueDate = Some(LocalDate.of(2024, 4, 6)),
      canAmendStartDate = Some(true),
      canAmendEndDate = Some(true)
    )

  val originalAccountingPeriod: OriginalAccountingPeriod =
    OriginalAccountingPeriod(
      taxObligationStartDate = LocalDate.of(2024, 1, 1),
      taxObligationEndDate = LocalDate.of(2024, 12, 31)
    )

  val newAccountingPeriod: NewAccountingPeriod =
    NewAccountingPeriod(
      updateObligationStartDate = LocalDate.of(2025, 1, 1),
      updateObligationEndDate = LocalDate.of(2025, 12, 31)
    )

  val accountingPeriodAmend: AccountingPeriodAmend =
    AccountingPeriodAmend(
      amendAccountingPeriod = true,
      originalAccountingPeriods = Some(Seq(originalAccountingPeriod)),
      newAccountingPeriod = Some(newAccountingPeriod)
    )

  val subscriptionDataDisplay: SubscriptionDataDisplay = SubscriptionDataDisplay(
    formBundleNumber = testFormBundleNumber,
    upeDetails = upeDetails,
    upeCorrespAddressDetails = upeCorrespAddressDetails,
    primaryContactDetails = primaryContactDetails,
    secondaryContactDetails = None,
    filingMemberDetails = None,
    accountingPeriod = Some(Seq(amendableAccountingPeriod)),
    accountStatus = Some(ActiveAccount)
  )

  val subscriptionDataDisplayJson: String =
    s"""
       |{
       |  "formBundleNumber": "$testFormBundleNumber",
       |  "upeDetails": {
       |    "organisationName": "UK Only Organisation Ltd",
       |    "registrationDate": "2024-01-31",
       |    "domesticOnly": true,
       |    "filingMember": false
       |  },
       |  "upeCorrespAddressDetails": {
       |    "addressLine1": "1 High Street",
       |    "countryCode": "GB"
       |  },
       |  "primaryContactDetails": {
       |    "name": "John Doe",
       |    "emailAddress": "john.doe@example.com"
       |  },
       |  "secondaryContactDetails": null,
       |  "filingMemberDetails": null,
       |  "accountingPeriod": [
       |    {
       |      "startDate": "2024-01-06",
       |      "endDate": "2025-04-06",
       |      "dueDate": "2024-04-06",
       |      "canAmendStartDate": true,
       |      "canAmendEndDate": true
       |    }
       |  ],
       |  "accountStatus": {
       |    "inactive": false
       |  }
       |}
       |""".stripMargin

  val subscriptionDataDisplayWrappedJson: String =
    s"""{ "success": $subscriptionDataDisplayJson }"""

  val filingMemberDetailsAmend: FilingMemberDetailsAmend =
    FilingMemberDetailsAmend(
      addNewFilingMember = true,
      safeId = "someSafeId",
      customerIdentification1 = Some("CRN"),
      customerIdentification2 = Some("UTR"),
      organisationName = "Company"
    )

  private val amendUpeCorrespAddressDetails: UpeCorrespAddressDetails =
    UpeCorrespAddressDetails(
      addressLine1 = "1 Test Street",
      addressLine2 = None,
      addressLine3 = Some("Testville"),
      addressLine4 = None,
      postCode = None,
      countryCode = "US"
    )

  private val amendPrimaryContactDetails: ContactDetailsType =
    ContactDetailsType(
      name = "John Doe",
      phone = Some("07700987654"),
      emailAddress = "john.doe@example.com"
    )

  val amendSubscriptionDataV2: SubscriptionDataAmend =
    SubscriptionDataAmend(
      replaceFilingMember = true,
      upeDetails = upeDetailsAmend,
      accountingPeriod = accountingPeriodAmend,
      upeCorrespAddressDetails = amendUpeCorrespAddressDetails,
      primaryContactDetails = amendPrimaryContactDetails,
      secondaryContactDetails = Some(secondaryContactDetails),
      filingMemberDetails = Some(filingMemberDetailsAmend)
    )

  val subscriptionDataAmendJson: String =
    """
      |{
      |  "replaceFilingMember": false,
      |  "upeDetails": {
      |    "plrReference": "PLRXLM123123123",
      |    "organisationName": "Organisation Ltd",
      |    "registrationDate": "2024-01-31",
      |    "domesticOnly": true,
      |    "filingMember": false
      |  },
      |  "accountingPeriod": {
      |    "amendAccountingPeriod": true,
      |    "originalAccountingPeriods": [
      |      {
      |        "taxObligationStartDate": "2024-01-06",
      |        "taxObligationEndDate": "2025-04-06"
      |      }
      |    ],
      |    "newAccountingPeriod": {
      |      "updateObligationStartDate": "2024-06-01",
      |      "updateObligationEndDate": "2025-05-31"
      |    }
      |  },
      |  "upeCorrespAddressDetails": {
      |    "addressLine1": "1 High Street",
      |    "countryCode": "GB"
      |  },
      |  "primaryContactDetails": {
      |    "name": "John Doe",
      |    "emailAddress": "john.doe@example.com"
      |  }
      |}
      |""".stripMargin

  val replaceFilingMemberData: NewFilingMemberDetails = NewFilingMemberDetails(
    securityAnswerUserReference = "plrReference",
    securityAnswerRegistrationDate = LocalDate.of(2024, 12, 31),
    plrReference = "plrReference",
    corporatePosition = CorporatePosition.Upe,
    ukBased = Some(false),
    nameRegistration = Some("New Filing Member Ltd"),
    registeredAddress = Some(NonUKAddress("1 Test Street", None, "Testville", None, None, "US")),
    primaryContactName = "John Doe",
    primaryContactEmail = "john.doe@example.com",
    primaryContactPhonePreference = true,
    primaryContactPhoneNumber = Some("07700987654"),
    addSecondaryContact = true,
    secondaryContactInformation = Some(secondaryContactDetails),
    contactAddress = NonUKAddress("1 Test Street", None, "Testville", None, None, "US")
  )

  val validSubscriptionCreateParameter: SubscriptionRequestParameters =
    SubscriptionRequestParameters(
      id = "id",
      regSafeId = "regSafeId",
      fmSafeId = Some("fmSafeId")
    )

  val businessSubscriptionSuccessResponseJson: String =
    """
      |{
      |  "success" : {
      |    "plrReference": "XMPLR0012345678",
      |    "formBundleNumber": "119000004320",
      |    "processingDate": "2023-09-22T00:00"
      |  }
      |}""".stripMargin

  val validBusinessSubscriptionSuccessResponse: SubscriptionCreateResponse =
    SubscriptionCreateResponse(
      plrReference = "XMPLR0012345678",
      formBundleNumber = "119000004320",
      processingDate = LocalDate.parse("2023-09-22").atStartOfDay()
    )

  val unsuccessfulNotFoundJson: String =
    """
      |{
      |  "status": "404",
      |  "error": "there is nothing here"
      |}""".stripMargin

  val unsuccessfulResponseJson: String = """{ "status": "error" }"""

  val emptySubscriptionLocalData: SubscriptionLocalData =
    SubscriptionLocalData(
      plrReference = "XMPLR0123456789",
      subMneOrDomestic = MneOrDomestic.Uk,
      subAccountingPeriod = Some(AccountingPeriod(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31))),
      subPrimaryContactName = "",
      subPrimaryEmail = "",
      subPrimaryPhonePreference = false,
      subPrimaryCapturePhone = None,
      subAddSecondaryContact = false,
      subSecondaryContactName = None,
      subSecondaryEmail = None,
      subSecondaryCapturePhone = None,
      subSecondaryPhonePreference = Some(false),
      subRegisteredAddress = NonUKAddress("", None, "", None, None, ""),
      accountStatus = Some(ActiveAccount),
      organisationName = None
    )

  val someSubscriptionLocalData: SubscriptionLocalData =
    SubscriptionLocalData(
      plrReference = "XMPLR0123456789",
      subMneOrDomestic = MneOrDomestic.Uk,
      subAccountingPeriod = Some(AccountingPeriod(testLocalDate, testLocalDate.plusYears(1))),
      subPrimaryContactName = "John Doe",
      subPrimaryEmail = "john.doe@example.com",
      subPrimaryPhonePreference = true,
      subPrimaryCapturePhone = Some("07700987654"),
      subAddSecondaryContact = true,
      subSecondaryContactName = Some("Jane Smith"),
      subSecondaryEmail = Some("jane.smith@example.com"),
      subSecondaryCapturePhone = Some("07700912345"),
      subSecondaryPhonePreference = Some(true),
      subRegisteredAddress = NonUKAddress("1 High Street", None, "Testville", None, None, "GB"),
      accountStatus = Some(ActiveAccount),
      organisationName = Some("Test Organisation Ltd")
    )

  val someSubscriptionLocalDataUkOther: SubscriptionLocalData =
    SubscriptionLocalData(
      plrReference = "XMPLR0123456789",
      subMneOrDomestic = MneOrDomestic.UkAndOther,
      subAccountingPeriod = Some(AccountingPeriod(LocalDate.of(2024, 10, 24), LocalDate.of(2025, 10, 23))),
      subPrimaryContactName = "John Doe",
      subPrimaryEmail = "john.doe@example.com",
      subPrimaryPhonePreference = true,
      subPrimaryCapturePhone = Some("07700987654"),
      subAddSecondaryContact = true,
      subSecondaryContactName = Some("Jane Smith"),
      subSecondaryEmail = Some("jane.smith@example.com"),
      subSecondaryCapturePhone = Some("07700912345"),
      subSecondaryPhonePreference = Some(true),
      subRegisteredAddress = NonUKAddress("1 High Street", None, "Testville", None, None, "GB"),
      accountStatus = Some(ActiveAccount),
      organisationName = Some("Test Organisation Ltd")
    )

  val allocateEnrolmentParameters: AllocateEnrolmentParameters =
    AllocateEnrolmentParameters(
      userId = "testUserId",
      verifiers = Seq(Verifier("CTUTR", "Utr"), Verifier("CRN", "Crn"))
    )

  def subscriptionDataGroupSummaryList()(using messages: Messages, request: SubscriptionDataRequest[?]): SummaryList =
    SummaryListViewModel(
      rows = Seq(
        MneOrDomesticSummary.row(),
        GroupAccountingPeriodSummary.row(),
        GroupAccountingPeriodStartDateSummary.row(),
        GroupAccountingPeriodEndDateSummary.row()
      ).flatten
    )

  def subscriptionDataPrimaryContactList()(using messages: Messages, request: SubscriptionDataRequest[?]): SummaryList =
    SummaryListViewModel(
      rows = Seq(
        ContactNameComplianceSummary.row(),
        ContactEmailAddressSummary.row(),
        ContactByPhoneSummary.row(),
        ContactCapturePhoneDetailsSummary.row()
      ).flatten
    )

  def subscriptionDataSecondaryContactList()(using messages: Messages, request: SubscriptionDataRequest[?]): SummaryList =
    SummaryListViewModel(
      rows = Seq(
        AddSecondaryContactSummary.row(),
        SecondaryContactNameSummary.row(),
        SecondaryContactEmailSummary.row(),
        SecondaryPhonePreferenceSummary.row(),
        SecondaryPhoneSummary.row()
      ).flatten
    )

  def subscriptionDataAddress(countryOptions: CountryOptions)(using
    messages: Messages,
    request:  SubscriptionDataRequest[?]
  ): SummaryList = SummaryListViewModel(
    rows = Seq(ContactCorrespondenceAddressSummary.row(countryOptions)).flatten
  )

}
