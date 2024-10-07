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

package base

import models.repayments._

trait TestData {

  val validReplaceFilingMember: String =
    s"""{
       |        "addSecondaryContact": true,
       |        "contactAddress": {
       |            "addressLine1": "83 Wingrove Road",
       |            "addressLine2": "Fenham",
       |            "addressLine3": "Newcastle upon Tyne",
       |            "addressLine4": "Tyne and Wear",
       |            "countryCode": "GB",
       |            "postalCode": "NE4 5BU"
       |        },
       |        "corporatePosition": "newNfm",
       |        "nameRegistration": "Ashley Smith",
       |        "plrReference": "XMPLR0123456789",
       |        "primaryContactEmail": "test@test.com",
       |        "primaryContactName": "Ashley Smith 2",
       |        "primaryContactPhonePreference": false,
       |        "registeredAddress": {
       |            "addressLine1": "18 Winder Drive",
       |            "addressLine2": "Hazelrigg",
       |            "addressLine3": "Newcastle upon Tyne",
       |            "addressLine4": "Tyne and Wear",
       |            "countryCode": "GB",
       |            "postalCode": "NE13 7FU"
       |        },
       |        "secondaryContactName": "Ashley Smith 3",
       |        "secondaryEmail": "ola@abc.com",
       |        "secondaryPhonePreference": false,
       |        "securityAnswerRegistrationDate": "2024-01-31",
       |        "securityAnswerUserReference": "XMPLR0123456789",
       |        "ukBased": false
       |  }
       """.stripMargin

  val validRegistrationWithIdResponseForLLP: String =
    s"""{
       |  "companyProfile" : {
       |                "companyName" : "Test Example Partnership Name",
       |                "companyNumber" : "76543210",
       |                "dateOfIncorporation" : "2010-12-12",
       |                "unsanitisedCHROAddress" : {
       |                    "address_line_1" : "Address Line 1",
       |                    "address_line_2" : "Address Line 2",
       |                    "country" : "United Kingdom",
       |                    "locality" : "Town",
       |                    "postal_code" : "AB12 3CD",
       |                    "region" : "Region"
       |                }
       |            },
       |            "sautr" : "1234567890",
       |            "postcode" : "AA11AA",
       |            "identifiersMatch" : true,
       |            "registration" : {
       |                "registrationStatus" : "REGISTERED",
       |                "registeredBusinessPartnerId" : "XB0000000000001"
       |            }
       |  }

       """.stripMargin

  val validRegistrationWithIdResponse: String =
    s"""{
       |            "companyProfile" : {
       |                "companyName" : "Test Example Company Name",
       |                "companyNumber" : "76543210",
       |                "dateOfIncorporation" : "2010-12-12",
       |                "unsanitisedCHROAddress" : {
       |                    "address_line_1" : "Address Line 1",
       |                    "address_line_2" : "Address Line 2",
       |                    "locality" : "Town",
       |                    "postal_code" : "AB12 3CD",
       |                    "region" : "Region"
       |                }
       |            },
       |            "ctutr" : "1234567890",
       |            "identifiersMatch" : true,
       |            "registration" : {
       |                "registrationStatus" : "REGISTERED",
       |                "registeredBusinessPartnerId" : "XB0000000000001"
       |            }
       |  }

       """.stripMargin

  val validRegistrationWithIdResponse2: String =
    s"""{
       |            "companyProfile" : {
       |                "companyName" : "Test Example Company Name",
       |                "companyNumber" : "76543210",
       |                "dateOfIncorporation" : "2010-12-12",
       |                "unsanitisedCHROAddress" : {
       |                    "address_line_1" : "Address Line 1",
       |                    "address_line_2" : "Address Line 2",
       |                    "locality" : "Town",
       |                    "postal_code" : "AB12 3CD",
       |                    "region" : "Region"
       |                }
       |            },
       |            "ctutr" : "1234567890",
       |            "identifiersMatch" : true,
       |            "registration" : {
       |                "registrationStatus" : "REGISTERED"
       |            }
       |  }

       """.stripMargin

