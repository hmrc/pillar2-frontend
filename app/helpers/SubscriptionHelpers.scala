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

  // TODO - refactor this
  def fmStatus: RowStatus =
    get(NominateFilingMemberPage)
      .map { nominated =>
        if (nominated) {
          get(FmRegisteredInUKPage)
            .map { ukBased =>
              if (!ukBased) {
                (for {
                  nameReg      <- get(FmNameRegistrationPage)
                  address      <- get(FmRegisteredAddressPage)
                  contactName  <- get(FmContactNamePage)
                  contactEmail <- get(FmContactEmailPage)
                  telPref      <- get(FmPhonePreferencePage)
                } yield {
                  val telephone = get(FmCapturePhonePage).isDefined
                  if ((telPref & telephone) | (!telPref & !telephone)) {
                    RowStatus.Completed
                  } else {
                    RowStatus.InProgress
                  }
                }).getOrElse(RowStatus.InProgress)
              } else if (ukBased) {
                (for {
                  entityType <- get(FmEntityTypePage)
                  grsData    <- get(FmGRSResponsePage)
                  grsStatus  <- get(GrsFilingMemberStatusPage)
                } yield grsStatus).getOrElse(RowStatus.InProgress)
              } else {
                RowStatus.NotStarted
              }
            }
            .getOrElse(RowStatus.InProgress)
        } else if (!nominated) {
          RowStatus.Completed
        } else {
          RowStatus.NotStarted
        }
      }
      .getOrElse(RowStatus.NotStarted)

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
    if (
      groupDetailStatus == RowStatus.Completed
        & fmStatus == RowStatus.Completed
        & upeStatus == RowStatus.Completed
        & contactDetailStatus == RowStatus.Completed
    ) { true }
    else { false }

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
