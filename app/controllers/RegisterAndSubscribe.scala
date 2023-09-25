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
import models.registration.Registration
import models.requests.DataRequest
import models.MandatoryInformationMissingError
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import services.{RegisterWithoutIdService, SubscriptionService}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait RegisterAndSubscribe extends Logging {
  val registerWithoutIdService: RegisterWithoutIdService
  val subscriptionService:      SubscriptionService
  val userAnswersConnectors:    UserAnswersConnectors

  def createRegistrationAndSubscription(registration: Registration, filingMemeber: FilingMember)(implicit
    hc:                                               HeaderCarrier,
    ec:                                               ExecutionContext,
    request:                                          DataRequest[AnyContent]
  ): Future[Result] =
    (registration.safeId, filingMemeber.safeId) match {
      case (Some(safeId), Some(fmSafeId)) =>
        println(" Yes I have both safe id -----------------------------")
        createSubscription(safeId, Some(fmSafeId))
      // do the enrolement with PIllar2 id.

      case (Some(safeId), None) =>
        println(" Yes I have(Some(safeId), None) id -----------------------------")
        if (filingMemeber.nfmConfirmation) {
          registerWithoutIdService.sendFmRegistrationWithoutId(request.userId, request.userAnswers).flatMap {
            case Right(fmSafeId) =>
              createSubscription(safeId, Some(fmSafeId.value))
            case Left(value) =>
              logger.warn(s"Error $value")
              value match {
                case MandatoryInformationMissingError(_) => Future.successful(Redirect(routes.ErrorController.onPageLoad))
                case _                                   => Future.successful(Redirect(routes.ErrorController.onPageLoad))
              }
          }
        } else {
          println(" Came in else part. -----------------------------")
          createSubscription(safeId)
        }

      case (None, Some(fmSafeId)) =>
        println(" Yes I have(None, Some(fmSafeId))id -----------------------------")
        registerWithoutIdService.sendUpeRegistrationWithoutId(request.userId, request.userAnswers).flatMap {
          case Right(upeSafeId) =>
            //createSubscription(safeId, fmSafeid)
            createSubscription(upeSafeId.value, Some(fmSafeId))
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
          case Right(upeSafeId) =>
            if (filingMemeber.nfmConfirmation) {
              registerWithoutIdService.sendFmRegistrationWithoutId(request.userId, request.userAnswers).flatMap {
                case Right(fmSafeId) =>
                  createSubscription(upeSafeId.value, Some(fmSafeId.value))
                case Left(value) =>
                  logger.warn(s"Error $value")
                  value match {
                    case MandatoryInformationMissingError(_) => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
                    case _                                   => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
                  }
              }
            } else {
              println(" Came in else part. -----------------------------")
              createSubscription(upeSafeId.value)
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

  private def createSubscription(upeSafeId: String, fmSafeId: Option[String] = None)(implicit
    hc:                                     HeaderCarrier,
    ec:                                     ExecutionContext,
    request:                                DataRequest[AnyContent]
  ): Future[Result] =
    subscriptionService.checkAndCreateSubscription(request.userId, upeSafeId, fmSafeId).map {
      case Right(successReponse) => Redirect(routes.IndexController.onPageLoad)
      case Left(value)           => Redirect(routes.ErrorController.onPageLoad)
    }
}