  val registrationNotCalledLimited: String =
    s"""{
       |            "companyProfile" : {
       |                "companyName" : "Test Example Company Name",
       |                "companyNumber" : "76543210",
       |                "dateOfIncorporation" : "2010-12-12",
       |                "unsanitisedCHROAddress" : {
       |                    "address_line_1" : "Address Line 1",
       |                    "address_line_2" : "Address Line 2",
       |                    "locality" : "Town",
       |                    "postal_code" : "AB12 3CD",
       |                    "region" : "Region"
       |                }
       |            },
       |            "ctutr" : "1234567890",
       |            "identifiersMatch" : false,
       |            "registration" : {
       |                "registrationStatus" : "REGISTRATION_NOT_CALLED"
       |            }
       |  }

       """.stripMargin

  val registrationNotCalledLLP: String =
    s"""{
       |  "companyProfile" : {
       |                "companyName" : "Test Example Partnership Name",
       |                "companyNumber" : "76543210",
       |                "dateOfIncorporation" : "2010-12-12",
       |                "unsanitisedCHROAddress" : {
       |                    "address_line_1" : "Address Line 1",
       |                    "address_line_2" : "Address Line 2",
       |                    "country" : "United Kingdom",
       |                    "locality" : "Town",
       |                    "postal_code" : "AB12 3CD",
       |                    "region" : "Region"
       |                }
       |            },
       |            "sautr" : "1234567890",
       |            "postcode" : "AA11AA",
       |            "identifiersMatch" : false,
       |            "registration" : {
       |                "registrationStatus" : "REGISTRATION_NOT_CALLED"
       |            }
       |  }
       """.stripMargin

  val registrationFailedLimitedJs: String =
    s"""{
       |            "companyProfile" : {
       |                "companyName" : "Test Example Company Name",
       |                "companyNumber" : "76543210",
       |                "dateOfIncorporation" : "2010-12-12",
       |                "unsanitisedCHROAddress" : {
       |                    "address_line_1" : "Address Line 1",
       |                    "address_line_2" : "Address Line 2",
       |                    "locality" : "Town",
       |                    "postal_code" : "AB12 3CD",
       |                    "region" : "Region"
       |                }
       |            },
       |            "ctutr" : "1234567890",
       |            "identifiersMatch" : true,
       |            "registration" : {
       |                "registrationStatus" : "REGISTRATION_FAILED"
       |            }
       |  }

       """.stripMargin

  val registrationFailedLLPJs: String =
    s"""{
       |  "companyProfile" : {
       |                "companyName" : "Test Example Partnership Name",
       |                "companyNumber" : "76543210",
       |                "dateOfIncorporation" : "2010-12-12",
       |                "unsanitisedCHROAddress" : {
       |                    "address_line_1" : "Address Line 1",
       |                    "address_line_2" : "Address Line 2",
       |                    "country" : "United Kingdom",
       |                    "locality" : "Town",
       |                    "postal_code" : "AB12 3CD",
       |                    "region" : "Region"
       |                }
       |            },
       |            "sautr" : "1234567890",
       |            "postcode" : "AA11AA",
       |            "identifiersMatch" : true,
       |            "registration" : {
       |                "registrationStatus" : "REGISTRATION_FAILED"
       |            }
       |  }
       """.stripMargin

  val validRegistrationWithIdResponseForLLPWithoutPartnerId: String =
    s"""{
       |  "companyProfile" : {
       |                "companyName" : "Test Example Partnership Name",
       |                "companyNumber" : "76543210",
       |                "dateOfIncorporation" : "2010-12-12",
       |                "unsanitisedCHROAddress" : {
       |                    "address_line_1" : "Address Line 1",
       |                    "address_line_2" : "Address Line 2",
       |                    "country" : "United Kingdom",
       |                    "locality" : "Town",
       |                    "postal_code" : "AB12 3CD",
       |                    "region" : "Region"
       |                }
       |            },
       |            "sautr" : "1234567890",
       |            "postcode" : "AA11AA",
       |            "identifiersMatch" : true,
       |            "registration" : {
       |                "registrationStatus" : "REGISTERED"
       |            }
       |  }

       """.stripMargin

