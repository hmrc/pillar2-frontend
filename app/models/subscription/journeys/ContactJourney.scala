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

package models.subscription.journeys

import cats.data.EitherNec
import cats.implicits.*
import models.*
import pages.*
import queries.Query

final case class ContactJourney(
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

object ContactJourney {

  def from(answers: UserAnswers): EitherNec[Query, ContactJourney] =
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
        ContactJourney(
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
