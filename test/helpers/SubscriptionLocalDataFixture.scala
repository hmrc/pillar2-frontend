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

package helpers

import models.EnrolmentRequest.AllocateEnrolmentParameters
import models.requests.SubscriptionDataRequest
import models.rfm.CorporatePosition
import models.subscription._
import models.{MneOrDomestic, NonUKAddress, Verifier}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers.manageAccount._
import viewmodels.govuk.summarylist.SummaryListViewModel

import java.time.LocalDate

trait SubscriptionLocalDataFixture {
  private val upeCorrespondenceAddress = UpeCorrespAddressDetails("middle", None, Some("lane"), None, None, "obv")
  private val upeDetailsAmend =
    UpeDetailsAmend("plrReference", None, None, "orgName", LocalDate.of(2024, 1, 31), domesticOnly = false, filingMember = false)
  private val contactDetails = ContactDetailsType("shadow", Some("dota2"), "shadow@fiend.com")
  val filingMemberAmendDetails: FilingMemberAmendDetails = FilingMemberAmendDetails(
    addNewFilingMember = true,
    safeId = "someSafeId",
    customerIdentification1 = Some("CRN"),
    customerIdentification2 = Some("UTR"),
    organisationName = "Company"
  )

  lazy val currentDate: LocalDate = LocalDate.now()

  val emptySubscriptionLocalData: SubscriptionLocalData = SubscriptionLocalData(
    plrReference = "Abc123",
    subMneOrDomestic = MneOrDomestic.Uk,
    subAccountingPeriod = AccountingPeriod(LocalDate.now, LocalDate.now.plusYears(1)),
    subPrimaryContactName = "",
    subPrimaryEmail = "",
    subPrimaryPhonePreference = false,
    subPrimaryCapturePhone = None,
    subAddSecondaryContact = false,
    subSecondaryContactName = None,
    subSecondaryEmail = None,
    subSecondaryCapturePhone = None,
    subSecondaryPhonePreference = Some(false),
    subRegisteredAddress = NonUKAddress("", None, "", None, None, "")
  )

  val someSubscriptionLocalData: SubscriptionLocalData = SubscriptionLocalData(
    plrReference = "Abc123",
    subMneOrDomestic = MneOrDomestic.Uk,
    subAccountingPeriod = AccountingPeriod(LocalDate.now, LocalDate.now.plusYears(1)),
    subPrimaryContactName = "John",
    subPrimaryEmail = "john@email.com",
    subPrimaryPhonePreference = true,
    subPrimaryCapturePhone = Some("123"),
    subAddSecondaryContact = true,
    subSecondaryContactName = Some("Doe"),
    subSecondaryEmail = Some("doe@email.com"),
    subSecondaryCapturePhone = Some("123"),
    subSecondaryPhonePreference = Some(true),
    subRegisteredAddress = NonUKAddress("line1", None, "line", None, None, "GB")
  )

  val subscriptionData: SubscriptionData = SubscriptionData(
    formBundleNumber = "form bundle",
    upeDetails = UpeDetails(None, None, None, "orgName", LocalDate.of(2024, 1, 31), domesticOnly = false, filingMember = false),
    upeCorrespAddressDetails = upeCorrespondenceAddress,
    primaryContactDetails = contactDetails,
    secondaryContactDetails = None,
    filingMemberDetails = None,
    accountingPeriod = AccountingPeriod(currentDate, currentDate.plusYears(1)),
    accountStatus = Some(AccountStatus(false))
  )

  val allocateEnrolmentParameters: AllocateEnrolmentParameters = AllocateEnrolmentParameters(
    userId = "id",
    verifiers = Seq(Verifier("CTUTR", "Utr"), Verifier("CRN", "Crn"))
  )

  val amendData: AmendSubscription = AmendSubscription(
    replaceFilingMember = true,
    upeDetails = upeDetailsAmend,
    accountingPeriod = AccountingPeriodAmend(currentDate, currentDate.plusYears(1)),
    upeCorrespAddressDetails = upeCorrespondenceAddress,
    primaryContactDetails = contactDetails,
    secondaryContactDetails = Some(contactDetails),
    filingMemberDetails = Some(filingMemberAmendDetails)
  )
  val replaceFilingMemberData: NewFilingMemberDetail = NewFilingMemberDetail(
    securityAnswerUserReference = "plrReference",
    securityAnswerRegistrationDate = LocalDate.of(2024, 12, 31),
    plrReference = "plrReference",
    corporatePosition = CorporatePosition.Upe,
    ukBased = Some(false),
    nameRegistration = Some("nameRegistration"),
    registeredAddress = Some(NonUKAddress("middle", None, "lane", None, None, "obv")),
    primaryContactName = "shadow",
    primaryContactEmail = "shadow@fiend.com",
    primaryContactPhonePreference = true,
    primaryContactPhoneNumber = Some("dota2"),
    addSecondaryContact = true,
    secondaryContactInformation = Some(contactDetails),
    contactAddress = NonUKAddress("middle", None, "lane", None, None, "obv")
  )

  def subscriptionDataGroupSummaryList()(implicit messages: Messages, request: SubscriptionDataRequest[_]): SummaryList = SummaryListViewModel(
    rows = Seq(
      MneOrDomesticSummary.row(),
      GroupAccountingPeriodSummary.row(),
      GroupAccountingPeriodStartDateSummary.row(),
      GroupAccountingPeriodEndDateSummary.row()
    ).flatten
  )

  def subscriptionDataPrimaryContactList()(implicit messages: Messages, request: SubscriptionDataRequest[_]): SummaryList = SummaryListViewModel(
    rows = Seq(
      ContactNameComplianceSummary.row(),
      ContactEmailAddressSummary.row(),
      ContactByTelephoneSummary.row(),
      ContactCaptureTelephoneDetailsSummary.row()
    ).flatten
  )

  def subscriptionDataSecondaryContactList()(implicit messages: Messages, request: SubscriptionDataRequest[_]): SummaryList = SummaryListViewModel(
    rows = Seq(
      AddSecondaryContactSummary.row(),
      SecondaryContactNameSummary.row(),
      SecondaryContactEmailSummary.row(),
      SecondaryTelephonePreferenceSummary.row(),
      SecondaryTelephoneSummary.row()
    ).flatten
  )

  def subscriptionDataAddress(countryOptions: CountryOptions)(implicit
    messages:                                 Messages,
    request:                                  SubscriptionDataRequest[_]
  ): SummaryList = SummaryListViewModel(
    rows = Seq(ContactCorrespondenceAddressSummary.row(countryOptions)).flatten
  )
}
