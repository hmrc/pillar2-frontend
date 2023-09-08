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
import models.{MandatoryInformationMissingError, NfmRegistrationConfirmation}
import models.fm.FilingMember
import models.registration.Registration
import models.requests.DataRequest
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import services.RegisterWithoutIdService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait RegisterAndSubscribe extends Logging {
  val registerWithoutIdService: RegisterWithoutIdService
  val userAnswersConnectors:    UserAnswersConnectors

  def createRegistrationAndSubscription(registration: Registration, filingMemeber: FilingMember)(implicit
    hc:                                               HeaderCarrier,
    ec:                                               ExecutionContext,
    request:                                          DataRequest[AnyContent]
  ): Future[Result] =
    (registration.safeId, filingMemeber.safeId) match {
      case (Some(safeId), Some(fmSafeId)) =>
        println(" Yes I have both safe id -----------------------------")
        Future.successful(Redirect(routes.IndexController.onPageLoad))
      //createSubscription(safeId, fmSafeId)

      case (Some(safeId), None) =>
        println(" Yes I have(Some(safeId), None) id -----------------------------")
        Future.successful(Redirect(routes.IndexController.onPageLoad))
        if (filingMemeber.nfmConfirmation == NfmRegistrationConfirmation.Yes) {
          registerWithoutIdService.sendFmRegistrationWithoutId(request.userId, request.userAnswers).flatMap {
            case Right(fmsafeId) => Future.successful(Redirect(routes.IndexController.onPageLoad)) //createSubscription(safeId, fmSafeid)
            case Left(value) =>
              logger.warn(s"Error $value")
              value match {
                case MandatoryInformationMissingError(_) => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
                case _                                   => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
              }
          }
        } else {
          println(" Came in else part. -----------------------------")
          Future.successful(Redirect(routes.IndexController.onPageLoad))
        }

      case (None, Some(fmSafeId)) =>
        println(" Yes I have(None, Some(fmSafeId))id -----------------------------")
        registerWithoutIdService.sendUpeRegistrationWithoutId(request.userId, request.userAnswers).flatMap {
          case Right(safeId) => Future.successful(Redirect(routes.IndexController.onPageLoad)) //createSubscription(safeId, fmSafeid)
          case Left(value) =>
            logger.warn(s"Error $value")
            value match {
              case MandatoryInformationMissingError(_) => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
              case _                                   => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
            }
        }

      case (None, None) =>
        println(" Yes I have(None, None)id -----------------------------")
        registerWithoutIdService.sendUpeRegistrationWithoutId(request.userId, request.userAnswers).flatMap {
          case Right(safeId) =>
            if (filingMemeber.nfmConfirmation == NfmRegistrationConfirmation.Yes) {
              registerWithoutIdService.sendFmRegistrationWithoutId(request.userId, request.userAnswers).flatMap {
                case Right(fmsafeId) => Future.successful(Redirect(routes.IndexController.onPageLoad)) //createSubscription(safeId, fmSafeid)
                case Left(value) =>
                  logger.warn(s"Error $value")
                  value match {
                    case MandatoryInformationMissingError(_) => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
                    case _                                   => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
                  }
              }
            } else {
              println(" Came in else part. -----------------------------")
              //createSubscription(safeId)
              Future.successful(Redirect(routes.IndexController.onPageLoad))
            }

          case Left(value) =>
            logger.warn(s"Error $value")
            value match {
              case MandatoryInformationMissingError(_) => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
              case _                                   => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
            }

        }
      case _ => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
    }
}
