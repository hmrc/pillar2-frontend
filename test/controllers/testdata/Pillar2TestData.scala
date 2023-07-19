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

import models.{ContactUPEByTelephone, UPERegisteredInUKConfirmation, UpeRegisteredAddress, UserAnswers}
import models.grs.{EntityType, GrsCreateRegistrationResponse}
import models.registration.{GrsResponse, IncorporatedEntityRegistrationData, PartnershipEntityRegistrationData, Registration, WithoutIdRegData}
import play.api.libs.json.{JsObject, Json}
import utils.RowStatus

trait Pillar2TestData {

  def validNoIdRegData(
    isUPERegisteredInUK:   UPERegisteredInUKConfirmation = UPERegisteredInUKConfirmation.No,
    isRegistrationStatus:  RowStatus = RowStatus.InProgress,
    upeNameRegistration:   String = "Test Name",
    upeContactName:        Option[String] = Some("TestName"),
    contactUpeByTelephone: Option[ContactUPEByTelephone] = Some(ContactUPEByTelephone.Yes),
    telephoneNumber:       Option[String] = Some("1234567"),
    emailAddress:          Option[String] = Some("test@test.com"),
    addressLine1:          String = "Line1",
    addressLine2:          Option[String] = Some("Line2"),
    addressLine3:          String = "Line3",
    addressLine4:          Option[String] = Some("Line4"),
    postalCode:            Option[String] = Some("VR11 3PA"),
    countryCode:           String = "GB"
  ) =
    new Registration(
      isUPERegisteredInUK = isUPERegisteredInUK,
      isRegistrationStatus = isRegistrationStatus,
      withoutIdRegData = Some(
        WithoutIdRegData(
          upeNameRegistration = upeNameRegistration,
          upeContactName = upeContactName,
          contactUpeByTelephone = contactUpeByTelephone,
          telephoneNumber = telephoneNumber,
          emailAddress = emailAddress,
          upeRegisteredAddress = Some(
            UpeRegisteredAddress(
              addressLine1 = addressLine1,
              addressLine2 = addressLine2,
              addressLine3 = addressLine3,
              addressLine4 = addressLine4,
              postalCode = postalCode,
              countryCode = countryCode
            )
          )
        )
      )
    )

  def validWithoutIdRegData(
    isUPERegisteredInUK:  UPERegisteredInUKConfirmation = UPERegisteredInUKConfirmation.No,
    isRegistrationStatus: RowStatus = RowStatus.InProgress
  ) =
    new Registration(
      isUPERegisteredInUK = isUPERegisteredInUK,
      isRegistrationStatus = isRegistrationStatus,
      withoutIdRegData = None
    )

  val validUpeRegisteredAddressed = new UpeRegisteredAddress(
    addressLine1 = "Line1",
    addressLine2 = Some("Line2"),
    addressLine3 = "Line3",
    addressLine4 = Some("Line4"),
    postalCode = Some("VR11 3PA"),
    countryCode = "GB"
  )

  val validIdRegistrationData =
    new Registration(
      isUPERegisteredInUK = UPERegisteredInUKConfirmation.Yes,
      isRegistrationStatus = RowStatus.InProgress,
      orgType = Some(EntityType.UkLimitedCompany),
      withIdRegData = Some(
        GrsResponse(incorporatedEntityRegistrationData = Some(Json.parse(validRegistrationWithIdResponse()).as[IncorporatedEntityRegistrationData]))
      )
    )
  val validIdRegistrationDataWithNoOrgType =
    new Registration(
      isUPERegisteredInUK = UPERegisteredInUKConfirmation.Yes,
      isRegistrationStatus = RowStatus.InProgress,
      orgType = None,
      withIdRegData = None
    )

  val validGrsResponse = new GrsResponse(
    incorporatedEntityRegistrationData = Some(Json.parse(validRegistrationWithIdResponse()).as[IncorporatedEntityRegistrationData])
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
