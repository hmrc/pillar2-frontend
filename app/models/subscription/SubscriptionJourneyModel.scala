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

package models.subscription

import cats.data.{EitherNec, NonEmptyChain}
import cats.implicits._
import models._
import models.grs.EntityType
import models.registration.GrsResponse
import pages._
import play.api.i18n.Messages
import queries.Query
import utils.DateTimeUtils._

final case class upeJourney(
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

final case class fmJourney(
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

final case class groupJourney(
  mneOrDomestic:                  MneOrDomestic,
  groupAccountingPeriodStartDate: String,
  groupAccountingPeriodEndDate:   String
)

final case class contactJourney(
  primaryContactName:      String,
  primaryContactEmail:     String,
  primaryContactByPhone:   Boolean,
  primaryContactPhone:     Option[String],
  secondaryContact:        Boolean,
  secondaryContactName:    Option[String],
  secondaryContactEmail:   Option[String],
  secondaryContactByPhone: Option[Boolean],
  secondaryContactPhone:   Option[String],
  contactAddress:          NonUKAddress
)

object upeJourney {

  def from(answers: UserAnswers): EitherNec[Query, upeJourney] =
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
        upeJourney(
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

object fmJourney {

  def from(answers: UserAnswers): EitherNec[Query, fmJourney] =
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
        fmJourney(
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

object groupJourney {

  def from(answers: UserAnswers)(implicit messages: Messages): EitherNec[Query, groupJourney] =
    (
      answers.getEither(SubMneOrDomesticPage),
      answers.getEither(SubAccountingPeriodPage).map(ap => formatDateGDS(ap.startDate)),
      answers.getEither(SubAccountingPeriodPage).map(ap => formatDateGDS(ap.endDate))
    ).parMapN {
      (
        mneOrDomestic,
        apStartDate,
        apEndDate
      ) =>
        groupJourney(
          mneOrDomestic,
          apStartDate,
          apEndDate
        )
    }
}

object contactJourney {

  def from(answers: UserAnswers): EitherNec[Query, contactJourney] =
    (
      answers.getEither(SubPrimaryContactNamePage),
      answers.getEither(SubPrimaryEmailPage),
      answers.getEither(SubPrimaryPhonePreferencePage),
      getPrimaryPhone(answers),
      answers.getEither(SubAddSecondaryContactPage),
      getSecondaryContactName(answers),
      getSecondaryContactEmail(answers),
      getSecondaryPhonePreference(answers),
      getSecondaryPhone(answers),
      answers.getEither(SubRegisteredAddressPage)
    ).parMapN {
      (
        primaryName,
        primaryEmail,
        primaryContactByPhone,
        primaryPhone,
        addSecondaryContact,
        secondaryName,
        secondaryEmail,
        secondaryContactByPhone,
        secondaryPhone,
        contactAddress
      ) =>
        contactJourney(
          primaryName,
          primaryEmail,
          primaryContactByPhone,
          primaryPhone,
          addSecondaryContact,
          secondaryName,
          secondaryEmail,
          secondaryContactByPhone,
          secondaryPhone,
          contactAddress
        )
    }

  private def getPrimaryPhone(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers.getEither(SubPrimaryPhonePreferencePage).flatMap {
      case true  => answers.getEither(SubPrimaryCapturePhonePage).map(Some(_))
      case false => Right(None)
    }

  private def getSecondaryContactName(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers.getEither(SubAddSecondaryContactPage).flatMap {
      case true  => answers.getEither(SubSecondaryContactNamePage).map(Some(_))
      case false => Right(None)
    }

  private def getSecondaryContactEmail(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers.getEither(SubAddSecondaryContactPage).flatMap {
      case true  => answers.getEither(SubSecondaryEmailPage).map(Some(_))
      case false => Right(None)
    }

  private def getSecondaryPhonePreference(answers: UserAnswers): EitherNec[Query, Option[Boolean]] =
    answers.getEither(SubAddSecondaryContactPage).flatMap {
      case true  => answers.getEither(SubSecondaryPhonePreferencePage).map(Some(_))
      case false => Right(None)
    }

  private def getSecondaryPhone(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers.getEither(SubAddSecondaryContactPage).flatMap {
      case true =>
        answers.getEither(SubSecondaryPhonePreferencePage).flatMap {
          case true  => answers.getEither(SubSecondaryCapturePhonePage).map(Some(_))
          case false => Right(None)
        }
      case false => Right(None)
    }

}
