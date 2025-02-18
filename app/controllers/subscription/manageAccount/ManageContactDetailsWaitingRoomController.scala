/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.subscription.manageAccount

import config.FrontendAppConfig
import controllers.actions.{IdentifierAction, SubscriptionDataRetrievalAction}
import models.subscription.ManageContactDetailsStatus
import pages.ManageContactDetailsStatusPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.manageAccount.ManageContactDetailsWaitingRoomView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class ManageContactDetailsWaitingRoomController @Inject() (
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getData:                                SubscriptionDataRetrievalAction,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   ManageContactDetailsWaitingRoomView,
  sessionRepository:                      SessionRepository
)(implicit ec:                            ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData).async { implicit request =>
    logger.info(s"[ManageContactDetailsWaitingRoom] Loading waiting room for user ${request.userId}")

    sessionRepository
      .get(request.userId)
      .flatMap { refreshedAnswers =>
        val status = refreshedAnswers.flatMap(_.get(ManageContactDetailsStatusPage))
        logger.info(s"[ManageContactDetailsWaitingRoom] Current status for ${request.userId}: $status")

        status match {
          case Some(ManageContactDetailsStatus.SuccessfullyCompleted) =>
            logger.info(s"[ManageContactDetailsWaitingRoom] SuccessfullyCompleted detected for ${request.userId}, redirecting to dashboard")
            Future.successful(Redirect(controllers.routes.DashboardController.onPageLoad))

          case Some(ManageContactDetailsStatus.InProgress) =>
            logger.info(s"[ManageContactDetailsWaitingRoom] InProgress status for ${request.userId}, re-rendering spinner")
            sessionRepository.set(
              refreshedAnswers.get.setOrException(ManageContactDetailsStatusPage, ManageContactDetailsStatus.SuccessfullyCompleted)
            )
            Future.successful(Ok(view(Some(ManageContactDetailsStatus.InProgress))))

          case _ =>
            logger.warn(s"[ManageContactDetailsWaitingRoom] Missing or unexpected status for ${request.userId}, redirecting to error page")
            Future.successful(Redirect(controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad))
        }
      }
      .recover { case ex =>
        logger.error(s"[ManageContactDetailsWaitingRoom] Error while loading waiting room for ${request.userId}: ${ex.getMessage}")
        Redirect(controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad)
      }
  }
}
