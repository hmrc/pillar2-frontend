/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.testdata

import models.fm.{ContactNFMByTelephone, FilingMember, NfmRegisteredAddress, WithoutIdNfmData}
import models.grs.GrsCreateRegistrationResponse
import models.registration.{IncorporatedEntityRegistrationData, PartnershipEntityRegistrationData, Registration, WithoutIdRegData}
import models.{ContactUPEByTelephone, NfmRegistrationConfirmation, UPERegisteredInUKConfirmation, UpeRegisteredAddress, UserAnswers}
import play.api.libs.json.{JsObject, Json}
import utils.RowStatus

trait Pillar2TestData {

  val validNoIdRegistrationData =
    new Registration(
      isUPERegisteredInUK = UPERegisteredInUKConfirmation.No,
      isRegistrationStatus = RowStatus.InProgress,
      withoutIdRegData = Some(
        WithoutIdRegData(
          upeNameRegistration = "Test Name",
          contactUpeByTelephone = Some(ContactUPEByTelephone.No),
          telephoneNumber = Some("1234567"),
          upeRegisteredAddress = Some(
            UpeRegisteredAddress(
              addressLine1 = "Line1",
              addressLine2 = Some("Line2"),
              addressLine3 = "Line3",
              addressLine4 = Some("Line4"),
              postalCode = Some("VR11 3PA"),
              countryCode = "GB"
            )
          )
        )
      )
    )

  val validNoIdNfmData = new FilingMember(
    NfmRegistrationConfirmation.Yes,
    None,
    None,
    RowStatus.InProgress,
    None,
    withoutIdRegData = Some(
      WithoutIdNfmData(
        "Test FM",
        registeredFmNameAddress = Some(
          NfmRegisteredAddress(
            "Line1",
            Some("Line2"),
            "Line3",
            Some("Line4"),
            Some("VR11 3PA"),
            "GB"
          )
        ),
        fmContactName = Some("Ashley Smith"),
        fmEmailAddress = Some("AshleySmith@email.com"),
        Some(ContactNFMByTelephone.No),
        None
      )
    )
  )

  val validUpeRegisteredAddressed = new UpeRegisteredAddress(
    addressLine1 = "Line1",
    addressLine2 = Some("Line2"),
    addressLine3 = "Line3",
    addressLine4 = Some("Line4"),
    postalCode = Some("VR11 3PA"),
    countryCode = "GB"
  )

  val validRegisterWithIdResponse = Json.parse(validRegistrationWithIdResponse()).as[IncorporatedEntityRegistrationData]

  val validRegisterWithIdResponseForLLP = Json.parse(validRegistrationWithIdResponseForLLP()).as[PartnershipEntityRegistrationData]

  val validUserAnswersGrsDataForLimitedCompany = UserAnswers(
    "testId",
    data = Json.parse(validDataObjectForUKLimtedCompany).as[JsObject]
  )
  val validUserAnswersGrsDataForLLP = UserAnswers(
    "testId",
    data = Json.parse(validDataObjectForLLP()).as[JsObject]
  )
  val validGrsCreateRegistrationResponse = new GrsCreateRegistrationResponse("http://journey-start")

  def validRegistrationWithIdResponse(): String =
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

  def validRegistrationWithIdResponseForLLP(): String =
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

  def validDataObjectForUKLimtedCompany(): String =
    s"""
      {
       |        "Registration" : {
       |            "isUPERegisteredInUK" : "yes",
       |            "isRegistrationStatus" : "InProgress",
       |            "orgType" : "ukLimitedCompany",
       |            "withIdRegData" : {
       |                "incorporatedEntityRegistrationData" : {
       |                    "companyProfile" : {
       |                        "companyName" : "Test Example Company Name",
       |                        "companyNumber" : "76543210",
       |                        "dateOfIncorporation" : "2010-12-12",
       |                        "unsanitisedCHROAddress" : {
       |                            "address_line_1" : "Address Line 1",
       |                            "address_line_2" : "Address Line 2",
       |                            "country" : "United Kingdom",
       |                            "locality" : "Town",
       |                            "postal_code" : "AB12 3CD",
       |                            "region" : "Region"
       |                        }
       |                    },
       |                    "ctutr" : "1234567890",
       |                    "identifiersMatch" : true,
       |                    "registration" : {
       |                        "registrationStatus" : "REGISTERED",
       |                        "registeredBusinessPartnerId" : "XB0000000000001"
       |                    }
       |                }
       |            }
       |        }
       |    }
       """.stripMargin

  def validDataObjectForLLP(): String =
    s"""
       |{
       |        "Registration" : {
       |            "isUPERegisteredInUK" : "yes",
       |            "isRegistrationStatus" : "InProgress",
       |            "orgType" : "limitedLiabilityPartnership",
       |            "withIdRegData" : {
       |                "partnershipEntityRegistrationData" : {
       |                    "companyProfile" : {
       |                        "companyName" : "Test Example Partnership Name",
       |                        "companyNumber" : "76543210",
       |                        "dateOfIncorporation" : "2010-12-12",
       |                        "unsanitisedCHROAddress" : {
       |                            "address_line_1" : "Address Line 1",
       |                            "address_line_2" : "Address Line 2",
       |                            "country" : "United Kingdom",
       |                            "locality" : "Town",
       |                            "postal_code" : "AB12 3CD",
       |                            "region" : "Region"
       |                        }
       |                    },
       |                    "sautr" : "1234567890",
       |                    "postcode" : "AA11AA",
       |                    "identifiersMatch" : true,
       |                    "registration" : {
       |                        "registrationStatus" : "REGISTERED",
       |                        "registeredBusinessPartnerId" : "XB0000000000001"
       |                    }
       |                }
       |            }
       |        }
       |    }
       """.stripMargin

}
