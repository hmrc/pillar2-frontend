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

import models.registration.upeContactInformation
import models.requests.DataRequest
import pages.{GrsFilingMemberStatusPage, GrsUpStatusPage, NominateFilingMemberPage, fmCapturePhonePage, fmContactEmailPage, fmContactNamePage, fmEntityTypePage, fmGRSResponsePage, fmNameRegistrationPage, fmPhonePreferencePage, fmRegisteredAddressPage, fmRegisteredInUKPage, subAccountingPeriodPage, subMneOrDomesticPage, subPrimaryContactNamePage, subRegisteredAddressPage, upeCapturePhonePage, upeContactEmailPage, upeContactNamePage, upeEntityTypePage, upeGRSResponsePage, upeNameRegistrationPage, upePhonePreferencePage, upeRegisteredAddressPage, upeRegisteredInUKPage}
import play.api.mvc.AnyContent
import utils.RowStatus

trait SubscriptionHelpers {

  def getUpeStatus(request: DataRequest[AnyContent]): RowStatus =
    request.userAnswers
      .get(upeRegisteredInUKPage)
      .map { ukBased =>
        if (!ukBased) {
          (for {
            nameReg      <- request.userAnswers.get(upeNameRegistrationPage)
            address      <- request.userAnswers.get(upeRegisteredAddressPage)
            contactName  <- request.userAnswers.get(upeContactNamePage)
            contactEmail <- request.userAnswers.get(upeContactEmailPage)
            telPref      <- request.userAnswers.get(upePhonePreferencePage)
          } yield {
            val telephone = request.userAnswers.get(upeCapturePhonePage).isDefined
            if ((telPref & telephone) | !telPref) {
              RowStatus.Completed
            } else {
              RowStatus.InProgress
            }
          }).getOrElse(RowStatus.InProgress)
        } else {
          (for {
            entityType <- request.userAnswers.get(upeEntityTypePage)
            grsData    <- request.userAnswers.get(upeGRSResponsePage)
            grsStatus  <- request.userAnswers.get(GrsUpStatusPage)
          } yield grsStatus).getOrElse(RowStatus.InProgress)
        }
      }
      .getOrElse(RowStatus.NotStarted)

  def getFmStatus(request: DataRequest[AnyContent]): RowStatus =
    request.userAnswers
      .get(NominateFilingMemberPage)
      .map { nominated =>
        if (nominated) {
          request.userAnswers
            .get(fmRegisteredInUKPage)
            .map { ukBased =>
              if (!ukBased) {
                (for {
                  nameReg      <- request.userAnswers.get(fmNameRegistrationPage)
                  address      <- request.userAnswers.get(fmRegisteredAddressPage)
                  contactName  <- request.userAnswers.get(fmContactNamePage)
                  contactEmail <- request.userAnswers.get(fmContactEmailPage)
                  telPref      <- request.userAnswers.get(fmPhonePreferencePage)
                } yield {
                  val telephone = request.userAnswers.get(fmCapturePhonePage).isDefined
                  if ((telPref & telephone) | !telPref) {
                    RowStatus.Completed
                  } else {
                    RowStatus.InProgress
                  }
                }).getOrElse(RowStatus.InProgress)
              } else {
                (for {
                  entityType <- request.userAnswers.get(fmEntityTypePage)
                  grsData    <- request.userAnswers.get(fmGRSResponsePage)
                  grsStatus  <- request.userAnswers.get(GrsFilingMemberStatusPage)
                } yield grsStatus).getOrElse(RowStatus.InProgress)
              }
            }
            .getOrElse(RowStatus.Completed)
        } else if (!nominated) {
          RowStatus.Completed
        } else {
          RowStatus.NotStarted
        }
      }
      .getOrElse(RowStatus.NotStarted)

  def getGroupDetailStatus(request: DataRequest[AnyContent]): RowStatus = {
    val first  = request.userAnswers.get(subMneOrDomesticPage).isDefined
    val second = request.userAnswers.get(subAccountingPeriodPage).isDefined
    (first, second) match {
      case (true, true)  => RowStatus.Completed
      case (true, false) => RowStatus.InProgress
      case _             => RowStatus.NotStarted
    }
  }

  def getContactDetailStatus(request: DataRequest[AnyContent]): RowStatus = {
    val first = request.userAnswers.get(subPrimaryContactNamePage).isDefined
    val last  = request.userAnswers.get(subRegisteredAddressPage).isDefined
    (first, last) match {
      case (true, true)  => RowStatus.Completed
      case (true, false) => RowStatus.InProgress
      case _             => RowStatus.NotStarted
    }
  }
}
