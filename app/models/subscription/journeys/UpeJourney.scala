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

package models.subscription.journeys

import cats.data.{EitherNec, NonEmptyChain}
import cats.implicits.*
import models.*
import models.grs.EntityType
import models.registration.GrsResponse
import pages.*
import queries.Query
import utils.DateTimeUtils.*

final case class UpeJourney(
  upeRegisteredInUK:                 Boolean,
  upeEntityType:                     Option[EntityType],
  upeNameRegistration:               Option[String],
  upeRegisteredAddress:              Option[UKAddress],
  upeContactName:                    Option[String],
  upeContactEmail:                   Option[String],
  upePhonePreference:                Option[Boolean],
  upeCapturePhone:                   Option[String],
  entityTypeIncorporatedCompanyName: Option[String],
  entityTypeIncorporatedCompanyReg:  Option[String],
  entityTypeIncorporatedCompanyUtr:  Option[String],
  entityTypePartnershipCompanyName:  Option[String],
  entityTypePartnershipCompanyReg:   Option[String],
  entityTypePartnershipCompanyUtr:   Option[String]
)

object UpeJourney {

  def from(answers: UserAnswers): EitherNec[Query, UpeJourney] =
    (
      answers.getEither(UpeRegisteredInUKPage),
      getUpeEntityType(answers),
      getNameRegistration(answers),
      getRegisteredAddress(answers),
      getContactName(answers),
      getContactEmail(answers),
      getPhonePref(answers),
      getUpePhone(answers),
      getEtiCompanyName(answers),
      getEtiCompanyReg(answers),
      getEtiCompanyUtr(answers),
      getEtpCompanyName(answers),
      getEtpCompanyReg(answers),
      getEtpCompanyUtr(answers)
    ).parMapN {
      (
        upeRegisteredInUK,
        upeEntityType,
        upeNameRegistration,
        upeRegistrationAddress,
        upeContactName,
        upeContactEmail,
        upePhonePreference,
        upePhone,
        etiCompanyName,
        etiCompanyReg,
        etiCompanyUtr,
        etpCompanyName,
        etpCompanyReg,
        etpCompanyUtr
      ) =>
        UpeJourney(
          upeRegisteredInUK,
          upeEntityType,
          upeNameRegistration,
          upeRegistrationAddress,
          upeContactName,
          upeContactEmail,
          upePhonePreference,
          upePhone,
          etiCompanyName,
          etiCompanyReg,
          etiCompanyUtr,
          etpCompanyName,
          etpCompanyReg,
          etpCompanyUtr
        )
    }

  private def getUpeEntityType(answers: UserAnswers): EitherNec[Query, Option[EntityType]] =
    answers.getEither(UpeRegisteredInUKPage).flatMap {
      case true  => answers.getEither(UpeEntityTypePage).map(Some(_))
      case false => Right(None)
    }

  private def getNameRegistration(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers.getEither(UpeNameRegistrationPage).map(Some(_)).orElse(Right(None))

  private def getRegisteredAddress(answers: UserAnswers): EitherNec[Query, Option[UKAddress]] =
    answers.getEither(UpeRegisteredAddressPage).map(Some(_)).orElse(Right(None))

  private def getContactName(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers.getEither(UpeContactNamePage).map(Some(_)).orElse(Right(None))

  private def getContactEmail(answers: UserAnswers): Either[NonEmptyChain[Query], Option[String]] =
    answers.getEither(UpeContactEmailPage).map(Some(_)).orElse(Right(None))

  private def getPhonePref(answers: UserAnswers): EitherNec[Query, Option[Boolean]] =
    answers.getEither(UpePhonePreferencePage).map(Some(_)).orElse(Right(None))

  private def getUpePhone(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers
      .getEither(UpePhonePreferencePage)
      .map {
        case true => answers.getEither(UpeCapturePhonePage).map(Some(_))
        case _    => Right(None)
      }
      .getOrElse(Right(None))

  private def getEtiCompanyName(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers
      .getEither(UpeGRSResponsePage)
      .flatMap {
        case GrsResponse(Some(data), _) => Right(Some(data.companyProfile.companyName))
        case _                          => Right(None)
      }
      .orElse(Right(None))

  private def getEtiCompanyReg(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers
      .getEither(UpeGRSResponsePage)
      .flatMap {
        case GrsResponse(Some(data), _) => Right(Some(data.companyProfile.companyNumber))
        case _                          => Right(None)
      }
      .orElse(Right(None))

  private def getEtiCompanyUtr(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers
      .getEither(UpeGRSResponsePage)
      .flatMap {
        case GrsResponse(Some(data), _) => Right(Some(data.ctutr))
        case _                          => Right(None)
      }
      .orElse(Right(None))

  private def getEtpCompanyName(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers
      .getEither(UpeGRSResponsePage)
      .flatMap {
        case GrsResponse(_, Some(data)) => Right(data.companyProfile.map(t => t.companyName))
        case _                          => Right(None)
      }
      .orElse(Right(None))

  private def getEtpCompanyReg(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers
      .getEither(UpeGRSResponsePage)
      .flatMap {
        case GrsResponse(_, Some(data)) => Right(data.companyProfile.map(t => t.companyNumber))
        case _                          => Right(None)
      }
      .orElse(Right(None))

  private def getEtpCompanyUtr(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers
      .getEither(UpeGRSResponsePage)
      .flatMap {
        case GrsResponse(_, Some(data)) => Right(data.sautr)
        case _                          => Right(None)
      }
      .orElse(Right(None))

}
