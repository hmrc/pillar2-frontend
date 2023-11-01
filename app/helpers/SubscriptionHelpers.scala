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

package helpers

import models.UserAnswers
import models.registration.RegistrationInfo
import pages._
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import utils.RowStatus

trait SubscriptionHelpers {

  self: UserAnswers =>

  def upeStatus: RowStatus =
    get(upeRegisteredInUKPage)
      .map { ukBased =>
        if (!ukBased) {
          (for {
            nameReg      <- get(upeNameRegistrationPage)
            address      <- get(upeRegisteredAddressPage)
            contactName  <- get(upeContactNamePage)
            contactEmail <- get(upeContactEmailPage)
            telPref      <- get(upePhonePreferencePage)
          } yield {
            val telephone = get(upeCapturePhonePage).isDefined
            if ((telPref & telephone) | !telPref) {
              RowStatus.Completed
            } else {
              RowStatus.InProgress
            }
          }).getOrElse(RowStatus.InProgress)
        } else {
          (for {
            entityType <- get(upeEntityTypePage)
            grsData    <- get(upeGRSResponsePage)
            grsStatus  <- get(GrsUpeStatusPage)
          } yield grsStatus).getOrElse(RowStatus.InProgress)
        }
      }
      .getOrElse(RowStatus.NotStarted)

  def fmStatus: RowStatus =
    get(NominateFilingMemberPage)
      .map { nominated =>
        if (nominated) {
          get(fmRegisteredInUKPage)
            .map { ukBased =>
              if (!ukBased) {
                (for {
                  nameReg      <- get(fmNameRegistrationPage)
                  address      <- get(fmRegisteredAddressPage)
                  contactName  <- get(fmContactNamePage)
                  contactEmail <- get(fmContactEmailPage)
                  telPref      <- get(fmPhonePreferencePage)
                } yield {
                  val telephone = get(fmCapturePhonePage).isDefined
                  if ((telPref & telephone) | !telPref) {
                    RowStatus.Completed
                  } else {
                    RowStatus.InProgress
                  }
                }).getOrElse(RowStatus.InProgress)
              } else if (ukBased) {
                (for {
                  entityType <- get(fmEntityTypePage)
                  grsData    <- get(fmGRSResponsePage)
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
    val first  = get(subMneOrDomesticPage).isDefined
    val second = get(subAccountingPeriodPage).isDefined
    (first, second) match {
      case (true, true)  => RowStatus.Completed
      case (true, false) => RowStatus.InProgress
      case _             => RowStatus.NotStarted
    }
  }

  def contactDetailStatus: RowStatus = {
    val first = get(subPrimaryContactNamePage).isDefined
    val last  = get(subRegisteredAddressPage).isDefined
    (first, last) match {
      case (true, true)  => RowStatus.Completed
      case (true, false) => RowStatus.InProgress
      case _             => RowStatus.NotStarted
    }
  }

  def finalCYAStatus(upe: RowStatus, nfm: RowStatus, groupDetail: RowStatus, contactDetail: RowStatus) =
    if (
      upe == RowStatus.Completed &
        nfm == RowStatus.Completed &
        groupDetail == RowStatus.Completed &
        contactDetail == RowStatus.Completed
    ) {
      RowStatus.NotStarted.toString
    } else { "Cannot start yet" }

  def getFmSafeID: Either[Result, Option[String]] =
    get(NominateFilingMemberPage)
      .map { nominated =>
        if (nominated) {
          get(fmRegisteredInUKPage)
            .map { ukBased =>
              if (ukBased) {
                (for {
                  safeID <- get(FmSafeIDPage)
                } yield Right(Some(safeID))).getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
              } else if (!ukBased) {
                Right(None)
              } else {
                Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
              }
            }
            .getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
        } else if (!nominated) {
          Right(None)
        } else {
          Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
      }
      .getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))

  def getUpRegData: Either[Result, Option[RegistrationInfo]] =
    get(upeRegisteredInUKPage)
      .map { ukBased =>
        if (ukBased) {
          (for {
            regInfo <- get(UpeRegInformationPage)
          } yield Right(Some(regInfo))).getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
        } else if (!ukBased) {
          Right(None)
        } else {
          Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
      }
      .getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
}
