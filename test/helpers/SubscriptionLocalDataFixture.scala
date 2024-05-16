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
import play.api.i18n.Messages
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers.manageAccount._

import java.time.LocalDate
import viewmodels.govuk.summarylist.SummaryListViewModel

trait SubscriptionLocalDataFixture {

  lazy val currentDate = LocalDate.now()

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

  val someSubscriptionLocalData = SubscriptionLocalData(
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

  val subscriptionData = SubscriptionData(
    formBundleNumber = "form bundle",
    upeDetails = UpeDetails(None, None, None, "orgName", LocalDate.of(2024, 1, 31), domesticOnly = false, filingMember = false),
    upeCorrespAddressDetails = UpeCorrespAddressDetails("line1", None, None, None, None, "GB"),
    primaryContactDetails = ContactDetailsType("name", None, "email"),
    secondaryContactDetails = None,
    filingMemberDetails = None,
    accountingPeriod = AccountingPeriod(currentDate, currentDate.plusYears(1)),
    accountStatus = Some(AccountStatus(false))
  )

  def subscriptionDataGroupSummaryList(implicit messages: Messages) = SummaryListViewModel(
    rows = Seq(
      MneOrDomesticSummary.row(someSubscriptionLocalData),
      GroupAccountingPeriodSummary.row(someSubscriptionLocalData),
      GroupAccountingPeriodStartDateSummary.row(someSubscriptionLocalData),
      GroupAccountingPeriodEndDateSummary.row(someSubscriptionLocalData)
    ).flatten
  )

  def subscriptionDataPrimaryContactList(implicit messages: Messages) = SummaryListViewModel(
    rows = Seq(
      ContactNameComplianceSummary.row(someSubscriptionLocalData),
      ContactEmailAddressSummary.row(someSubscriptionLocalData),
      ContactByTelephoneSummary.row(someSubscriptionLocalData),
      ContactCaptureTelephoneDetailsSummary.row(someSubscriptionLocalData)
    ).flatten
  )

  def subscriptionDataSecondaryContactList(implicit messages: Messages) = SummaryListViewModel(
    rows = Seq(
      AddSecondaryContactSummary.row(someSubscriptionLocalData),
      SecondaryContactNameSummary.row(someSubscriptionLocalData),
      SecondaryContactEmailSummary.row(someSubscriptionLocalData),
      SecondaryTelephonePreferenceSummary.row(someSubscriptionLocalData),
      SecondaryTelephoneSummary.row(someSubscriptionLocalData)
    ).flatten
  )

  def subscriptionDataAddress(countryOptions: CountryOptions)(implicit messages: Messages) = SummaryListViewModel(
    rows = Seq(ContactCorrespondenceAddressSummary.row(someSubscriptionLocalData, countryOptions)).flatten
  )
}
