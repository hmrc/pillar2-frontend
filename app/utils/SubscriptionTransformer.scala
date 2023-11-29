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

package utils

import models.registration.RegistrationInfo
import models.subscription.{AccountStatus, AccountingPeriod, FilingMemberDetails}
import models.{ApiError, InternalServerError, MandatoryInformationMissingError, MneOrDomestic, UKAddress, UserAnswers}
import pages._
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate
import java.util.UUID
object SubscriptionTransformer {

  def jsValueToSubscription(jsValue: JsValue): Either[ApiError, UserAnswers] = {
    val randomId    = UUID.randomUUID().toString
    val userAnswers = UserAnswers(randomId, Json.obj())

    // Extract registration info
    val registrationInfo = (jsValue \ "upeRegInformationId").validate[RegistrationInfo](Json.reads[RegistrationInfo]) match {
      case JsSuccess(value, _) =>
        value.copy(
          registrationDate = (jsValue \ "upeRegInformationId" \ "registrationDate").asOpt[String].map(LocalDate.parse),
          filingMember = Some((jsValue \ "upeRegInformationId" \ "filingMember").as[Boolean])
        )
      case JsError(errors) => return Left(MandatoryInformationMissingError("Error parsing RegistrationInfo"))
    }

    // Extract UK address
    val ukAddress = (jsValue \ "upeRegisteredAddress").validate[UKAddress](
      (
        (__ \ "addressLine1").read[String] and
          (__ \ "addressLine2").readNullable[String] and
          (__ \ "addressLine3").read[String] and
          Reads.pure(None: Option[String]) and
          (__ \ "postalCode").read[String] and
          (__ \ "countryCode").read[String]
      )(UKAddress.apply _)
    ) match {
      case JsSuccess(value, _) => value
      case JsError(errors)     => return Left(MandatoryInformationMissingError("Error parsing UKAddress"))
    }

    // Extract filing member details
    val filingMemberDetails = (jsValue \ "subFilingMemberDetails").validate[FilingMemberDetails](Json.reads[FilingMemberDetails]) match {
      case JsSuccess(value, _) => value
      case JsError(errors)     => return Left(MandatoryInformationMissingError("Error parsing FilingMemberDetails"))
    }

    // Extract accounting period
    val accountingPeriod = (jsValue \ "subAccountingPeriod").validate[AccountingPeriod](Json.reads[AccountingPeriod]) match {
      case JsSuccess(value, _) =>
        value.copy(
          duetDate = (jsValue \ "subAccountingPeriod" \ "duetDate").asOpt[String].map(LocalDate.parse)
        )
      case JsError(errors) => return Left(MandatoryInformationMissingError("Error parsing AccountingPeriod"))
    }

    // Extract account status
    val accountStatus = (jsValue \ "subAccountStatus").validate[AccountStatus](Json.reads[AccountStatus]) match {
      case JsSuccess(value, _) => value
      case JsError(errors)     => return Left(MandatoryInformationMissingError("Error parsing AccountStatus"))
    }

    val result = for {

      u1 <- userAnswers.set(subMneOrDomesticPage, (jsValue \ "subMneOrDomestic").as[MneOrDomestic]).toEither.left.map(_ => InternalServerError)
      u2 <- u1.set(upeNameRegistrationPage, (jsValue \ "upeNameRegistration").as[String]).toEither.left.map(_ => InternalServerError)
      u3 <- u2.set(subPrimaryContactNamePage, (jsValue \ "subPrimaryContactName").as[String]).toEither.left.map(_ => InternalServerError)
      u4 <- u3.set(subPrimaryEmailPage, (jsValue \ "subPrimaryEmail").as[String]).toEither.left.map(_ => InternalServerError)

      u5  <- u4.set(subSecondaryContactNamePage, (jsValue \ "subSecondaryContactName").as[String]).toEither.left.map(_ => InternalServerError)
      u6  <- u5.set(UpeRegInformationPage, registrationInfo).toEither.left.map(_ => InternalServerError)
      u7  <- u6.set(upeRegisteredAddressPage, ukAddress).toEither.left.map(_ => InternalServerError)
      u8  <- u7.set(FmSafeIDPage, (jsValue \ "FmSafeID").as[String]).toEither.left.map(_ => InternalServerError)
      u9  <- u8.set(subFillingMemberDetailsPage, filingMemberDetails).toEither.left.map(_ => InternalServerError)
      u10 <- u9.set(subAccountingPeriodPage, accountingPeriod).toEither.left.map(_ => InternalServerError)
      u11 <- u10.set(subAccountStatusPage, accountStatus).toEither.left.map(_ => InternalServerError)

      telephone    = (jsValue \ "subSecondaryCapturePhone").asOpt[String]
      telephoneStr = telephone.getOrElse("")

      u12 <- u11.set(subSecondaryEmailPage, (jsValue \ "subSecondaryEmail").as[String]).toEither.left.map(_ => InternalServerError)
      u13 <- u12.set(subSecondaryCapturePhonePage, telephoneStr).toEither.left.map(_ => InternalServerError)
    } yield u13

    result
  }
}
