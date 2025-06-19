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
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.requests.OptionalDataRequest
import models.subscription.ReadSubscriptionRequestParameters
import models.{InternalIssueError, UserAnswers}
import pages._
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{ReferenceNumberService, SubscriptionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{DashboardView, RegistrationInProgressView}

import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class DashboardController @Inject() (
  val userAnswersConnectors:              UserAnswersConnectors,
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getData:                                DataRetrievalAction,
  val subscriptionService:                SubscriptionService,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   DashboardView,
  referenceNumberService:                 ReferenceNumberService,
  sessionRepository:                      SessionRepository,
  registrationInProgressView:             RegistrationInProgressView
)(implicit ec:                            ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData).async { implicit request: OptionalDataRequest[AnyContent] =>
      (for {
        mayBeUserAnswer <- OptionT.liftF(sessionRepository.get(request.userId))
        userAnswers = mayBeUserAnswer.getOrElse(UserAnswers(request.userId))
        referenceNumber <- OptionT
                             .fromOption[Future](userAnswers.get(AgentClientPillar2ReferencePage))
                             .orElse(OptionT.fromOption[Future](referenceNumberService.get(Some(userAnswers), request.enrolments)))
        updatedAnswers  <- OptionT.liftF(Future.fromTry(userAnswers.set(PlrReferencePage, referenceNumber)))
        updatedAnswers1 <- OptionT.liftF(Future.fromTry(updatedAnswers.set(RedirectToASAHome, false)))
        updatedAnswers2 <- OptionT.liftF(Future.fromTry(updatedAnswers1.remove(RepaymentsStatusPage)))
        updatedAnswers3 <- OptionT.liftF(Future.fromTry(updatedAnswers2.remove(RepaymentCompletionStatus)))
        updatedAnswers4 <- OptionT.liftF(Future.fromTry(updatedAnswers3.remove(RfmStatusPage)))
        updatedAnswers5 <- OptionT.liftF(Future.fromTry(updatedAnswers4.remove(RepaymentsWaitingRoomVisited)))
        _               <- OptionT.liftF(sessionRepository.set(updatedAnswers5))
        dashboard <- OptionT.liftF(
                       subscriptionService
                         .readAndCacheSubscription(ReadSubscriptionRequestParameters(request.userId, referenceNumber))
                         .recover {
                           case InternalIssueError =>
                             logger.info(s"DashboardController - subscription is in progress for PLR reference: $referenceNumber")
                             throw new RuntimeException("REGISTRATION_IN_PROGRESS:" + referenceNumber)
                           case other => throw other
                         }
                     )
      } yield Ok(
        view(
          dashboard.upeDetails.organisationName,
          dashboard.upeDetails.registrationDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
          referenceNumber,
          inactiveStatus = dashboard.accountStatus.exists(_.inactive),
          agentView = request.isAgent
        )
      )).recover {
        case ex: RuntimeException if ex.getMessage.startsWith("REGISTRATION_IN_PROGRESS:") =>
          val plrRef = ex.getMessage.drop("REGISTRATION_IN_PROGRESS:".length)
          Redirect(controllers.routes.RegistrationInProgressController.onPageLoad(plrRef))
        case _ =>
          logger.error("DashboardController - read subscription failed as no valid Json was returned from the controller")
          Redirect(controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad)
      }.getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

    }
}
