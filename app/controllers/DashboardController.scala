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

import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.subscription.ReadSubscriptionRequestParameters
import pages.fmDashboardPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ReadSubscriptionService
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DashboardView

import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DashboardController @Inject() (
  val userAnswersConnectors:   UserAnswersConnectors,
  getData:                     DataRetrievalAction,
  identify:                    IdentifierAction,
  requireData:                 DataRequiredAction,
  val readSubscriptionService: ReadSubscriptionService,
  val controllerComponents:    MessagesControllerComponents,
  view:                        DashboardView
)(implicit ec:                 ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val plrReference = extractPlrReference(request.enrolments).orElse(request.session.get("plrId"))
    val userId       = request.userId

    plrReference match {
      case Some(ref) =>
        readSubscriptionService.readSubscription(ReadSubscriptionRequestParameters(userId, ref)).flatMap {
          case Right(_) =>
            userAnswersConnectors.getUserAnswer(userId).flatMap {
              case Some(userAnswers) =>
                userAnswers.get(fmDashboardPage) match {
                  case Some(dashboardInfo) =>
                    Future.successful(
                      Ok(view(dashboardInfo.organisationName, dashboardInfo.registrationDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")), ref))
                    )
                  case None =>
                    Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
                }
              case None =>
                Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            }

          case Left(error) =>
            logger.error(s"Error retrieving subscription: $error")
            Future.successful(InternalServerError("Internal Server Error occurred"))
        }

      case None =>
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }

  private def extractPlrReference(enrolmentsOption: Option[Set[Enrolment]]): Option[String] =
    enrolmentsOption.flatMap { enrolments =>
      enrolments
        .find(_.key.equalsIgnoreCase("HMRC-PILLAR2-ORG"))
        .flatMap(_.identifiers.find(_.key.equalsIgnoreCase("PLRID")))
        .map(_.value)
    }

}
