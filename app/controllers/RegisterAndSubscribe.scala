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

package controllers

import connectors.UserAnswersConnectors
import models.fm.FilingMember
import models.registration.{Registration, RegistrationInfo}
import models.requests.DataRequest
import models.{EnrolmentCreationError, EnrolmentExistsError, EnrolmentInfo, MandatoryInformationMissingError}
import pages.{NominateFilingMemberPage, upeRegisteredAddressPage}
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import services.{RegisterWithoutIdService, SubscriptionService, TaxEnrolmentService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{Pillar2SessionKeys, RegistrationType}

import scala.concurrent.{ExecutionContext, Future}

trait RegisterAndSubscribe extends Logging {
  val registerWithoutIdService: RegisterWithoutIdService
  val subscriptionService:      SubscriptionService
  val userAnswersConnectors:    UserAnswersConnectors
  val taxEnrolmentService:      TaxEnrolmentService

  // noinspection ScalaStyle
  def createRegistrationAndSubscription(regInformation: Option[RegistrationInfo], fmSafeID: Option[String])(implicit
    hc:                                                 HeaderCarrier,
    ec:                                                 ExecutionContext,
    request:                                            DataRequest[AnyContent]
  ): Future[Result] =
    (regInformation, fmSafeID) match {
      case (Some(regInfo), Some(fmSafeId)) =>
        createSubscription(RegistrationType.WithId, regInformation, regInfo.safeId, Some(fmSafeId))
      case (Some(regInfo), None) =>
        request.userAnswers
          .get(NominateFilingMemberPage)
          .map { nominated =>
            if (nominated) {
              registerWithoutIdService.sendFmRegistrationWithoutId(request.userId, request.userAnswers).flatMap {
                case Right(fmSafeId) =>
                  createSubscription(RegistrationType.WithId, regInformation, regInfo.safeId, Some(fmSafeId.value))
                case Left(value) =>
                  logger.warn(s"Error $value")
                  value match {
                    case MandatoryInformationMissingError(_) => Future.successful(Redirect(routes.ErrorController.onPageLoad))
                    case _                                   => Future.successful(Redirect(routes.ErrorController.onPageLoad))
                  }
              }
            } else {
              createSubscription(RegistrationType.WithId, regInformation, regInfo.safeId)
            }
          }
          .getOrElse(Future.successful(Redirect(routes.ErrorController.onPageLoad)))

      case (None, Some(fmSafeId)) =>
        registerWithoutIdService.sendUpeRegistrationWithoutId(request.userId, request.userAnswers).flatMap {
          case Right(upeSafeId) =>
            createSubscription(RegistrationType.NoId, regInformation, upeSafeId.value, Some(fmSafeId))
          case Left(value) =>
            logger.warn(s"Error $value")
            value match {
              case MandatoryInformationMissingError(_) => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
              case _                                   => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
            }
        }

      case (None, None) =>
        registerWithoutIdService.sendUpeRegistrationWithoutId(request.userId, request.userAnswers).flatMap {
          case Right(upeSafeId) =>
            request.userAnswers
              .get(NominateFilingMemberPage)
              .map { nominated =>
                if (nominated) {
                  registerWithoutIdService.sendFmRegistrationWithoutId(request.userId, request.userAnswers).flatMap {
                    case Right(fmSafeId) =>
                      createSubscription(RegistrationType.NoId, regInformation, upeSafeId.value, Some(fmSafeId.value))
                    case Left(value) =>
                      logger.warn(s"Error $value")
                      value match {
                        case MandatoryInformationMissingError(_) => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
                        case _                                   => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
                      }
                  }
                } else {
                  createSubscription(RegistrationType.NoId, regInformation, upeSafeId.value)
                }
              }
              .getOrElse(Future.successful(Redirect(routes.ErrorController.onPageLoad)))

          case Left(value) =>
            logger.warn(s"Error $value")
            value match {
              case MandatoryInformationMissingError(_) => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
              //here
              case _ => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
            }
        }
      case _ => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
    }

  def createSubscription(regType: RegistrationType, regInformation: Option[RegistrationInfo], upeSafeId: String, fmSafeId: Option[String] = None)(
    implicit
    hc:      HeaderCarrier,
    ec:      ExecutionContext,
    request: DataRequest[AnyContent]
  ): Future[Result] =
    subscriptionService.checkAndCreateSubscription(request.userId, upeSafeId, fmSafeId).flatMap {
      case Right(successReponse) =>
        val enrolmentInfo = {
          if (regType == RegistrationType.WithId) {
            val registrationInfo = regInformation.getOrElse(throw new Exception("Registration Info Not found"))
            EnrolmentInfo(ctUtr = Some(registrationInfo.utr), crn = Some(registrationInfo.crn), plrId = successReponse.plrReference)
          } else {
            val address     = request.userAnswers.get(upeRegisteredAddressPage).getOrElse(throw new Exception("Registration Info Not found"))
            val countryCode = address.countryCode
            val postCode    = address.postalCode
            EnrolmentInfo(countryCode = Some(countryCode), nonUkPostcode = Some(postCode), plrId = successReponse.plrReference)
          }
        }
        taxEnrolmentService.checkAndCreateEnrolment(enrolmentInfo).flatMap {
          case Right(_) =>
            logger.info(s"Redirecting to RegistrationConfirmationController for ${successReponse.plrReference}")
            Future.successful(
              Redirect(routes.RegistrationConfirmationController.onPageLoad).withSession(
                request.session
                  + (Pillar2SessionKeys.plrId -> successReponse.plrReference)
              )
            )
          case Left(EnrolmentCreationError) =>
            logger.warn(s"Encountered EnrolmentCreationError. Redirecting to ErrorController.")
            Future.successful(Redirect(routes.ErrorController.onPageLoad))
          case Left(EnrolmentExistsError) =>
            logger.warn(s"Encountered EnrolmentExistsError. Redirecting to ErrorController.")
            Future.successful(Redirect(routes.ErrorController.onPageLoad))
        }

      case Left(value) =>
        Future.successful(Redirect(routes.ErrorController.onPageLoad))
    }
}
