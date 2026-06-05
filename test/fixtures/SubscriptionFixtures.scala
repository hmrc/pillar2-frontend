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
import play.api.libs.json.{JsValue, Json}

import java.time.LocalDate

trait SubscriptionFixtures {

  Json.parse("""
    {
      "formBundleNumber": "119000004323",
      "upeDetails": {
        "safeId": null,
        "customerIdentification1": "12345678",
        "customerIdentification2": "12345678",
        "organisationName": "UK Only Organisation Ltd",
        "registrationDate": "2024-01-31",
        "domesticOnly": true,
        "filingMember": false
      },
      "upeCorrespAddressDetails": {
        "addressLine1": "1 High Street",
        "addressLine2": "Egham",
        "addressLine3": "Wycombe",
        "addressLine4": "Surrey",
        "postCode": "HP13 6TT",
        "countryCode": "GB"
      },
      "primaryContactDetails": {
        "name": "Primary Contact",
        "telephone": "0115 9700 700",
        "emailAddress": "primary.contact@example.com"
      },
      "secondaryContactDetails": null,
      "filingMemberDetails": null,
      "accountingPeriod": [
        {
          "startDate": "2024-01-06",
          "endDate":   "2025-04-06",
          "dueDate":   "2024-04-06",
          "canAmendStartDate": true,
          "canAmendEndDate": true
        }
      ],
      "accountStatus": {
        "inactive": false
      }
    }
  """)

  private val upeCorrespondenceAddress = UpeCorrespAddressDetails("middle", None, Some("lane"), None, None, "obv")

  private val contactDetails = ContactDetailsType("shadow", Some("dota2"), "shadow@fiend.com")

  val subscriptionDataV1: SubscriptionData = SubscriptionData(
    formBundleNumber = "form bundle",
    upeDetails = UpeDetails(None, None, None, "orgName", LocalDate.of(2024, 1, 31), domesticOnly = false, filingMember = false),
    upeCorrespAddressDetails = upeCorrespondenceAddress,
    primaryContactDetails = contactDetails,
    secondaryContactDetails = None,
    filingMemberDetails = None,
    accountingPeriod = AccountingPeriod(LocalDate.of(2024, 1, 31), LocalDate.of(2024, 1, 31).plusYears(1)),
    accountStatus = Some(ActiveAccount)
  )

  val subscriptionDataV1Json: JsValue = Json.parse("""
    {
      "formBundleNumber": "119000004323",
      "upeDetails": {
        "safeId": null,
        "customerIdentification1": "12345678",
        "customerIdentification2": "12345678",
        "organisationName": "UK Only Organisation Ltd",
        "registrationDate": "2024-01-31",
        "domesticOnly": true,
        "filingMember": false
      },
      "upeCorrespAddressDetails": {
        "addressLine1": "1 High Street",
        "addressLine2": "Egham",
        "addressLine3": "Wycombe",
        "addressLine4": "Surrey",
        "postCode": "HP13 6TT",
        "countryCode": "GB"
      },
      "primaryContactDetails": {
        "name": "Primary Contact",
        "telephone": "0115 9700 700",
        "emailAddress": "primary.contact@example.com"
      },
      "secondaryContactDetails": null,
      "filingMemberDetails": null,
      "accountingPeriod": {
        "startDate": "2024-01-31",
        "endDate": "2025-01-31"
      },
      "accountStatus": {
        "inactive": false
      }
    }
    """)

  val subscriptionDataV2Json: JsValue = Json.parse("""
    {
      "formBundleNumber": "119000004323",
      "upeDetails": {
        "safeId": null,
        "customerIdentification1": "12345678",
        "customerIdentification2": "12345678",
        "organisationName": "UK Only Organisation Ltd",
        "registrationDate": "2024-01-31",
        "domesticOnly": true,
        "filingMember": false
      },
      "upeCorrespAddressDetails": {
        "addressLine1": "1 High Street",
        "addressLine2": "Egham",
        "addressLine3": "Wycombe",
        "addressLine4": "Surrey",
        "postCode": "HP13 6TT",
        "countryCode": "GB"
      },
      "primaryContactDetails": {
        "name": "Primary Contact",
        "telephone": "0115 9700 700",
        "emailAddress": "primary.contact@example.com"
      },
      "secondaryContactDetails": null,
      "filingMemberDetails": null,
      "accountingPeriod": [
        {
          "startDate": "2024-01-06",
          "endDate":   "2025-04-06",
          "dueDate":   "2025-04-06",
          "canAmendStartDate": true,
          "canAmendEndDate": true
        }
      ],
      "accountStatus": {
        "inactive": false
      }
    }
    """)

