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
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.InternalIssueError
import models.subscription.ReadSubscriptionRequestParameters
import pages.{fmDashboardPage, plrReferencePage, subAccountStatusPage}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.ReadSubscriptionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.{Pillar2Reference, Pillar2SessionKeys}
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
  view:                        DashboardView,
  sessionRepository:           SessionRepository
)(implicit ec:                 ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  // noinspection ScalaStyle
  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    val userId = request.userId

    sessionRepository.get(request.userAnswers.id).flatMap { optionalUserAnswer =>
      val pillar2ID = optionalUserAnswer match {
        case Some(userAnswers) => Pillar2Reference.getPillar2ID(request.enrolments).orElse(userAnswers.get(plrReferencePage))
        case None              => Pillar2Reference.getPillar2ID(request.enrolments)
      }
      pillar2ID
        .map { plrId =>
          (for {
            _ <- readSubscriptionService.readSubscription(ReadSubscriptionRequestParameters(userId, plrId))
            _ <- userAnswersConnectors.getUserAnswer(request.userId)
          } yield request.userAnswers
            .get(fmDashboardPage)
            .map { dashboard =>
              val inactiveStatus = request.userAnswers
                .get(subAccountStatusPage)
                .exists { acctStatus =>
                  acctStatus.inactive
                }
              Ok(
                view(
                  dashboard.organisationName,
                  dashboard.registrationDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
                  plrId,
                  inactiveStatus,
                  appConfig.showPaymentsSection
                )
              )
            }
            .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
            .recover { case InternalIssueError =>
              logger.error(
                s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - read subscription failed as no valid Json was returned from the controller"
              )
              Redirect(routes.ViewAmendSubscriptionFailedController.onPageLoad)
            }

        }
        .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))

    }
  }
}
