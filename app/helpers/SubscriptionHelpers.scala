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

  private def upeStatusChecker: Boolean =
    (
      get(UpeRegisteredInUKPage),
      get(UpeNameRegistrationPage).isDefined,
      get(UpeRegisteredAddressPage).isDefined,
      get(UpeContactNamePage).isDefined,
      get(UpeContactEmailPage).isDefined,
      get(UpePhonePreferencePage),
      get(UpeCapturePhonePage).isDefined,
      get(UpeEntityTypePage).isDefined,
      get(UpeGRSResponsePage).isDefined,
      get(GrsUpeStatusPage).isDefined
    ) match {
      case (Some(false), true, true, true, true, Some(_), _, _, _, _) => true
      case (Some(true), _, _, _, _, _, _, true, true, true)           => true
      case _                                                          => false
    }

  def upeStatus: RowStatus =
    (get(UpeRegisteredInUKPage), get(CheckYourAnswersLogicPage)) match {
      case (Some(_), Some(true)) if !upeFinalStatusChecker => RowStatus.InProgress
      case (Some(_), _) if upeStatusChecker                => RowStatus.Completed
      case (None, _)                                       => RowStatus.NotStarted
      case _                                               => RowStatus.InProgress
    }

  def upeFinalStatusChecker: Boolean =
    (
      get(UpeRegisteredInUKPage),
      get(UpeNameRegistrationPage).isDefined,
      get(UpeRegisteredAddressPage).isDefined,
      get(UpeContactNamePage).isDefined,
      get(UpeContactEmailPage).isDefined,
      get(UpePhonePreferencePage),
      get(UpeCapturePhonePage).isDefined,
      get(UpeEntityTypePage).isDefined,
      get(UpeGRSResponsePage).isDefined,
      get(GrsUpeStatusPage).isDefined
    ) match {
      case (Some(false), true, true, true, true, Some(true), true, false, false, false)   => true
      case (Some(false), true, true, true, true, Some(false), false, false, false, false) => true
      case (Some(true), false, false, false, false, None, false, true, true, true)        => true
      case _                                                                              => false
    }

  def upeFinalStatus: RowStatus =
    get(UpeRegisteredInUKPage) match {
      case Some(_) if upeFinalStatusChecker => RowStatus.Completed
      case None                             => RowStatus.NotStarted
      case _                                => RowStatus.InProgress
    }

  def fmFinalStatusChecker: Boolean =
    (
      get(NominateFilingMemberPage),
      get(FmRegisteredInUKPage),
      get(FmNameRegistrationPage).isDefined,
      get(FmRegisteredAddressPage).isDefined,
      get(FmContactNamePage).isDefined,
      get(FmContactEmailPage).isDefined,
      get(FmPhonePreferencePage),
      get(FmCapturePhonePage).isDefined,
      get(FmEntityTypePage).isDefined,
      get(FmGRSResponsePage).isDefined,
      get(GrsFilingMemberStatusPage).isDefined
    ) match {
      case (Some(true), Some(false), true, true, true, true, Some(false), false, false, false, false) => true
      case (Some(true), Some(false), true, true, true, true, Some(true), true, false, false, false)   => true
      case (Some(true), Some(true), false, false, false, false, None, false, true, true, true)        => true
      case (Some(false), None, false, false, false, false, None, false, false, false, false)          => true
      case _                                                                                          => false
    }

  def fmFinalStatus: RowStatus =
    get(NominateFilingMemberPage) match {
      case Some(_) if fmFinalStatusChecker => RowStatus.Completed
      case None                            => RowStatus.NotStarted
      case _                               => RowStatus.InProgress
    }

  private def fmStatusChecker: Boolean =
    (
      get(NominateFilingMemberPage),
      get(FmRegisteredInUKPage),
      get(FmNameRegistrationPage).isDefined,
      get(FmRegisteredAddressPage).isDefined,
      get(FmContactNamePage).isDefined,
      get(FmContactEmailPage).isDefined,
      get(FmPhonePreferencePage),
      get(FmCapturePhonePage).isDefined,
      get(FmEntityTypePage).isDefined,
      get(FmGRSResponsePage).isDefined,
      get(GrsFilingMemberStatusPage).isDefined
    ) match {
      case (Some(true), Some(false), true, true, true, true, Some(_), _, _, _, _) => true
      case (Some(true), Some(true), _, _, _, _, _, _, true, true, true)           => true
      case (Some(false), _, _, _, _, _, _, _, _, _, _)                            => true
      case _                                                                      => false
    }

  def fmStatus: RowStatus =
    (
      get(NominateFilingMemberPage),
      get(CheckYourAnswersLogicPage)
    ) match {
      case (Some(_), Some(true)) if !fmFinalStatusChecker => RowStatus.InProgress
      case (Some(_), _) if fmStatusChecker                => RowStatus.Completed
      case (None, _)                                      => RowStatus.NotStarted
      case _                                              => RowStatus.InProgress
    }

  def groupDetailStatus: RowStatus =
    (
      get(SubMneOrDomesticPage).isDefined,
      get(SubAccountingPeriodPage).isDefined
    ) match {
      case (true, true)  => RowStatus.Completed
      case (true, false) => RowStatus.InProgress
      case _             => RowStatus.NotStarted
    }

  private def contactsStatusChecker: Boolean =
    (
      get(SubPrimaryContactNamePage).isDefined,
      get(SubPrimaryEmailPage).isDefined,
      get(SubPrimaryPhonePreferencePage),
      get(SubPrimaryCapturePhonePage).isDefined,
      get(SubAddSecondaryContactPage),
      get(SubSecondaryContactNamePage).isDefined,
      get(SubSecondaryEmailPage).isDefined,
      get(SubSecondaryPhonePreferencePage),
      get(SubSecondaryCapturePhonePage).isDefined,
      get(SubRegisteredAddressPage).isDefined
    ) match {
      case (true, true, Some(_), _, Some(_), _, _, _, _, true) => true
      case _                                                   => false
    }

  def contactsStatus: RowStatus =
    (
      get(SubPrimaryContactNamePage),
      get(CheckYourAnswersLogicPage)
    ) match {
      case (Some(_), Some(true)) if !contactsFinalStatusChecker => RowStatus.InProgress
      case (Some(_), _) if contactsStatusChecker                => RowStatus.Completed
      case (None, _)                                            => RowStatus.NotStarted
      case _                                                    => RowStatus.InProgress
    }
  def contactsFinalStatusChecker: Boolean =
    (
      get(SubPrimaryContactNamePage).isDefined,
      get(SubPrimaryEmailPage).isDefined,
      get(SubPrimaryPhonePreferencePage),
      get(SubPrimaryCapturePhonePage).isDefined,
      get(SubAddSecondaryContactPage),
      get(SubSecondaryContactNamePage).isDefined,
      get(SubSecondaryEmailPage).isDefined,
      get(SubSecondaryPhonePreferencePage),
      get(SubSecondaryCapturePhonePage).isDefined,
      get(SubRegisteredAddressPage).isDefined
    ) match {
      case (true, true, Some(true), true, Some(true), true, true, Some(true), true, true)     => true
      case (true, true, Some(true), true, Some(true), true, true, Some(false), false, true)   => true
      case (true, true, Some(true), true, Some(false), false, false, None, false, true)       => true
      case (true, true, Some(false), false, Some(true), true, true, Some(true), true, true)   => true
      case (true, true, Some(false), false, Some(true), true, true, Some(false), false, true) => true
      case (true, true, Some(false), false, Some(false), false, false, None, false, true)     => true
      case _                                                                                  => false
    }

  def contactsFinalStatus: RowStatus =
    get(SubPrimaryContactNamePage) match {
      case Some(_) if contactsFinalStatusChecker => RowStatus.Completed
      case None                                  => RowStatus.NotStarted
      case _                                     => RowStatus.InProgress
    }

  def finalStatusCheck: Boolean =
    groupDetailStatus == RowStatus.Completed &
      fmFinalStatus == RowStatus.Completed &
      upeFinalStatus == RowStatus.Completed &
      contactsFinalStatus == RowStatus.Completed

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
          if (ukBased) get(FmSafeIDPage) else get(FmNonUKSafeIDPage)
        }
      } else {
        None
      }
    )

  def getUpeSafeID: Option[String] =
    get(UpeRegisteredInUKPage).flatMap { ukBased =>
      if (ukBased) get(UpeRegInformationPage).map(regInfo => regInfo.safeId) else get(UpeNonUKSafeIDPage)
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

}