  val validRegistrationWithIdResponseWithoutPartnerId: String =
    s"""{
       |            "companyProfile" : {
       |                "companyName" : "Test Example Company Name",
       |                "companyNumber" : "76543210",
       |                "dateOfIncorporation" : "2010-12-12",
       |                "unsanitisedCHROAddress" : {
       |                    "address_line_1" : "Address Line 1",
       |                    "address_line_2" : "Address Line 2",
       |                    "locality" : "Town",
       |                    "postal_code" : "AB12 3CD",
       |                    "region" : "Region"
       |                }
       |            },
       |            "ctutr" : "1234567890",
       |            "identifiersMatch" : true,
       |            "registration" : {
       |                "registrationStatus" : "REGISTERED"
       |            }
       |  }

       """.stripMargin

  val expectedKnownFactsResponse: String =
    """
      |{
      |    "service": "HMRC-PILLAR2-ORG",
      |    "enrolments": [{
      |        "identifiers": [{
      |            "key": "PLRID",
      |            "value": "XLMP123456789"
      |        }],
      |        "verifiers": [{
      |            "key": "NonUKPostalCode",
      |            "value": "SE1232113"
      |        },
      |        {
      |            "key": "CountryCode",
      |            "value": "AR"
      |        }]
      |    }]
      |}
      |""".stripMargin

  val badKnownFactsResponse: String =
    """
      |{
      |    "enrolments": [{
      |        "identifiers": [{
      |            "key": "PLRID",
      |            "value": "XLMP123456789"
      |        }],
      |        "verifiers": [{
      |            "value": "SE1232113"
      |        },
      |        {
      |            "value": "AR"
      |        }]
      |    }]
      |}
      |""".stripMargin
  val knownFactsRequest: String =
    """
      |{
      |    "service": "HMRC-PILLAR2-ORG",
      |    "knownFacts": [
      |        {
      |            "key": "PLRID",
      |            "value": "XLMP123456789"
      |        }
      |    ]
      |}
      |""".stripMargin

  val validRepaymentPayloadUkBank: SendRepaymentDetails = SendRepaymentDetails(
    repaymentDetails = RepaymentDetails(plrReference = "plrReference", name = "name", utr = None, reasonForRepayment = "???", refundAmount = 10000.1),
    bankDetails = BankDetails(
      nameOnBankAccount = "Paddington",
      bankName = "Bank of Bears",
      sortCode = Some("666666"),
      accountNumber = Some("00000000"),
      iban = None,
      bic = None,
      countryCode = None
    ),
    contactDetails = RepaymentContactDetails(contactDetails = "name, paddington@peru.com, marmalade sandwich")
  )

  val validRepaymentPayloadNonUkBank: SendRepaymentDetails = validRepaymentPayloadUkBank.copy(bankDetails =
    BankDetails(
      nameOnBankAccount = "Paddington",
      bankName = "Bank of Bears",
      sortCode = None,
      accountNumber = None,
      iban = Some("123132"),
      bic = Some("11111111"),
      countryCode = None
    )
  )

  val validRepaymentDetails: String =
    s"""{
       |        "nonUKBank": {
       |            "bankName": "Bank of UAE",
       |            "bic": "HBUKGB4B",
       |            "iban": "GB29NWBK60161331926819",
       |            "nameOnBankAccount": "Bank Account Name"
       |        },
       |        "reasonForRequestingRefund": "Reason for refund",
       |        "refundAmount": 100.99,
       |        "repaymentsContactByPhone": true,
       |        "repaymentsContactEmail": "test@test.com",
       |        "repaymentsContactName": "Contact name",
       |        "repaymentsTelephoneDetails": "01234567890",
       |        "ukOrAbroadBankAccount": "nonUkBankAccount"
       |  }
       """.stripMargin

}
