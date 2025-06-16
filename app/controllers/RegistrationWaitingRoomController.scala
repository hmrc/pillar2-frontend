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

package controllers

import config.FrontendAppConfig
import controllers.actions.{IdentifierAction, SessionDataRequiredAction, SessionDataRetrievalAction}
import models.subscription.SubscriptionStatus._
import pages._
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.registrationview.RegistrationWaitingRoomView

import java.time.{Duration, LocalDateTime}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationWaitingRoomController @Inject() (
  getData:                  SessionDataRetrievalAction,
  identify:                 IdentifierAction,
  requireData:              SessionDataRequiredAction,
  sessionRepository:        SessionRepository,
  subscriptionService:      SubscriptionService,
  val controllerComponents: MessagesControllerComponents,
  view:                     RegistrationWaitingRoomView
)(implicit appConfig:       FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val currentTime    = LocalDateTime.now()
    val startTime      = request.userAnswers.get(SubscriptionStartTimePage).getOrElse(currentTime)
    val attemptCount   = request.userAnswers.get(SubscriptionAttemptCountPage).getOrElse(0)
    val elapsedSeconds = Duration.between(startTime, currentTime).getSeconds

    request.userAnswers.get(SubscriptionStatusPage) match {
      case Some(SuccessfullyCompletedSubscription) =>
        Future.successful(Redirect(routes.RegistrationConfirmationController.onPageLoad))

      case Some(RegistrationInProgress) =>
        request.userAnswers.get(PlrReferencePage) match {
          case Some(plrReference) => Future.successful(Redirect(controllers.routes.RegistrationInProgressController.onPageLoad(plrReference)))
          case None =>
            logger.warn("RegistrationInProgress status found but no PLR reference available")
            Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        }

      case Some(FailedWithDuplicatedSubmission) =>
        Future.successful(Redirect(controllers.subscription.routes.SubscriptionFailureController.onPageLoad))
      case Some(FailedWithUnprocessableEntity) =>
        Future.successful(Redirect(controllers.subscription.routes.SubscriptionFailureController.onPageLoad))
      case Some(FailedWithInternalIssueError) =>
        Future.successful(Redirect(controllers.subscription.routes.SubscriptionFailedController.onPageLoad))
      case Some(FailedWithDuplicatedSafeIdError) =>
        Future.successful(Redirect(controllers.subscription.routes.DuplicateSafeIdController.onPageLoad))
      case Some(FailedWithNoMneOrDomesticValueFoundError) =>
        Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))

      case _ =>
        // Check if we have PLR reference for subscription checking
        request.userAnswers.get(PlrReferencePage) match {
          case Some(plrReference) =>
            val updatedAnswersWithStartTime = if (request.userAnswers.get(SubscriptionStartTimePage).isEmpty) {
              request.userAnswers.setOrException(SubscriptionStartTimePage, currentTime)
            } else {
              request.userAnswers
            }

            if (elapsedSeconds >= 20) {
              for {
                finalAnswers <- Future.fromTry(updatedAnswersWithStartTime.set(SubscriptionStatusPage, RegistrationInProgress))
                _            <- sessionRepository.set(finalAnswers)
              } yield Redirect(controllers.routes.RegistrationInProgressController.onPageLoad(plrReference))
            } else {
              subscriptionService
                .readSubscription(plrReference)
                .flatMap { _ =>
                  for {
                    successAnswers <- Future.fromTry(updatedAnswersWithStartTime.set(SubscriptionStatusPage, SuccessfullyCompletedSubscription))
                    _              <- sessionRepository.set(successAnswers)
                  } yield Redirect(routes.RegistrationConfirmationController.onPageLoad)
                }
                .recover { case _ =>
                  val refreshInterval = if (elapsedSeconds < 9) 3 else 5
                  val updatedAttempts = updatedAnswersWithStartTime.setOrException(SubscriptionAttemptCountPage, attemptCount + 1)
                  sessionRepository.set(updatedAttempts)
                  Ok(view(None, Some(refreshInterval)))
                }
            }

          case None =>
            // No PLR reference - show basic waiting room
            Future.successful(Ok(view(None, Some(2))))
        }
    }
  }
}
