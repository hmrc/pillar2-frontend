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

package service

import models.UpeRegisteredAddress
import models.errors._
import models.fm.{FilingMember, NfmRegisteredAddress}
import models.grs.EntityType
import models.registration.Registration
import play.api.Logging
import utils.countryOptions.CountryOptions

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SubscriptionService @Inject() (
  countryOptions: CountryOptions
)(implicit
  ec: ExecutionContext
) extends Logging {

  def getUpeAddressDetails(registration: Registration): Either[RegistrationError, UpeRegisteredAddress] =
    registration.isUPERegisteredInUK match {
      case true =>
        registration.orgType match {
          case Some(EntityType.UkLimitedCompany) =>
            for {
              withIdData <- registration.withIdRegData.toRight(MalformedDataError("UPE: Malformed WithId data for UK Limited Company"))
              incorporatedEntityData <- withIdData.incorporatedEntityRegistrationData.toRight(
                                          MalformedDataError("UPE: Malformed IncorporatedEntityRegistrationData for UK Limited Company")
                                        )
            } yield {
              val address = incorporatedEntityData.companyProfile.unsanitisedCHROAddress
              UpeRegisteredAddress(
                addressLine1 = address.address_line_1.getOrElse(""),
                addressLine2 = address.address_line_2,
                addressLine3 = address.premises.getOrElse(""),
                addressLine4 = address.region,
                postalCode = address.postal_code,
                countryCode = address.country.map(countryOptions.getCountryNameFromCode).getOrElse("")
              )
            }
          case Some(EntityType.LimitedLiabilityPartnership) =>
            for {
              withIdData <- registration.withIdRegData.toRight(MalformedDataError("Malformed withId data for LLP"))
              partnershipEntityRegistrationData <-
                withIdData.partnershipEntityRegistrationData.toRight(MalformedDataError("Malformed partnershipEntityRegistrationData data"))
              companyProfile <- partnershipEntityRegistrationData.companyProfile.toRight(MalformedDataError("Malformed Company profile data in LLP"))
            } yield UpeRegisteredAddress(
              addressLine1 = companyProfile.unsanitisedCHROAddress.address_line_1.getOrElse(""),
              addressLine2 = companyProfile.unsanitisedCHROAddress.address_line_2,
              addressLine3 = companyProfile.unsanitisedCHROAddress.premises.getOrElse(""),
              addressLine4 = companyProfile.unsanitisedCHROAddress.region,
              postalCode = companyProfile.unsanitisedCHROAddress.postal_code,
              countryCode = companyProfile.unsanitisedCHROAddress.country.map(countryOptions.getCountryNameFromCode).getOrElse("")
            )
          case _ =>
            Left(InvalidOrgTypeError())
        }

      case false =>
        for {
          withoutIdData <- registration.withoutIdRegData.toRight(MalformedDataError("Malformed withoutIdReg data"))
          address       <- withoutIdData.upeRegisteredAddress.toRight(MalformedDataError("Malformed address data"))
        } yield UpeRegisteredAddress(
          addressLine1 = address.addressLine1,
          addressLine2 = address.addressLine2,
          addressLine3 = address.addressLine3,
          addressLine4 = address.addressLine4,
          postalCode = address.postalCode,
          countryCode = address.countryCode
        )

    }

  def getNfmAddressDetails(filingMember: FilingMember): Either[RegistrationError, NfmRegisteredAddress] =
    filingMember.isNfmRegisteredInUK match {
      case true =>
        filingMember.orgType match {
          case Some(EntityType.UkLimitedCompany) =>
            for {
              withIdData <-
                filingMember.withIdRegData.toRight(MalformedDataError("Error processing WithId data for UK Limited Company. WithId data not found."))
              incorporatedEntityData <-
                withIdData.incorporatedEntityRegistrationData.toRight(
                  MalformedDataError("Error processing IncorporatedEntityRegistrationData for UK Limited Company. Data is malformed or missing.")
                )
            } yield NfmRegisteredAddress(
              addressLine1 = incorporatedEntityData.companyProfile.unsanitisedCHROAddress.address_line_1.getOrElse(""),
              addressLine2 = incorporatedEntityData.companyProfile.unsanitisedCHROAddress.address_line_2,
              addressLine3 = incorporatedEntityData.companyProfile.unsanitisedCHROAddress.premises.getOrElse(""),
              addressLine4 = incorporatedEntityData.companyProfile.unsanitisedCHROAddress.region,
              postalCode = incorporatedEntityData.companyProfile.unsanitisedCHROAddress.postal_code,
              countryCode =
                incorporatedEntityData.companyProfile.unsanitisedCHROAddress.country.map(countryOptions.getCountryNameFromCode).getOrElse("")
            )

          case Some(EntityType.LimitedLiabilityPartnership) =>
            for {
              withIdData <- filingMember.withIdRegData.toRight(MalformedDataError("Error processing WithId data for LLP. WithId data not found."))
              partnershipEntityRegistrationData <-
                withIdData.partnershipEntityRegistrationData.toRight(
                  MalformedDataError("Error processing PartnershipEntityRegistrationData for LLP. Data is malformed or missing.")
                )
              companyProfile <- partnershipEntityRegistrationData.companyProfile.toRight(
                                  MalformedDataError("Error processing CompanyProfile for LLP. Profile data is malformed or missing.")
                                )
            } yield NfmRegisteredAddress(
              addressLine1 = companyProfile.unsanitisedCHROAddress.address_line_1.getOrElse(""),
              addressLine2 = companyProfile.unsanitisedCHROAddress.address_line_2,
              addressLine3 = companyProfile.unsanitisedCHROAddress.premises.getOrElse(""),
              addressLine4 = companyProfile.unsanitisedCHROAddress.region,
              postalCode = companyProfile.unsanitisedCHROAddress.postal_code,
              countryCode = companyProfile.unsanitisedCHROAddress.country.map(countryOptions.getCountryNameFromCode).getOrElse("")
            )
          case _ =>
            Left(InvalidOrgTypeError())
        }

      case false =>
        for {
          withoutIdData <- filingMember.withoutIdRegData.toRight(MalformedDataError("Malformed withoutIdReg data"))
          address       <- withoutIdData.registeredFmAddress.toRight(MalformedDataError("Malformed address data"))
        } yield NfmRegisteredAddress(
          addressLine1 = address.addressLine1,
          addressLine2 = address.addressLine2,
          addressLine3 = address.addressLine3,
          addressLine4 = address.addressLine4,
          postalCode = address.postalCode,
          countryCode = address.countryCode
        )

    }

}
