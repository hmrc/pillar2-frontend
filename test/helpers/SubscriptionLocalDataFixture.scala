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

import models.subscription.{AccountStatus, AccountingPeriod, ContactDetailsType, SubscriptionData, SubscriptionLocalData, UpeCorrespAddressDetails, UpeDetails}
import models.{MneOrDomestic, NonUKAddress}

import java.time.LocalDate

trait SubscriptionLocalDataFixture {

  val emptySubscriptionLocalData = SubscriptionLocalData(
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
  private lazy val date = LocalDate.now()
  val subscriptionData = SubscriptionData(
    formBundleNumber = "form bundle",
    upeDetails = UpeDetails(None, None, None, "orgName", date, domesticOnly = false, filingMember = false),
    upeCorrespAddressDetails = UpeCorrespAddressDetails("line1", None, None, None, None, "GB"),
    primaryContactDetails = ContactDetailsType("name", None, "email"),
    secondaryContactDetails = None,
    filingMemberDetails = None,
    accountingPeriod = AccountingPeriod(date, date),
    accountStatus = Some(AccountStatus(false))
  )
}
