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

final case class FmJourney(
  fmYesNo:                             Boolean,
  fmRegisteredInUK:                    Option[Boolean],
  fmEntityType:                        Option[EntityType],
  fmNameRegistration:                  Option[String],
  fmRegisteredAddress:                 Option[NonUKAddress],
  fmContactName:                       Option[String],
  fmEmailAddress:                      Option[String],
  fmPhonePreference:                   Option[Boolean],
  fmContactPhone:                      Option[String],
  fmEntityTypeIncorporatedCompanyName: Option[String],
  fmEntityTypeIncorporatedCompanyReg:  Option[String],
  fmEntityTypeIncorporatedCompanyUtr:  Option[String],
  fmEntityTypePartnershipCompanyName:  Option[String],
  fmEntityTypePartnershipCompanyReg:   Option[String],
  fmEntityTypePartnershipCompanyUtr:   Option[String]
)

object FmJourney {

  def from(answers: UserAnswers): EitherNec[Query, FmJourney] =
    (
      answers.getEither(NominateFilingMemberPage),
      getFmRegisteredInUK(answers),
      getFmEntityType(answers),
      getFmNameRegistration(answers),
      getFmRegisteredAddress(answers),
      getFmContactName(answers),
      getFmContactEmail(answers),
      getFmPhonePref(answers),
      getFmPhone(answers),
      getFmEtiCompanyName(answers),
      getFmEtiCompanyReg(answers),
      getFmEtiCompanyUtr(answers),
      getFmEtpCompanyName(answers),
      getFmEtpCompanyReg(answers),
      getFmEtpCompanyUtr(answers)
    ).parMapN {
      (
        nominatedFilingMember,
        fmRegisteredInUK,
        fmEntityType,
        fmNameRegistration,
        fmRegistrationAddress,
        fmContactName,
        fmContactEmail,
        fmPhonePreference,
        fmPhone,
        fmEtiCompanyName,
        fmEtiCompanyReg,
        fmEtiCompanyUtr,
        fmEtpCompanyName,
        fmEtpCompanyReg,
        fmEtpCompanyUtr
      ) =>
        FmJourney(
          nominatedFilingMember,
          fmRegisteredInUK,
          fmEntityType,
          fmNameRegistration,
          fmRegistrationAddress,
          fmContactName,
          fmContactEmail,
          fmPhonePreference,
          fmPhone,
          fmEtiCompanyName,
          fmEtiCompanyReg,
          fmEtiCompanyUtr,
          fmEtpCompanyName,
          fmEtpCompanyReg,
          fmEtpCompanyUtr
        )
    }

  private def getFmRegisteredInUK(answers: UserAnswers): EitherNec[Query, Option[Boolean]] =
    answers.getEither(FmRegisteredInUKPage).map(Some(_)).orElse(Right(None))

  private def getFmEntityType(answers: UserAnswers): EitherNec[Query, Option[EntityType]] =
    answers
      .getEither(FmRegisteredInUKPage)
      .map {
        case true  => answers.getEither(FmEntityTypePage).map(Some(_))
        case false => Right(None)
      }
      .getOrElse(Right(None))

  private def getFmNameRegistration(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers.getEither(FmNameRegistrationPage).map(Some(_)).orElse(Right(None))

  private def getFmRegisteredAddress(answers: UserAnswers): EitherNec[Query, Option[NonUKAddress]] =
    answers.getEither(FmRegisteredAddressPage).map(Some(_)).orElse(Right(None))

  private def getFmContactName(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers.getEither(FmContactNamePage).map(Some(_)).orElse(Right(None))

  private def getFmContactEmail(answers: UserAnswers): Either[NonEmptyChain[Query], Option[String]] =
    answers.getEither(FmContactEmailPage).map(Some(_)).orElse(Right(None))

  private def getFmPhonePref(answers: UserAnswers): EitherNec[Query, Option[Boolean]] =
    answers.getEither(FmPhonePreferencePage).map(Some(_)).orElse(Right(None))

  private def getFmPhone(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers
      .getEither(FmPhonePreferencePage)
      .map {
        case true => answers.getEither(FmCapturePhonePage).map(Some(_))
        case _    => Right(None)
      }
      .getOrElse(Right(None))

  private def getFmEtiCompanyName(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers
      .getEither(FmGRSResponsePage)
      .flatMap {
        case GrsResponse(Some(data), _) => Right(Some(data.companyProfile.companyName))
        case _                          => Right(None)
      }
      .orElse(Right(None))

  private def getFmEtiCompanyReg(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers
      .getEither(FmGRSResponsePage)
      .flatMap {
        case GrsResponse(Some(data), _) => Right(Some(data.companyProfile.companyNumber))
        case _                          => Right(None)
      }
      .orElse(Right(None))

  private def getFmEtiCompanyUtr(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers
      .getEither(FmGRSResponsePage)
      .flatMap {
        case GrsResponse(Some(data), _) => Right(Some(data.ctutr))
        case _                          => Right(None)
      }
      .orElse(Right(None))

  private def getFmEtpCompanyName(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers
      .getEither(FmGRSResponsePage)
      .flatMap {
        case GrsResponse(_, Some(data)) => Right(data.companyProfile.map(t => t.companyName))
        case _                          => Right(None)
      }
      .orElse(Right(None))

  private def getFmEtpCompanyReg(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers
      .getEither(FmGRSResponsePage)
      .flatMap {
        case GrsResponse(_, Some(data)) => Right(data.companyProfile.map(t => t.companyNumber))
        case _                          => Right(None)
      }
      .orElse(Right(None))

  private def getFmEtpCompanyUtr(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers
      .getEither(FmGRSResponsePage)
      .flatMap {
        case GrsResponse(_, Some(data)) => Right(data.sautr)
        case _                          => Right(None)
      }
      .orElse(Right(None))

}
