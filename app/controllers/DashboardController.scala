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

import cats.data.OptionT
import cats.implicits._
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.IdentifierAction
import models.InternalIssueError
import models.subscription.ReadSubscriptionRequestParameters
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{ReferenceNumberService, SubscriptionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DashboardView

import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DashboardController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  val subscriptionService:   SubscriptionService,
  val controllerComponents:  MessagesControllerComponents,
  view:                      DashboardView,
  referenceNumberService:    ReferenceNumberService,
  sessionRepository:         SessionRepository
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = identify.async { implicit request =>
    val x = for {
      userAnswers     <- OptionT.liftF(sessionRepository.get(request.userId))
      referenceNumber <- OptionT.fromOption[Future](referenceNumberService.get(userAnswers, Some(request.enrolments)))
      dashboard       <- OptionT.liftF(subscriptionService.readSubscription(ReadSubscriptionRequestParameters(request.userId, referenceNumber)))
    } yield Ok(
      view(
        dashboard.upeDetails.organisationName,
        dashboard.upeDetails.registrationDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
        referenceNumber,
        inactiveStatus = dashboard.accountStatus.exists(_.inactive)
      )
    )
    x.recover { case InternalIssueError =>
      logger.error(
        s"[ read subscription failed as no valid Json was returned from the controller"
      )
      Redirect(routes.ViewAmendSubscriptionFailedController.onPageLoad)
    }.getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))

  }
}
