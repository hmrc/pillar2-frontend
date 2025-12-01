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

package controllers.btn

import config.FrontendAppConfig
import controllers.actions.*
import models.btn.BTNStatus
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.*
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.btn.BTNWaitingRoomView

import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BTNWaitingRoomController @Inject() (
  val controllerComponents:               MessagesControllerComponents,
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  dataRetrievalAction:                    SubscriptionDataRetrievalAction,
  dataRequiredAction:                     SubscriptionDataRequiredAction,
  sessionRepository:                      SessionRepository,
  btnWaitingRoomView:                     BTNWaitingRoomView
)(using appConfig: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen dataRetrievalAction andThen dataRequiredAction).async { request =>
    given Request[AnyContent] = request
    sessionRepository.get(request.userId).flatMap {
      case Some(userAnswers) =>
        val status = userAnswers.get(BTNStatus)

        logger.info(s"BTNWaitingRoomController.onPageLoad: Current BTN status = $status")

        status match {
          case Some(BTNStatus.submitted) =>
            logger.info(s"BTNWaitingRoomController: Status is submitted, redirecting to confirmation page")
            Future.successful(Redirect(routes.BTNConfirmationController.onPageLoad))

          case Some(BTNStatus.error) =>
            logger.info(s"BTNWaitingRoomController: Status is error, redirecting to problem page")
            Future.successful(Redirect(routes.BTNProblemWithServiceController.onPageLoad))

          case _ =>
            logger.info("BTNWaitingRoomController: Status is processing, showing waiting room with refresh header")
            Future.successful(
              Ok(btnWaitingRoomView()).withHeaders(
                "Refresh"       -> s"${appConfig.btnWaitingRoomPollIntervalSeconds}",
                "Cache-Control" -> "no-store, no-cache, must-revalidate",
                "Pragma"        -> "no-cache",
                "Expires"       -> "0"
              )
            )
        }
      case None =>
        logger.error("user answers not found")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }
}
