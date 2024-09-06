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

package models.rfm

import cats.data.EitherNec
import cats.implicits.catsSyntaxTuple15Parallel
import models.grs.GrsRegistrationData
import models.rfm.CorporatePosition.{NewNfm, Upe}
import models.{NonUKAddress, UserAnswers}
import pages.{RfmAddSecondaryContactPage, RfmCapturePrimaryTelephonePage, RfmContactAddressPage, RfmContactByTelephonePage, RfmCorporatePositionPage, RfmGrsDataPage, RfmNameRegistrationPage, RfmPrimaryContactEmailPage, RfmPrimaryContactNamePage, RfmRegisteredAddressPage, RfmSecondaryCapturePhonePage, RfmSecondaryContactNamePage, RfmSecondaryEmailPage, RfmSecondaryPhonePreferencePage, RfmUkBasedPage}
import queries.Query

final case class RfmJourneyModel(
  corporateStructurePosition:  CorporatePosition,
  ukRegistered:                Option[Boolean],
  grsUkLimited:                Option[GrsRegistrationData],
  name:                        Option[String],
  registeredOfficeAddress:     Option[NonUKAddress],
  primaryContactName:          String,
  primaryContactEmail:         String,
  primaryContactByTelephone:   Boolean,
  primaryContactTelephone:     Option[String],
  secondaryContact:            Boolean,
  secondaryContactName:        Option[String],
  secondaryContactEmail:       Option[String],
  secondaryContactByTelephone: Option[Boolean],
  secondaryContactTelephone:   Option[String],
  contactAddress:              NonUKAddress
)

object RfmJourneyModel {

  def from(answers: UserAnswers): EitherNec[Query, RfmJourneyModel] =
    (
      answers.getEither(RfmCorporatePositionPage),
      getUkRegistered(answers),
      getGrsUkLimited(answers),
      getNameRegistration(answers),
      getRegisteredAddress(answers),
      answers.getEither(RfmPrimaryContactNamePage),
      answers.getEither(RfmPrimaryContactEmailPage),
      answers.getEither(RfmContactByTelephonePage),
      getPrimaryTelephone(answers),
      answers.getEither(RfmAddSecondaryContactPage),
      getSecondaryContactName(answers),
      getSecondaryContactEmail(answers),
      getSecondaryPhonePreference(answers),
      getSecondaryTelephone(answers),
      answers.getEither(RfmContactAddressPage)
    ).parMapN {
      (
        corpPosition,
        ukBased,
        grsUkLimited,
        regName,
        regAddress,
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
        RfmJourneyModel(
          corpPosition,
          ukBased,
          grsUkLimited,
          regName,
          regAddress,
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

  private def getUkRegistered(answers: UserAnswers): EitherNec[Query, Option[Boolean]] =
    answers.getEither(RfmCorporatePositionPage).flatMap {
      case NewNfm => answers.getEither(RfmUkBasedPage).map(Some(_))
      case Upe    => Right(None)
    }

  private def getGrsUkLimited(answers: UserAnswers): EitherNec[Query, Option[GrsRegistrationData]] =
    answers.getEither(RfmCorporatePositionPage).flatMap {
      case NewNfm =>
        answers.getEither(RfmUkBasedPage).flatMap {
          case true  => answers.getEither(RfmGrsDataPage).map(Some(_))
          case false => Right(None)
        }
      case Upe => Right(None)
    }

  private def getNameRegistration(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers.getEither(RfmCorporatePositionPage).flatMap {
      case NewNfm =>
        answers.getEither(RfmUkBasedPage).flatMap {
          case true  => Right(None)
          case false => answers.getEither(RfmNameRegistrationPage).map(Some(_))
        }
      case Upe => Right(None)
    }

  private def getRegisteredAddress(answers: UserAnswers): EitherNec[Query, Option[NonUKAddress]] =
    answers.getEither(RfmCorporatePositionPage).flatMap {
      case NewNfm =>
        answers.getEither(RfmUkBasedPage).flatMap {
          case true  => Right(None)
          case false => answers.getEither(RfmRegisteredAddressPage).map(Some(_))
        }
      case Upe => Right(None)
    }

  private def getPrimaryTelephone(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers.getEither(RfmContactByTelephonePage).flatMap {
      case true  => answers.getEither(RfmCapturePrimaryTelephonePage).map(Some(_))
      case false => Right(None)
    }

  private def getSecondaryContactName(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers.getEither(RfmAddSecondaryContactPage).flatMap {
      case true  => answers.getEither(RfmSecondaryContactNamePage).map(Some(_))
      case false => Right(None)
    }

  private def getSecondaryContactEmail(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers.getEither(RfmAddSecondaryContactPage).flatMap {
      case true  => answers.getEither(RfmSecondaryEmailPage).map(Some(_))
      case false => Right(None)
    }

  private def getSecondaryPhonePreference(answers: UserAnswers): EitherNec[Query, Option[Boolean]] =
    answers.getEither(RfmAddSecondaryContactPage).flatMap {
      case true  => answers.getEither(RfmSecondaryPhonePreferencePage).map(Some(_))
      case false => Right(None)
    }

  private def getSecondaryTelephone(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers.getEither(RfmAddSecondaryContactPage).flatMap {
      case true =>
        answers.getEither(RfmSecondaryPhonePreferencePage).flatMap {
          case true  => answers.getEither(RfmSecondaryCapturePhonePage).map(Some(_))
          case false => Right(None)
        }
      case false => Right(None)
    }

}
