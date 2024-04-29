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

import models.{EnrolmentInfo, UserAnswers}
import pages._
import utils.RowStatus

trait SubscriptionHelpers {

  self: UserAnswers =>

  def upeStatus: RowStatus =
    get(UpeRegisteredInUKPage)
      .map { ukBased =>
        if (!ukBased) {
          (for {
            nameReg      <- get(UpeNameRegistrationPage)
            address      <- get(UpeRegisteredAddressPage)
            contactName  <- get(UpeContactNamePage)
            contactEmail <- get(UpeContactEmailPage)
            telPref      <- get(UpePhonePreferencePage)
          } yield {
            val telephone = get(UpeCapturePhonePage).isDefined
            if ((telPref & telephone) | (!telPref & !telephone)) {
              RowStatus.Completed
            } else {
              RowStatus.InProgress
            }
          }).getOrElse(RowStatus.InProgress)
        } else {
          (for {
            entityType <- get(UpeEntityTypePage)
            grsData    <- get(UpeGRSResponsePage)
            grsStatus  <- get(GrsUpeStatusPage)
          } yield grsStatus).getOrElse(RowStatus.InProgress)
        }
      }
      .getOrElse(RowStatus.NotStarted)

  private def fmDetailStatusChecker: Boolean = {
    val nominateFM        = get(NominateFilingMemberPage)
    val registeredInUK    = get(FmRegisteredInUKPage)
    val registeredName    = get(FmNameRegistrationPage).isDefined
    val registeredAddress = get(FmRegisteredAddressPage).isDefined
    val contactName       = get(FmContactNamePage).isDefined
    val contactEmail      = get(FmContactEmailPage).isDefined
    val phonePref         = get(FmPhonePreferencePage)
    val phoneNumber       = get(FmCapturePhonePage).isDefined
    val entityType        = get(FmEntityTypePage).isDefined
    val grsResponse       = get(FmGRSResponsePage).isDefined
    val grsStatus         = get(GrsFilingMemberStatusPage).isDefined
    (
      nominateFM,
      registeredInUK,
      registeredName,
      registeredAddress,
      contactName,
      contactEmail,
      phonePref,
      phoneNumber,
      entityType,
      grsResponse,
      grsStatus
    ) match {
      case (Some(true), Some(false), true, true, true, true, Some(false), false, false, false, false) => true
      case (Some(true), Some(false), true, true, true, true, Some(true), true, false, false, false)   => true
      case (Some(true), Some(true), false, false, false, false, None, false, true, true, true)        => true
      case (Some(false), None, false, false, false, false, None, false, false, false, false)          => true
      case _                                                                                          => false
    }
  }

  def fmStatus: RowStatus =
    get(NominateFilingMemberPage) match {
      case Some(_) if fmDetailStatusChecker => RowStatus.Completed
      case None                             => RowStatus.NotStarted
      case _                                => RowStatus.InProgress
    }

  def groupDetailStatus: RowStatus = {
    val first  = get(SubMneOrDomesticPage).isDefined
    val second = get(SubAccountingPeriodPage).isDefined
    (first, second) match {
      case (true, true)  => RowStatus.Completed
      case (true, false) => RowStatus.InProgress
      case _             => RowStatus.NotStarted
    }
  }

  def contactDetailStatusChecker: Boolean = {
    val pName            = get(SubPrimaryContactNamePage).isDefined
    val PEmail           = get(SubPrimaryEmailPage).isDefined
    val pPhonePref       = get(SubPrimaryPhonePreferencePage)
    val pPhone           = get(SubPrimaryCapturePhonePage).isDefined
    val addSecondaryPref = get(SubAddSecondaryContactPage)
    val sName            = get(SubSecondaryContactNamePage).isDefined
    val sEmail           = get(SubSecondaryEmailPage).isDefined
    val sPhonePref       = get(SubSecondaryPhonePreferencePage)
    val sPhone           = get(SubSecondaryCapturePhonePage).isDefined
    val address          = get(SubRegisteredAddressPage).isDefined
    (pName, PEmail, pPhonePref, pPhone, addSecondaryPref, sName, sEmail, sPhonePref, sPhone, address) match {
      case (true, true, Some(true), true, Some(true), true, true, Some(true), true, true)     => true
      case (true, true, Some(true), true, Some(true), true, true, Some(false), false, true)   => true
      case (true, true, Some(true), true, Some(false), false, false, None, false, true)       => true
      case (true, true, Some(false), false, Some(true), true, true, Some(true), true, true)   => true
      case (true, true, Some(false), false, Some(true), true, true, Some(false), false, true) => true
      case (true, true, Some(false), false, Some(false), false, false, None, false, true)     => true
      case _                                                                                  => false
    }
  }

  def contactDetailStatus: RowStatus =
    get(SubPrimaryContactNamePage) match {
      case Some(_) if contactDetailStatusChecker => RowStatus.Completed
      case None                                  => RowStatus.NotStarted
      case _                                     => RowStatus.InProgress
    }

  def finalStatusCheck: Boolean =
    groupDetailStatus == RowStatus.Completed &
      fmStatus == RowStatus.Completed &
      upeStatus == RowStatus.Completed &
      contactDetailStatus == RowStatus.Completed

  def finalCYAStatus(upe: RowStatus, nfm: RowStatus, groupDetail: RowStatus, contactDetail: RowStatus): RowStatus =
    if (
      upe == RowStatus.Completed &
        nfm == RowStatus.Completed &
        groupDetail == RowStatus.Completed &
        contactDetail == RowStatus.Completed
    ) {
      RowStatus.NotStarted
    } else { RowStatus.CannotStartYet }

  def getFmSafeID: Option[String] =
    get(NominateFilingMemberPage).flatMap(nominated =>
      if (nominated) {
        get(FmRegisteredInUKPage).flatMap { ukBased =>
          if (ukBased) {
            get(FmSafeIDPage)
          } else {
            None
          }
        }
      } else {
        None
      }
    )

  def getUpeSafeID: Option[String] =
    get(UpeRegisteredInUKPage).flatMap { ukBased =>
      if (ukBased) {
        get(UpeRegInformationPage).map(regInfo => regInfo.safeId)
      } else {
        None
      }
    }

  def createEnrolmentInfo(plpID: String): EnrolmentInfo =
    get(UpeRegisteredInUKPage)
      .flatMap { ukBased =>
        if (ukBased) {
          get(UpeRegInformationPage)
            .map { regInfo =>
              EnrolmentInfo(ctUtr = Some(regInfo.utr), crn = Some(regInfo.crn), plrId = plpID)
            }
        } else {
          get(UpeRegisteredAddressPage).map(address =>
            EnrolmentInfo(nonUkPostcode = Some(address.postalCode), countryCode = Some(address.countryCode), plrId = plpID)
          )
        }
      }
      .getOrElse(EnrolmentInfo(plrId = plpID))

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
}
