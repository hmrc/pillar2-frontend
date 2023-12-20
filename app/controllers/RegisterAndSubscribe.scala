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
import models.requests.DataRequest
import models.{EnrolmentCreationError, EnrolmentExistsError, EnrolmentInfo, MandatoryInformationMissingError}
import pages.{NominateFilingMemberPage, UpeRegInformationPage, upeRegisteredAddressPage, upeRegisteredInUKPage}
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import services.{RegisterWithoutIdService, SubscriptionService, TaxEnrolmentService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Pillar2SessionKeys

import scala.concurrent.{ExecutionContext, Future}

trait RegisterAndSubscribe extends Logging {
  val registerWithoutIdService: RegisterWithoutIdService
  val subscriptionService:      SubscriptionService
  val userAnswersConnectors:    UserAnswersConnectors
  val taxEnrolmentService:      TaxEnrolmentService

  // noinspection ScalaStyle
  def createRegistrationAndSubscription(upeSafeId: Option[String], fmSafeID: Option[String])(implicit
    hc:                                            HeaderCarrier,
    ec:                                            ExecutionContext,
    request:                                       DataRequest[AnyContent]
  ): Future[Result] =
    (upeSafeId, fmSafeID) match {
      case (Some(regInfo), Some(fmSafeId)) =>
        createSubscription(regInfo, Some(fmSafeId))
      case (Some(regInfo), None) =>
        request.userAnswers
          .get(NominateFilingMemberPage)
          .map { nominated =>
            if (nominated) {
              registerWithoutIdService.sendFmRegistrationWithoutId(request.userId, request.userAnswers).flatMap {
                case Right(fmSafeId) =>
                  createSubscription(regInfo, Some(fmSafeId.value))
                case Left(value) =>
                  logger.warn(s"Error $value")
                  value match {
                    case MandatoryInformationMissingError(_) =>
                      Future.successful(Redirect(controllers.subscription.routes.SubscriptionFailedController.onPageLoad))
                    case _ => Future.successful(Redirect(controllers.subscription.routes.SubscriptionFailedController.onPageLoad))
                  }
              }
            } else {
              createSubscription(regInfo)
            }
          }
          .getOrElse(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))

      case (None, Some(fmSafeId)) =>
        registerWithoutIdService.sendUpeRegistrationWithoutId(request.userId, request.userAnswers).flatMap {
          case Right(upeSafeId) =>
            createSubscription(upeSafeId.value, Some(fmSafeId))
          case Left(value) =>
            logger.warn(s"Error $value")
            value match {
              case MandatoryInformationMissingError(_) =>
                Future.successful(Redirect(controllers.subscription.routes.SubscriptionFailedController.onPageLoad))
              case _ => Future.successful(Redirect(controllers.subscription.routes.SubscriptionFailedController.onPageLoad))
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
                      createSubscription(upeSafeId.value, Some(fmSafeId.value))
                    case Left(value) =>
                      logger.warn(s"Error $value")
                      value match {
                        case MandatoryInformationMissingError(_) =>
                          Future.successful(Redirect(controllers.subscription.routes.SubscriptionFailedController.onPageLoad))
                        case _ => Future.successful(Redirect(controllers.subscription.routes.SubscriptionFailedController.onPageLoad))
                      }
                  }
                } else {
                  createSubscription(upeSafeId.value)
                }
              }
              .getOrElse(Future.successful(Redirect(routes.UnderConstructionController.onPageLoad)))

          case Left(value) =>
            logger.warn(s"Error $value")
            value match {
              case MandatoryInformationMissingError(_) =>
                Future.successful(Redirect(controllers.subscription.routes.SubscriptionFailedController.onPageLoad))
              case _ => Future.successful(Redirect(controllers.subscription.routes.SubscriptionFailedController.onPageLoad))
            }
        }
      case _ => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
    }

  def createSubscription(upeSafeId: String, fmSafeId: Option[String] = None)(implicit
    hc:                             HeaderCarrier,
    ec:                             ExecutionContext,
    request:                        DataRequest[AnyContent]
  ): Future[Result] =
    subscriptionService.checkAndCreateSubscription(request.userId, upeSafeId, fmSafeId).flatMap {
      case Right(successResponse) =>
        val enrolmentInfo = request.userAnswers
          .get(upeRegisteredInUKPage)
          .flatMap { ukBased =>
            if (ukBased) {
              for {
                regInfo <- request.userAnswers.get(UpeRegInformationPage)
              } yield EnrolmentInfo(ctUtr = Some(regInfo.utr), crn = Some(regInfo.crn), plrId = successResponse.plrReference)
            } else {
              for {
                address <- request.userAnswers.get(upeRegisteredAddressPage)
              } yield EnrolmentInfo(
                countryCode = Some(address.countryCode),
                nonUkPostcode = Some(address.postalCode),
                plrId = successResponse.plrReference
              )
            }
          }
          .getOrElse(EnrolmentInfo(plrId = successResponse.plrReference))
        taxEnrolmentService.checkAndCreateEnrolment(enrolmentInfo).flatMap {
          case Right(_) =>
            userAnswersConnectors.remove(request.userId).map { _ =>
              logger.info(s"Redirecting to RegistrationConfirmationController for ${successResponse.plrReference}")
              Redirect(routes.RegistrationConfirmationController.onPageLoad).withSession(
                request.session
                  + (Pillar2SessionKeys.plrId -> successResponse.plrReference)
              )
            }
          case Left(EnrolmentCreationError) =>
            logger.warn(s"Encountered EnrolmentCreationError. Redirecting to ErrorController.")
            Future.successful(Redirect(controllers.subscription.routes.SubscriptionFailedController.onPageLoad))
          case Left(EnrolmentExistsError) =>
            logger.warn(s"Encountered EnrolmentExistsError. Redirecting to ErrorController.")
            Future.successful(Redirect(controllers.subscription.routes.SubscriptionFailedController.onPageLoad))
        }

      case Left(_) =>
        Future.successful(Redirect(controllers.subscription.routes.SubscriptionFailedController.onPageLoad))
    }
}
