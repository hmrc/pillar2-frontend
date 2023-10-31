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

import models.fm.{FilingMember, FilingMemberNonUKData}
import models.registration.{Registration, WithoutIdRegData}
import models.requests.DataRequest
import pages._
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
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
            grsStatus  <- request.userAnswers.get(GrsUpeStatusPage)
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
              } else if (ukBased) {
                (for {
                  entityType <- request.userAnswers.get(fmEntityTypePage)
                  grsData    <- request.userAnswers.get(fmGRSResponsePage)
                  grsStatus  <- request.userAnswers.get(GrsFilingMemberStatusPage)
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

  def finalCYAStatus(upe: RowStatus, nfm: RowStatus, groupDetail: RowStatus, contactDetail: RowStatus) =
    if (
      upe == RowStatus.Completed &
        nfm == RowStatus.Completed &
        groupDetail == RowStatus.Completed &
        contactDetail == RowStatus.Completed
    ) {
      RowStatus.NotStarted.toString
    } else { "Cannot start yet" }

  // noinspection ScalaStyle
  def createFilingMember(request: DataRequest[AnyContent]): Either[Result, FilingMember] =
    request.userAnswers
      .get(NominateFilingMemberPage)
      .map { nominated =>
        if (nominated) {
          request.userAnswers
            .get(fmRegisteredInUKPage)
            .map { ukBased =>
              if (ukBased) {
                (for {
                  entityType <- request.userAnswers.get(fmEntityTypePage)
                  grsData    <- request.userAnswers.get(fmGRSResponsePage)
                } yield Right(
                  FilingMember(
                    isNfmRegisteredInUK = ukBased,
                    orgType = Some(entityType),
                    withIdRegData = Some(grsData),
                    withoutIdRegData = None,
                    safeId = request.userAnswers.get(FmSafeIDPage)
                  )
                )).getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
              } else if (!ukBased) {
                (for {
                  nameReg      <- request.userAnswers.get(fmNameRegistrationPage)
                  address      <- request.userAnswers.get(fmRegisteredAddressPage)
                  contactName  <- request.userAnswers.get(fmContactNamePage)
                  contactEmail <- request.userAnswers.get(fmContactEmailPage)
                  phonePref    <- request.userAnswers.get(fmPhonePreferencePage)
                } yield
                  if (phonePref) {
                    request.userAnswers
                      .get(fmCapturePhonePage)
                      .map { tel =>
                        Right(
                          FilingMember(
                            isNfmRegisteredInUK = false,
                            orgType = None,
                            withIdRegData = None,
                            withoutIdRegData = Some(
                              FilingMemberNonUKData(
                                registeredFmName = nameReg,
                                registeredFmAddress = address,
                                contactName = contactName,
                                emailAddress = contactEmail,
                                phonePreference = phonePref,
                                telephone = Some(tel)
                              )
                            )
                          )
                        )
                      // here we could send them to a page where they get told you haven't answered every question try again
                      }
                      .getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
                  } else {
                    Right(
                      FilingMember(
                        isNfmRegisteredInUK = false,
                        orgType = None,
                        withIdRegData = None,
                        withoutIdRegData = Some(
                          FilingMemberNonUKData(
                            registeredFmName = nameReg,
                            registeredFmAddress = address,
                            contactName = contactName,
                            emailAddress = contactEmail,
                            phonePreference = phonePref,
                            telephone = None
                          )
                        )
                      )
                    )
                  }).getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
              } else {
                Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
              }
            }
            .getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
        } else if (!nominated) {
          //what will you do if a filing member is not nominated?
          Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        } else {
          Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
      }
      .getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))

  // noinspection ScalaStyle
  def createUltimateParent(request: DataRequest[AnyContent]): Either[Result, Registration] =
    request.userAnswers
      .get(upeRegisteredInUKPage)
      .map { ukBased =>
        if (ukBased) {
          (for {
            entityType <- request.userAnswers.get(upeEntityTypePage)
            grsData    <- request.userAnswers.get(upeGRSResponsePage)
            regInfo    <- request.userAnswers.get(GrsUpeRegInfoPage)
          } yield Right(
            Registration(
              isUPERegisteredInUK = ukBased,
              orgType = Some(entityType),
              withIdRegData = Some(grsData),
              withoutIdRegData = None,
              registrationInfo = Some(regInfo)
            )
          )).getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
        } else if (!ukBased) {
          (for {
            nameReg      <- request.userAnswers.get(upeNameRegistrationPage)
            address      <- request.userAnswers.get(upeRegisteredAddressPage)
            contactName  <- request.userAnswers.get(upeContactNamePage)
            contactEmail <- request.userAnswers.get(upeContactEmailPage)
            phonePref    <- request.userAnswers.get(upePhonePreferencePage)
          } yield
            if (phonePref) {
              request.userAnswers
                .get(upeCapturePhonePage)
                .map { tel =>
                  Right(
                    Registration(
                      isUPERegisteredInUK = false,
                      orgType = None,
                      withIdRegData = None,
                      withoutIdRegData = Some(
                        WithoutIdRegData(
                          upeNameRegistration = nameReg,
                          upeRegisteredAddress = address,
                          upeContactName = contactName,
                          emailAddress = contactEmail,
                          contactUpeByTelephone = phonePref,
                          telephoneNumber = Some(tel)
                        )
                      )
                    )
                  )
                // here we could send them to a page where they get told you haven't answered every question try again
                }
                .getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
            } else {
              Right(
                Registration(
                  isUPERegisteredInUK = false,
                  orgType = None,
                  withIdRegData = None,
                  withoutIdRegData = Some(
                    WithoutIdRegData(
                      upeNameRegistration = nameReg,
                      upeRegisteredAddress = address,
                      upeContactName = contactName,
                      emailAddress = contactEmail,
                      contactUpeByTelephone = phonePref,
                      telephoneNumber = None
                    )
                  )
                )
              )
            }).getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
        } else {
          Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }

      }
      .getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
}