  // FIXME: this is v2
  val misshapedSubscriptionDataV1Json: JsValue = Json.parse("""
    {
      "formBundleNumber": "119000004323",
      "upeDetails": {
        "safeId": null,
        "customerIdentification1": "12345678",
        "customerIdentification2": "12345678",
        "organisationName": "UK Only Organisation Ltd",
        "registrationDate": "2024-01-31",
        "domesticOnly": true,
        "filingMember": false
      },
      "upeCorrespAddressDetails": {
        "addressLine1": "1 High Street",
        "addressLine2": "Egham",
        "addressLine3": "Wycombe",
        "addressLine4": "Surrey",
        "postCode": "HP13 6TT",
        "countryCode": "GB"
      },
      "primaryContactDetails": {
        "name": "Primary Contact",
        "telephone": "0115 9700 700",
        "emailAddress": "primary.contact@example.com"
      },
      "secondaryContactDetails": null,
      "filingMemberDetails": null,
      "accountingPeriod": [
        {
          "startDate": "2024-01-06",
          "endDate":   "2025-04-06",
          "dueDate":   "2024-04-06",
          "canAmendStartDate": true,
          "canAmendEndDate": true
        }
      ],
      "accountStatus": {
        "inactive": false
      }
    }
    """)

  // FIXME: this is v1
  val misshapedSubscriptionDataV2Json: JsValue = Json.parse("""
    {
      "formBundleNumber": "119000004323",
      "upeDetails": {
        "safeId": null,
        "customerIdentification1": "12345678",
        "customerIdentification2": "12345678",
        "organisationName": "UK Only Organisation Ltd",
        "registrationDate": "2024-01-31",
        "domesticOnly": true,
        "filingMember": false
      },
      "upeCorrespAddressDetails": {
        "addressLine1": "1 High Street",
        "addressLine2": "Egham",
        "addressLine3": "Wycombe",
        "addressLine4": "Surrey",
        "postCode": "HP13 6TT",
        "countryCode": "GB"
      },
      "primaryContactDetails": {
        "name": "Primary Contact",
        "telephone": "0115 9700 700",
        "emailAddress": "primary.contact@example.com"
      },
      "secondaryContactDetails": null,
      "filingMemberDetails": null,
      "accountingPeriod": {
        "startDate": "2024-01-31",
        "endDate": "2025-01-31"
      },
      "accountStatus": {
        "inactive": false
      }
    }
    """)

  val subscriptionSuccessV1Json: JsValue = Json.parse("""
    {
      "success": {
        "formBundleNumber": "form bundle",
        "upeDetails": {
          "organisationName": "orgName",
          "registrationDate": "2024-01-31",
          "domesticOnly": false,
          "filingMember": false
        },
        "upeCorrespAddressDetails": {
          "addressLine1": "middle",
          "addressLine3": "lane",
          "countryCode": "obv"
        },
        "primaryContactDetails": {
          "name": "shadow",
          "telephone": "dota2",
          "emailAddress": "shadow@fiend.com"
        },
        "accountingPeriod": {
          "startDate": "2024-01-31",
          "endDate": "2025-01-31"
        },
        "accountStatus": {
          "inactive": false
        }
      }
    }
    """)

  val subscriptionSuccessV2Json: JsValue = Json.parse("""
    {
      "success": {
        "formBundleNumber": "119000004323",
        "upeDetails": {
          "safeId": null,
          "customerIdentification1": "12345678",
          "customerIdentification2": "12345678",
          "organisationName": "UK Only Organisation Ltd",
          "registrationDate": "2024-01-31",
          "domesticOnly": true,
          "filingMember": false
        },
        "upeCorrespAddressDetails": {
          "addressLine1": "1 High Street",
          "addressLine2": "Egham",
          "addressLine3": "Wycombe",
          "addressLine4": "Surrey",
          "postCode": "HP13 6TT",
          "countryCode": "GB"
        },
        "primaryContactDetails": {
          "name": "Primary Contact",
          "telephone": "0115 9700 700",
          "emailAddress": "primary.contact@example.com"
        },
        "secondaryContactDetails": null,
        "filingMemberDetails": null,
        "accountingPeriod": [
          {
            "startDate": "2024-01-06",
            "endDate":   "2025-04-06",
            "dueDate":   "2024-04-06",
            "canAmendStartDate": true,
            "canAmendEndDate": true
          }
        ],
        "accountStatus": {
          "inactive": false
        }
      }
    }
    """)

}
