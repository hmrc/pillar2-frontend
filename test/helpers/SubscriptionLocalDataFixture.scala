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
import models.rfm.CorporatePosition
import models.subscription.{AccountStatus, AccountingPeriod, AccountingPeriodAmend, AmendSubscription, ContactDetailsType, FilingMemberAmendDetails, NewFilingMemberDetail, SubscriptionData, SubscriptionLocalData, UpeCorrespAddressDetails, UpeDetails, UpeDetailsAmend}
import models.{MneOrDomestic, NonUKAddress, Verifier}

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

  val emptySubscriptionLocalData: SubscriptionLocalData = SubscriptionLocalData(
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
  lazy val currentDate: LocalDate = LocalDate.now()
  val subscriptionData: SubscriptionData = SubscriptionData(
    formBundleNumber = "form bundle",
    upeDetails = UpeDetails(None, None, None, "orgName", LocalDate.of(2024, 1, 31), domesticOnly = false, filingMember = false),
    upeCorrespAddressDetails = upeCorrespondenceAddress,
    primaryContactDetails = contactDetails,
    secondaryContactDetails = None,
    filingMemberDetails = None,
    accountingPeriod = AccountingPeriod(currentDate, currentDate),
    accountStatus = Some(AccountStatus(false))
  )

  val allocateEnrolmentParameters: AllocateEnrolmentParameters = AllocateEnrolmentParameters(
    userId = "id",
    verifiers = Seq(Verifier("CTUTR", "Utr"), Verifier("CRN", "Crn"))
  )

  val amendData: AmendSubscription = AmendSubscription(
    upeDetails = upeDetailsAmend,
    accountingPeriod = AccountingPeriodAmend(currentDate, currentDate),
    upeCorrespAddressDetails = upeCorrespondenceAddress,
    primaryContactDetails = contactDetails,
    secondaryContactDetails = Some(contactDetails),
    filingMemberDetails = Some(filingMemberAmendDetails)
  )
  val replaceFilingMemberData: NewFilingMemberDetail = NewFilingMemberDetail(
    plrReference = "plrReference",
    corporatePosition = CorporatePosition.Upe,
    contactName = "shadow",
    contactEmail = "shadow@fiend.com",
    phoneNumber = Some("dota2"),
    address = NonUKAddress("middle", None, "lane", None, None, "obv"),
    secondaryContactInformation = Some(contactDetails)
  )
}
