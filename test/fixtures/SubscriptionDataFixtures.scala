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

import models.subscription.AccountStatus.ActiveAccount
import models.subscription.{AccountingPeriodV2, ContactDetailsType, SubscriptionDataDisplay, UpeCorrespAddressDetails, UpeDetails}

import java.time.LocalDate

trait SubscriptionDataFixtures {

  val readCachedSubscriptionPath: String = "/report-pillar2-top-up-taxes/user-cache/read-subscription"

  val readSubscriptionPath:   String = "/report-pillar2-top-up-taxes/subscription/read-subscription"
  val readSubscriptionV2Path: String = "/report-pillar2-top-up-taxes/subscription/v2/read-subscription"

  val amendSubscriptionPath:   String = "/report-pillar2-top-up-taxes/subscription/amend-subscription"
  val amendSubscriptionV2Path: String = "/report-pillar2-top-up-taxes/subscription/v2/amend-subscription"

  private val upeCorrespondenceAddress = UpeCorrespAddressDetails("middle", None, Some("lane"), None, None, "obv")
  private val contactDetails           = ContactDetailsType("shadow", Some("dota2"), "shadow@fiend.com")
  private val upeDetails = UpeDetails(None, None, None, "orgName", LocalDate.of(2024, 1, 31), domesticOnly = false, filingMember = false)

  val subscriptionDataDisplay: SubscriptionDataDisplay = SubscriptionDataDisplay(
    formBundleNumber = "form bundle",
    upeDetails = upeDetails,
    upeCorrespAddressDetails = upeCorrespondenceAddress,
    primaryContactDetails = contactDetails,
    secondaryContactDetails = None,
    filingMemberDetails = None,
    accountingPeriod = Some(
      Seq(
        AccountingPeriodV2(
          startDate = None,
          endDate = None,
          dueDate = None,
          canAmendStartDate = Some(false),
          canAmendEndDate = Some(false)
        )
      )
    ),
    accountStatus = Some(ActiveAccount)
  )

  val subscriptionDataDisplayJson: String =
    """
      |{
      |  "formBundleNumber": "119000004323",
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
      |    "name": "Primary Contact",
      |    "emailAddress": "primary.contact@example.com"
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
      |     "inactive": false
      |   }
      |}
      |""".stripMargin

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
      |      { "taxObligationStartDate": "2024-01-06", "taxObligationEndDate": "2025-04-06" }
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
      |    "name": "Primary Contact",
      |    "emailAddress": "primary@example.com"
      |  }
      |}
      |""".stripMargin

  val subscriptionDataDisplayWrappedJson: String =
    s"""{ "success": $subscriptionDataDisplayJson }"""

}
