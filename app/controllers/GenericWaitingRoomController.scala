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

package controllers

import config.{FrontendAppConfig, WaitingRoomConfig, WaitingRoomRegistry}
import models.requests.SubscriptionDataRequest
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import play.twirl.api.Html
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.GenericWaitingRoomView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GenericWaitingRoomController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  sessionRepository: SessionRepository,
  registry: WaitingRoomRegistry,
  view: GenericWaitingRoomView
)(implicit val appConfig: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(feature: String): Action[AnyContent] = Action.async { implicit request =>
    registry.getConfig(feature) match {
      case Some(config) =>
        config.actionBuilder.invokeBlock(
          request,
          (subscriptionRequest: SubscriptionDataRequest[AnyContent]) =>
            handleRequest(subscriptionRequest, config, feature)
        )
      case None =>
        logger.error(s"GenericWaitingRoomController: No configuration found for feature '$feature'")
        Future.successful(NotFound)
    }
  }

  private def handleRequest(
    request: SubscriptionDataRequest[AnyContent],
    config: WaitingRoomConfig,
    feature: String
  ): Future[Result] = {
    val messages: Messages = messagesApi.preferred(request)
    sessionRepository.get(request.userId).flatMap {
      case Some(userAnswers) =>
        val status = userAnswers.get(config.statusGettable)
        logger.info(s"GenericWaitingRoomController: Current status for $feature = $status")

        status match {
          case Some(s) if s == config.statusComplete =>
            logger.info(s"GenericWaitingRoomController: Status is ${config.statusComplete}, redirecting to success")
            Future.successful(Redirect(config.successCall))

          case Some(s) if s == "error" => // Assumes "error" is standard, or could be in config
            logger.info(s"GenericWaitingRoomController: Status is error, redirecting to failure")
            Future.successful(Redirect(config.failureCall))

          case _ =>
            logger.info("GenericWaitingRoomController: Status is processing, showing waiting room with refresh header")
            Future.successful(
              Ok(
                view(
                  pageTitle = messages(config.pageTitleKey),
                  heading = messages(config.headingKey),
                  subHeading = messages(config.subHeadingKey),
                  pollInterval = config.pollInterval,
                  redirectUrl = routes.GenericWaitingRoomController.onPageLoad(feature),
                  afterHeadingsMessage = Some(messages(config.redirectMessageKey))
                )(request, appConfig, messages)
              ).withHeaders(
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
