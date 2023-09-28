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

import models.UPERegisteredInUKConfirmation
import models.grs.EntityType
import models.registration.{CompanyProfile, Registration}
import models.subscription.common.UpeCorrespAddressDetails
import play.api.Logging
import utils.countryOptions.CountryOptions

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SubscriptionService @Inject() (
  countryOptions: CountryOptions
)(implicit
  ec: ExecutionContext
) extends Logging {

  def getUpeAddressDetails(registration: Registration): Either[String, UpeCorrespAddressDetails] =
    registration.isUPERegisteredInUK match {
      case UPERegisteredInUKConfirmation.Yes => extractAddressFromWithIdData(registration)
      case UPERegisteredInUKConfirmation.No  => extractAddressFromWithoutIdData(registration)
    }

  private def extractAddressFromWithIdData(registration: Registration): Either[String, UpeCorrespAddressDetails] =
    for {
      withIdData <- registration.withIdRegData.toRight("Malformed WithId data")
      address <- registration.orgType match {
                   case Some(EntityType.UkLimitedCompany) =>
                     for {
                       entityData <- withIdData.incorporatedEntityRegistrationData.toRight("Malformed data")
                     } yield extractIncorporatedEntityAddress(entityData.companyProfile)
                   case Some(EntityType.LimitedLiabilityPartnership) =>
                     for {
                       partnershipData <- withIdData.partnershipEntityRegistrationData.toRight("Malformed partnershipEntityRegistrationData data")
                       companyProfile  <- partnershipData.companyProfile.toRight("Malformed Company profile data in LLP")
                     } yield extractIncorporatedEntityAddress(companyProfile)
                   case _ => Left("Invalid Org Type")
                 }
    } yield address

  private def extractAddressFromWithoutIdData(registration: Registration): Either[String, UpeCorrespAddressDetails] =
    for {
      withoutIdData <- registration.withoutIdRegData.toRight("Malformed withoutIdReg data")
      address       <- withoutIdData.upeRegisteredAddress.toRight("Malformed address data")
    } yield UpeCorrespAddressDetails(
      addressLine1 = address.addressLine1,
      addressLine2 = address.addressLine2,
      addressLine3 = Some(address.addressLine3),
      addressLine4 = address.addressLine4,
      postCode = address.postalCode,
      countryCode = address.countryCode
    )

  private def extractIncorporatedEntityAddress(profile: CompanyProfile): UpeCorrespAddressDetails = {
    val address = profile.unsanitisedCHROAddress
    UpeCorrespAddressDetails(
      addressLine1 = address.address_line_1.getOrElse(""),
      addressLine2 = address.address_line_2,
      addressLine3 = address.premises,
      addressLine4 = address.region,
      postCode = address.postal_code,
      countryCode = address.country.map(countryOptions.getCountryNameFromCode).getOrElse("")
    )
  }
}
