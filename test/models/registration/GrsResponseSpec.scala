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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json
import models.registration.GrsResponse
import java.time.LocalDate
import models.grs.{BusinessVerificationResult, RegistrationStatus, VerificationStatus}

class GrsResponseSpec extends AnyFreeSpec with Matchers {

  "GrsResponse" - {
    "must decode correctly" - {
      "when JSON contains only incorporatedEntityRegistrationData" in {
        val json = Json.parse(
          """
          {
            "incorporatedEntityRegistrationData": {
              "companyProfile": {
                "companyName": "Test Company",
                "companyNumber": "12345678",
                "dateOfIncorporation": "2021-01-01",
                "unsanitisedCHROAddress": {
                  "address_line_1": "123 Test Street",
                  "address_line_2": "Test Area",
                  "country": "United Kingdom",
                  "locality": "Test City",
                  "postal_code": "TE1 1ST"
                }
              },
              "ctutr": "1234567890",
              "identifiersMatch": true,
              "businessVerification": {
                "verificationStatus": "PASS"
              },
              "registration": {
                "registrationStatus": "REGISTERED",
                "registeredBusinessPartnerId": "X1234567"
              }
            }
          }
          """
        )

        val result = json.as[GrsResponse]
        result.incorporatedEntityRegistrationData must not be None
        result.partnershipEntityRegistrationData mustBe None
        // More specific assertions
        result.incorporatedEntityRegistrationData.foreach { data =>
          data.companyProfile.companyName mustBe "Test Company"
          data.companyProfile.companyNumber mustBe "12345678"
          data.companyProfile.dateOfIncorporation mustBe Some(LocalDate.of(2021, 1, 1))
          data.companyProfile.unsanitisedCHROAddress.address_line_1 mustBe Some("123 Test Street")
          data.ctutr mustBe "1234567890"
          data.identifiersMatch mustBe true
          data.businessVerification mustBe Some(BusinessVerificationResult(VerificationStatus.Pass))
          data.registration.registrationStatus mustBe RegistrationStatus.Registered
          data.registration.registeredBusinessPartnerId mustBe Some("X1234567")
        }
      }

      "when JSON contains only partnershipEntityRegistrationData" in {
        val json = Json.parse(
          """
          {
            "partnershipEntityRegistrationData": {
              "sautr": "1234567890",
              "postcode": "AB1 2CD",
              "identifiersMatch": true,
              "businessVerification": {
                "verificationStatus": "PASS"
              },
              "registration": {
                "registrationStatus": "REGISTERED",
                "registeredBusinessPartnerId": "X1234567"
              }
            }
          }
          """
        )

        val result = json.as[GrsResponse]
        result.incorporatedEntityRegistrationData mustBe None
        result.partnershipEntityRegistrationData must not be None

        // Specific assertions about the partnershipEntityRegistrationData
        result.partnershipEntityRegistrationData.foreach { data =>
          data.sautr mustBe Some("1234567890")
          data.postcode mustBe Some("AB1 2CD")
          data.identifiersMatch mustBe true
          data.businessVerification mustBe Some(BusinessVerificationResult(VerificationStatus.Pass))
          data.registration.registrationStatus mustBe RegistrationStatus.Registered
          data.registration.registeredBusinessPartnerId mustBe Some("X1234567")
        }
      }

      "when JSON contains both entity types" in {
        val json = Json.parse(
          """
          {
            "incorporatedEntityRegistrationData": {
              "companyProfile": {
                "companyName": "Test Company",
                "companyNumber": "12345678",
                "dateOfIncorporation": "2021-01-01",
                "unsanitisedCHROAddress": {
                  "address_line_1": "123 Test Street",
                  "address_line_2": "Test Area",
                  "country": "United Kingdom",
                  "locality": "Test City",
                  "postal_code": "TE1 1ST"
                }
              },
              "ctutr": "1234567890",
              "identifiersMatch": true,
              "businessVerification": {
                "verificationStatus": "PASS"
              },
              "registration": {
                "registrationStatus": "REGISTERED",
                "registeredBusinessPartnerId": "X1234567"
              }
            },
            "partnershipEntityRegistrationData": {
              "sautr": "0987654321",
              "postcode": "XY9 8ZW",
              "identifiersMatch": true,
              "businessVerification": {
                "verificationStatus": "PASS"
              },
              "registration": {
                "registrationStatus": "REGISTERED",
                "registeredBusinessPartnerId": "Y9876543"
              }
            }
          }
          """
        )

        val result = json.as[GrsResponse]
        result.incorporatedEntityRegistrationData must not be None
        result.partnershipEntityRegistrationData  must not be None
        result.incorporatedEntityRegistrationData.foreach { data =>
          data.companyProfile.companyName mustBe "Test Company"
          data.companyProfile.companyNumber mustBe "12345678"
          data.companyProfile.dateOfIncorporation mustBe Some(LocalDate.of(2021, 1, 1))
          data.companyProfile.unsanitisedCHROAddress.address_line_1 mustBe Some("123 Test Street")
          data.ctutr mustBe "1234567890"
          data.identifiersMatch mustBe true
          data.businessVerification mustBe Some(BusinessVerificationResult(VerificationStatus.Pass))
          data.registration.registrationStatus mustBe RegistrationStatus.Registered
          data.registration.registeredBusinessPartnerId mustBe Some("X1234567")
        }
        result.partnershipEntityRegistrationData.foreach { data =>
          data.sautr mustBe Some("0987654321")
          data.postcode mustBe Some("XY9 8ZW")
          data.identifiersMatch mustBe true
          data.businessVerification mustBe Some(BusinessVerificationResult(VerificationStatus.Pass))
          data.registration.registrationStatus mustBe RegistrationStatus.Registered
          data.registration.registeredBusinessPartnerId mustBe Some("Y9876543")
        }
      }

      "when JSON is empty" in {
        val json = Json.parse("{}")

        val result = json.as[GrsResponse]
        result.incorporatedEntityRegistrationData must be(None)
        result.partnershipEntityRegistrationData  must be(None)
      }

      "when JSON contains unexpected fields" in {
        val json = Json.parse(
          """
          {
            "unexpectedField": "Some value",
            "incorporatedEntityRegistrationData": {
              "companyProfile": {
                "companyName": "Test Company",
                "companyNumber": "12345678",
                "dateOfIncorporation": "2021-01-01",
                "unsanitisedCHROAddress": {
                  "address_line_1": "123 Test Street",
                  "address_line_2": "Test Area",
                  "country": "United Kingdom",
                  "locality": "Test City",
                  "postal_code": "TE1 1ST"
                }
              },
              "ctutr": "1234567890",
              "identifiersMatch": true,
              "businessVerification": {
                "verificationStatus": "PASS"
              },
              "registration": {
                "registrationStatus": "REGISTERED",
                "registeredBusinessPartnerId": "X1234567"
              }
            }
          }
          """
        )

        val result = json.as[GrsResponse]
        result.incorporatedEntityRegistrationData must not be None
        result.partnershipEntityRegistrationData  must be(None)

        // More specific assertions
        result.incorporatedEntityRegistrationData.foreach { data =>
          data.companyProfile.companyName mustBe "Test Company"
          data.companyProfile.companyNumber mustBe "12345678"
          data.companyProfile.dateOfIncorporation mustBe Some(LocalDate.of(2021, 1, 1))
          data.companyProfile.unsanitisedCHROAddress.address_line_1 mustBe Some("123 Test Street")
          data.ctutr mustBe "1234567890"
          data.identifiersMatch mustBe true
          data.businessVerification mustBe Some(BusinessVerificationResult(VerificationStatus.Pass))
          data.registration.registrationStatus mustBe RegistrationStatus.Registered
          data.registration.registeredBusinessPartnerId mustBe Some("X1234567")
        }

        // Ensure unexpected field is ignored
        (json \ "unexpectedField").as[String] mustBe "Some value"
      }

      "when JSON contains incorporatedEntityRegistrationData without optional fields" in {
        val json = Json.parse(
          """
          {
            "incorporatedEntityRegistrationData": {
              "companyProfile": {
                "companyName": "Test Company",
                "companyNumber": "12345678",
                "unsanitisedCHROAddress": {
                  "postal_code": "TE1 1ST"
                }
              },
              "ctutr": "1234567890",
              "identifiersMatch": true,
              "registration": {
                "registrationStatus": "REGISTERED"
              }
            }
          }
          """
        )

        val result = json.as[GrsResponse]
        result.incorporatedEntityRegistrationData must not be None
        result.partnershipEntityRegistrationData mustBe None

        result.incorporatedEntityRegistrationData.foreach { data =>
          data.companyProfile.companyName mustBe "Test Company"
          data.companyProfile.companyNumber mustBe "12345678"
          data.companyProfile.dateOfIncorporation mustBe None
          data.ctutr mustBe "1234567890"
          data.identifiersMatch mustBe true
          data.businessVerification mustBe None
          data.registration.registrationStatus mustBe RegistrationStatus.Registered
          data.registration.registeredBusinessPartnerId mustBe None
        }
      }
    }
  }
}
