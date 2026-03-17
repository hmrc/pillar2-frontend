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
import connectors.{SubscriptionConnector, UserAnswersConnectors}
import controllers.actions.{IdentifierAction, SubscriptionDataRequiredAction, SubscriptionDataRetrievalAction}
import controllers.routes
import models.*
import models.longrunningsubmissions.LongRunningSubmission.ManageGroupDetails
import models.requests.SubscriptionDataRequest
import models.subscription.ManageGroupDetailsStatus.*
import models.subscription.{ManageGroupDetailsStatus, SubscriptionLocalData}
import pages.*
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.*
import repositories.SessionRepository
import services.{ReferenceNumberService, SubscriptionService}
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.DateTimeUtils.*
import viewmodels.checkAnswers.manageAccount.*
import viewmodels.govuk.summarylist.*
import views.html.subscriptionview.manageAccount.{ManageGroupDetailsCheckYourAnswersView, ManageGroupDetailsMultiPeriodView}

import javax.inject.Named
import scala.concurrent.{ExecutionContext, Future}

class ManageGroupDetailsCheckYourAnswersController @Inject() (
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getData:                                SubscriptionDataRetrievalAction,
  requireData:                            SubscriptionDataRequiredAction,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   ManageGroupDetailsCheckYourAnswersView,
  multiPeriodView:                        ManageGroupDetailsMultiPeriodView,
  sessionRepository:                      SessionRepository,
  subscriptionService:                    SubscriptionService,
  subscriptionConnector:                  SubscriptionConnector,
  referenceNumberService:                 ReferenceNumberService,
  val userAnswersConnectors:              UserAnswersConnectors
)(using ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData).async { request =>
      request.maybeSubscriptionLocalData match {
        case None =>
          logger.warn(s"[ManageGroupDetailsCheckYourAnswers] No subscription cache for user ${request.userId}, redirecting to journey recovery")
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        case Some(data) =>
          val req = SubscriptionDataRequest(request.request, request.userId, data, request.enrolments, request.isAgent)
          renderSummaryPage(req)
      }
    }

  private def renderSummaryPage(request: SubscriptionDataRequest[AnyContent]): Future[Result] = {
    given SubscriptionDataRequest[AnyContent] = request
    given hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    sessionRepository.get(request.userId).flatMap {
      case None          => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      case Some(answers) =>
        answers.get(ManageGroupDetailsStatusPage) match {
          case Some(InProgress) =>
            Future.successful(Redirect(controllers.routes.WaitingRoomController.onPageLoad(ManageGroupDetails)))
          case _ =>
            if appConfig.amendMultipleAccountingPeriods then
              val localDataF: Future[SubscriptionLocalData] =
                subscriptionService.readSubscriptionV2AndSave(request.userId, request.subscriptionLocalData.plrReference)
              localDataF
                .map { local =>
                  given msgs: play.api.i18n.Messages = request.messages
                  val amendablePeriods = local.accountingPeriods
                    .getOrElse(Seq.empty)
                    .sortBy(_.endDate)(Ordering[java.time.LocalDate].reverse)
                  val periodCards = amendablePeriods.zipWithIndex.map { case (p, displayIdx) =>
                    val title =
                      if displayIdx == 0 then msgs("manageGroupDetails.multiPeriod.currentPeriod")
                      else if displayIdx == 1 then msgs("manageGroupDetails.multiPeriod.previousPeriod")
                      else msgs("manageGroupDetails.multiPeriod.periodLabel", displayIdx + 1)
                    (
                      title,
                      p.startDate.toDateFormat,
                      p.endDate.toDateFormat,
                      Some(
                        controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController
                          .selectPeriod(displayIdx)
                          .url
                      )
                    )
                  }
                  val locationKey =
                    if local.subMneOrDomestic == MneOrDomestic.Uk then "mneOrDomestic.uk" else "mneOrDomestic.ukAndOther"
                  Ok(
                    multiPeriodView(
                      locationMessageKey = locationKey,
                      periodCards = periodCards,
                      isEmpty = amendablePeriods.isEmpty,
                      isAgent = request.isAgent,
                      organisationName = local.organisationName,
                      plrReference = local.plrReference
                    )
                  )
                }
                .recover { case _ =>
                  logger.warn(
                    "[ManageGroupDetailsCheckYourAnswers] Display Subscription V2 failed (e.g. 404), falling back to single-period view"
                  )
                  val list = SummaryListViewModel(
                    rows = Seq(
                      MneOrDomesticSummary.row(),
                      GroupAccountingPeriodSummary.row(),
                      GroupAccountingPeriodStartDateSummary.row(),
                      GroupAccountingPeriodEndDateSummary.row()
                    ).flatten
                  )
                  Ok(view(list, request.isAgent, request.subscriptionLocalData.organisationName))
                }
            else
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

  /** Multi-period: cache selected period and redirect to Data Entry (PIL-2856). */
  def selectPeriod(index: Int): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { request =>
      given hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      val periods = request.subscriptionLocalData.accountingPeriods.getOrElse(Seq.empty)
      val sorted  = periods.sortBy(_.endDate)(Ordering[java.time.LocalDate].reverse)
      sorted.lift(index) match {
        case Some(period) =>
          val updated = request.subscriptionLocalData.set(SubAccountingPeriodPage, period.toAccountingPeriod) match {
            case scala.util.Success(ua) => ua
            case scala.util.Failure(_)  => request.subscriptionLocalData
          }
          subscriptionConnector
            .save(request.userId, Json.toJson(updated))
            .flatMap { _ =>
              for {
                maybeUserAnswers <- sessionRepository.get(request.userId)
                updatedAnswers   <- Future.fromTry(
                                    maybeUserAnswers
                                      .getOrElse(UserAnswers(request.userId))
                                      .remove(NewAccountingPeriodPage)
                                  )
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(controllers.subscription.manageAccount.routes.NewAccountingPeriodController.onPageLoad(NormalMode))
            }
        case None =>
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { request =>
      given Request[AnyContent] = request
      logger.info(s"[ManageGroupDetailsCheckYourAnswers] Submission started for user ${request.userId}")
      sessionRepository.get(request.userId).flatMap { userAnswers =>
        userAnswers.flatMap(_.get(ManageGroupDetailsStatusPage)) match {
          case Some(ManageGroupDetailsStatus.SuccessfullyCompleted) =>
            Future.successful(Redirect(controllers.routes.WaitingRoomController.onPageLoad(ManageGroupDetails)))
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
              Redirect(controllers.routes.WaitingRoomController.onPageLoad(ManageGroupDetails))
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

    result
      .getOrElseF(Future.failed(MissingReferenceNumberError))
      .recoverWith {
        case InternalIssueError =>
          logger.error(s"[ManageGroupDetailsCheckYourAnswers] Subscription update failed for $userId due to InternalIssueError")
          setStatusOnFailure(userId, ManageGroupDetailsStatus.FailedInternalIssueError)
        case e: Exception =>
          logger.error(s"[ManageGroupDetailsCheckYourAnswers] Subscription update failed for $userId: ${e.getMessage}")
          setStatusOnFailure(userId, ManageGroupDetailsStatus.FailException)
        case MissingReferenceNumberError =>
          logger.error(s"[ManageGroupDetailsCheckYourAnswers] Pillar 2 Reference Number for $userId not found")
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
