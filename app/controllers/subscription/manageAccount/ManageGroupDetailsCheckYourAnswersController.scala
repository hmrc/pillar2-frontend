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

package controllers.subscription.manageAccount

import cats.data.OptionT
import cats.implicits.*
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{IdentifierAction, SubscriptionDataRequiredAction, SubscriptionDataRetrievalAction}
import controllers.routes
import models.requests.SubscriptionDataRequest
import models.subscription.ManageGroupDetailsStatus.*
import models.subscription.{ManageGroupDetailsStatus, SubscriptionLocalData}
import models.{InternalIssueError, UserAnswers}
import pages.{AgentClientPillar2ReferencePage, ManageGroupDetailsStatusPage}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.*
import repositories.SessionRepository
import services.{ReferenceNumberService, SubscriptionService}
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.manageAccount.*
import viewmodels.govuk.summarylist.*
import views.html.subscriptionview.manageAccount.ManageGroupDetailsCheckYourAnswersView

import javax.inject.Named
import scala.concurrent.{ExecutionContext, Future}

class ManageGroupDetailsCheckYourAnswersController @Inject() (
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getData:                                SubscriptionDataRetrievalAction,
  requireData:                            SubscriptionDataRequiredAction,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   ManageGroupDetailsCheckYourAnswersView,
  sessionRepository:                      SessionRepository,
  subscriptionService:                    SubscriptionService,
  referenceNumberService:                 ReferenceNumberService,
  val userAnswersConnectors:              UserAnswersConnectors
)(using ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { request =>
      given SubscriptionDataRequest[AnyContent] = request
      sessionRepository.get(request.userId).flatMap {
        case None          => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        case Some(answers) =>
          answers.get(ManageGroupDetailsStatusPage) match {
            case Some(InProgress) =>
              Future.successful(Redirect(controllers.subscription.manageAccount.routes.ManageGroupDetailsWaitingRoomController.onPageLoad))
            case _ =>
              val list = SummaryListViewModel(
                rows = Seq(
                  MneOrDomesticSummary.row(),
                  GroupAccountingPeriodSummary.row(),
                  GroupAccountingPeriodStartDateSummary.row(),
                  GroupAccountingPeriodEndDateSummary.row()
                ).flatten
              )
              Future.successful(Ok(view(list, request.isAgent, request.subscriptionLocalData.organisationName)))
          }
      }
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { request =>
      given Request[AnyContent] = request
      logger.info(s"[ManageGroupDetailsCheckYourAnswers] Submission started for user ${request.userId}")
      sessionRepository.get(request.userId).flatMap { userAnswers =>
        userAnswers.flatMap(_.get(ManageGroupDetailsStatusPage)) match {
          case Some(ManageGroupDetailsStatus.SuccessfullyCompleted) =>
            Future.successful(Redirect(controllers.subscription.manageAccount.routes.ManageGroupDetailsWaitingRoomController.onPageLoad))
          case _ =>
            for {
              userAnswers <- sessionRepository.get(request.userId)
              updatedAnswers = userAnswers match {
                                 case Some(answers) => answers.setOrException(ManageGroupDetailsStatusPage, ManageGroupDetailsStatus.InProgress)
                                 case None          =>
                                   UserAnswers(request.userId).setOrException(ManageGroupDetailsStatusPage, ManageGroupDetailsStatus.InProgress)
                               }
              _ <- sessionRepository.set(updatedAnswers)
            } yield {
              updateGroupDetailsInBackground(request.userId, request.subscriptionLocalData, request.enrolments)
              logger.info(s"[ManageGroupDetailsCheckYourAnswers] Redirecting to waiting room for ${request.userId}")
              Redirect(controllers.subscription.manageAccount.routes.ManageGroupDetailsWaitingRoomController.onPageLoad)
            }
        }
      }
    }

  private def updateGroupDetailsInBackground(userId: String, subscriptionData: SubscriptionLocalData, enrolments: Set[Enrolment])(using
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Unit] = {
    val result = for {
      userAnswers     <- OptionT.liftF(sessionRepository.get(userId))
      referenceNumber <- OptionT
                           .fromOption[Future](userAnswers.flatMap(_.get(AgentClientPillar2ReferencePage)))
                           .orElse(OptionT.fromOption[Future](referenceNumberService.get(None, enrolments = Some(enrolments))))
      _ <- OptionT.liftF(subscriptionService.amendContactOrGroupDetails(userId, referenceNumber, subscriptionData))
      updatedAnswersOnSuccess = userAnswers match {
                                  case Some(answers) =>
                                    answers.setOrException(ManageGroupDetailsStatusPage, ManageGroupDetailsStatus.SuccessfullyCompleted)
                                  case None =>
                                    UserAnswers(userId).setOrException(ManageGroupDetailsStatusPage, ManageGroupDetailsStatus.SuccessfullyCompleted)
                                }
      _ <- OptionT.liftF(sessionRepository.set(updatedAnswersOnSuccess))
    } yield ()

    result.value
      .recoverWith {
        case InternalIssueError =>
          logger.error(s"[ManageGroupDetailsCheckYourAnswers] Subscription update failed for $userId due to InternalIssueError")
          setStatusOnFailure(userId, ManageGroupDetailsStatus.FailedInternalIssueError)
        case e: Exception =>
          logger.error(s"[ManageGroupDetailsCheckYourAnswers] Subscription update failed for $userId: ${e.getMessage}")
          setStatusOnFailure(userId, ManageGroupDetailsStatus.FailException)
      }
      .map(_ => ())
  }

  private def setStatusOnFailure(userId: String, status: ManageGroupDetailsStatus)(using ec: ExecutionContext): Future[Option[Unit]] =
    sessionRepository
      .get(userId)
      .flatMap { maybeUa =>
        val userAnswersToUpdate = maybeUa.getOrElse(UserAnswers(userId))
        sessionRepository.set(userAnswersToUpdate.setOrException(ManageGroupDetailsStatusPage, status))
      }
      .map(_ => None)

}
