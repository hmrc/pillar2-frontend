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

package helpers

import models.UserAnswers
import models.subscription.{ContactDetailsType, NewFilingMemberDetail}
import pages._
import utils.RowStatus

trait ReplaceFilingMemberHelpers {

  self: UserAnswers =>

  def rfmContactDetailStatus: Boolean =
    (
      get(RfmPrimaryContactNamePage).isDefined,
      get(RfmPrimaryContactEmailPage).isDefined,
      get(RfmContactByTelephonePage),
      get(RfmCapturePrimaryTelephonePage).isDefined,
      get(RfmAddSecondaryContactPage),
      get(RfmSecondaryContactNamePage).isDefined,
      get(RfmSecondaryEmailPage).isDefined,
      get(RfmSecondaryPhonePreferencePage),
      get(RfmSecondaryCapturePhonePage).isDefined,
      get(RfmContactAddressPage).isDefined
    ) match {
      case (true, true, Some(true), true, Some(true), true, true, Some(true), true, true)     => true
      case (true, true, Some(true), true, Some(true), true, true, Some(false), false, true)   => true
      case (true, true, Some(true), true, Some(false), false, false, None, false, true)       => true
      case (true, true, Some(false), false, Some(true), true, true, Some(true), true, true)   => true
      case (true, true, Some(false), false, Some(true), true, true, Some(false), false, true) => true
      case (true, true, Some(false), false, Some(false), false, false, None, false, true)     => true
      case _                                                                                  => false
    }

  def securityQuestionStatus: RowStatus = {
    val first  = get(RfmPillar2ReferencePage).isDefined
    val second = get(RfmRegistrationDatePage).isDefined
    (first, second) match {
      case (true, true)  => RowStatus.Completed
      case (true, false) => RowStatus.InProgress
      case _             => RowStatus.NotStarted
    }
  }

  def rfmNoIdQuestionStatus: RowStatus = {
    val first  = get(RfmNameRegistrationPage).isDefined
    val second = get(RfmRegisteredAddressPage).isDefined
    (first, second) match {
      case (true, true)  => RowStatus.Completed
      case (true, false) => RowStatus.InProgress
      case _             => RowStatus.NotStarted
    }
  }

  def getSecondaryContact: Option[ContactDetailsType] =
    get(RfmAddSecondaryContactPage).flatMap { nominated =>
      if (nominated) {
        for {
          secondaryName  <- get(RfmSecondaryContactNamePage)
          secondaryEmail <- get(RfmSecondaryEmailPage)
        } yield ContactDetailsType(name = secondaryName, telephone = getSecondaryTelephone, emailAddress = secondaryEmail)
      } else {
        None
      }
    }

  private def getSecondaryTelephone: Option[String] =
    get(RfmSecondaryPhonePreferencePage).flatMap { nominated =>
      if (nominated) get(RfmSecondaryCapturePhonePage) else None
    }

  def getNewFilingMemberDetail: Option[NewFilingMemberDetail] =
    for {
      referenceNumber   <- get(RfmPillar2ReferencePage)
      corporatePosition <- get(RfmCorporatePositionPage)
      primaryName       <- get(RfmPrimaryContactNamePage)
      email             <- get(RfmPrimaryContactEmailPage)
      address           <- get(RfmContactAddressPage)
    } yield NewFilingMemberDetail(
      plrReference = referenceNumber,
      corporatePosition = corporatePosition,
      contactName = primaryName,
      contactEmail = email,
      phoneNumber = getPrimaryTelephone,
      address = address,
      secondaryContactInformation = getSecondaryContact
    )

  private def getPrimaryTelephone: Option[String] =
    get(RfmContactByTelephonePage).flatMap { nominated =>
      if (nominated) get(RfmCapturePrimaryTelephonePage) else None
    }

  def rfmAnsweredSecurityQuestions: Boolean = {
    val isReferenceDefined = get(RfmPillar2ReferencePage).isDefined
    val isDateDefined      = get(RfmRegistrationDatePage).isDefined

    (isReferenceDefined, isDateDefined) match {
      case (true, true) => true
      case _            => false
    }
  }

}
