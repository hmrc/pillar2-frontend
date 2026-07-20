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

import models.subscription.*
import models.subscription.AccountStatus.ActiveAccount
import models.subscription.responses.SubscriptionResponse

import java.time.LocalDate

trait SubscriptionDataFixtures {

  val createSubscriptionPath: String = "/report-pillar2-top-up-taxes/subscription/create-subscription"

  val readCachedSubscriptionPath: String = "/report-pillar2-top-up-taxes/user-cache/read-subscription"

  val readSubscriptionPath:   String = "/report-pillar2-top-up-taxes/subscription/read-subscription"
  val readSubscriptionV2Path: String = "/report-pillar2-top-up-taxes/subscription/v2/read-subscription"

  val amendSubscriptionPath:   String = "/report-pillar2-top-up-taxes/subscription/amend-subscription"
  val amendSubscriptionV2Path: String = "/report-pillar2-top-up-taxes/subscription/v2/amend-subscription"

  val testFormBundleNumber: String = "123456789012"

  private val upeDetails =
    UpeDetails(
      safeId = None,
      customerIdentification1 = None,
      customerIdentification2 = None,
      organisationName = "UK Only Organisation Ltd",
      registrationDate = LocalDate.of(2024, 1, 31),
      domesticOnly = true,
      filingMember = false
    )

  private val upeCorrespAddressDetails =
    UpeCorrespAddressDetails(
      addressLine1 = "1 High Street",
      addressLine2 = None,
      addressLine3 = None,
      addressLine4 = None,
      postCode = None,
      countryCode = "GB"
    )

  private val primaryContactDetails =
    ContactDetailsType(
      name = "Primary Contact",
      phone = None,
      emailAddress = "primary.contact@example.com"
    )

  val amendableAccountingPeriod: AccountingPeriodV2 =
    AccountingPeriodV2(
      startDate = Some(LocalDate.of(2024, 1, 6)),
      endDate = Some(LocalDate.of(2025, 4, 6)),
      dueDate = Some(LocalDate.of(2024, 4, 6)),
      canAmendStartDate = Some(true),
      canAmendEndDate = Some(true)
    )

  val subscriptionDataDisplay: SubscriptionDataDisplay = SubscriptionDataDisplay(
    formBundleNumber = "123456789012",
    upeDetails = upeDetails,
    upeCorrespAddressDetails = upeCorrespAddressDetails,
    primaryContactDetails = primaryContactDetails,
    secondaryContactDetails = None,
    filingMemberDetails = None,
    accountingPeriod = Some(Seq(amendableAccountingPeriod)),
    accountStatus = Some(ActiveAccount)
  )

  val subscriptionDataDisplayJson: String =
    """
      |{
      |  "formBundleNumber": "123456789012",
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
      |    "name": "Primary Contact",
      |    "emailAddress": "primary@example.com"
      |  }
      |}
      |""".stripMargin

  val subscriptionDataDisplayWrappedJson: String =
    s"""{ "success": $subscriptionDataDisplayJson }"""

  val businessSubscriptionSuccessResponseJson: String =
    """
      |{
      |  "success" : {
      |    "plrReference": "XMPLR0012345678",
      |    "formBundleNumber": "119000004320",
      |    "processingDate": "2023-09-22T00:00"
      |  }
      |}""".stripMargin

  val validBusinessSubscriptionSuccessResponse: SubscriptionResponse =
    SubscriptionResponse(
      plrReference = "XMPLR0012345678",
      formBundleNumber = "119000004320",
      processingDate = LocalDate.parse("2023-09-22").atStartOfDay()
    )

  val validSubscriptionCreateParameter: SubscriptionRequestParameters =
    SubscriptionRequestParameters(
      id = "id",
      regSafeId = "regSafeId",
      fmSafeId = Some("fmSafeId")
    )

  val businessSubscriptionMissingPlrRefJson: String =
    """
      |{
      |  "failure" : {
      |    "formBundleNumber": "119000004320",
      |    "processingDate": "2023-09-22"
      |  }
      |}""".stripMargin

  val unsuccessfulNotFoundJson: String =
    """
      |{
      |  "status": "404",
      |  "error": "there is nothing here"
      |}""".stripMargin

  val unsuccessfulResponseJson: String = """{ "status": "error" }"""

}
