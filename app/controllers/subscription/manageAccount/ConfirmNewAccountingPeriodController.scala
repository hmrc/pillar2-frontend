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
import controllers.actions.{IdentifierAction, SubscriptionDataRequiredAction, SubscriptionDataRetrievalAction}
import models.longrunningsubmissions.LongRunningSubmission.ManageGroupDetails
import models.requests.SubscriptionDataRequest
import models.subscription.ManageGroupDetailsStatus
import models.{InternalIssueError, MissingReferenceNumberError, UserAnswers}
import pages.{AgentClientPillar2ReferencePage, ManageGroupDetailsStatusPage}
import java.time.Period
import play.api.i18n.I18nSupport
import play.api.mvc.*
import repositories.SessionRepository
import services.{ReferenceNumberService, SubscriptionService}
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeUtils.*
import config.FrontendAppConfig
import views.html.subscriptionview.manageAccount.ConfirmNewAccountingPeriodView

import javax.inject.Named
import scala.concurrent.{ExecutionContext, Future}

class ConfirmNewAccountingPeriodController @Inject() (
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getData:                                SubscriptionDataRetrievalAction,
  requireData:                            SubscriptionDataRequiredAction,
  val controllerComponents:               MessagesControllerComponents,
  sessionRepository:                      SessionRepository,
  referenceNumberService:                 ReferenceNumberService,
  subscriptionService:                    SubscriptionService,
  view:                                   ConfirmNewAccountingPeriodView
)(using ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def durationMonthsAndDays(start: java.time.LocalDate, end: java.time.LocalDate): String = {
    val period = Period.between(start, end)
    val totalMonths = period.getYears * 12 + period.getMonths
    val days        = period.getDays
    (totalMonths, days) match {
      case (0, d) => s"$d days"
      case (m, 0) => s"$m months"
      case (m, d) => s"$m months and $d days"
    }
  }

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      val session                    = request.session
      (for {
        prevStr <- session.get(ManageAccountV2SessionKeys.PreviousAccountingPeriodForSuccess)
        newStr  <- session.get(ManageAccountV2SessionKeys.NewAccountingPeriodForSuccess)
        prev    <- ManageAccountV2SessionKeys.parsePeriodJson(prevStr)
        newP    <- ManageAccountV2SessionKeys.parsePeriodJson(newStr)
      } yield (prev, newP)) match {
        case Some(((prevStart, prevEnd), (newStart, newEnd))) =>
          val durationText = durationMonthsAndDays(newStart, newEnd)
          Future.successful(
            Ok(
              view(
                previousStart = prevStart.toDateFormat,
                previousEnd = prevEnd.toDateFormat,
                newStart = newStart.toDateFormat,
                newEnd = newEnd.toDateFormat,
                durationWarning = Some(durationText),
                isAgent = request.isAgent
              )
            )
          )
        case _ =>
          Future.successful(Redirect(controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad()))
      }
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      given SubscriptionDataRequest[AnyContent] = request
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      val session                    = request.session
      (for {
        prevStr <- session.get(ManageAccountV2SessionKeys.PreviousAccountingPeriodForSuccess)
        newStr  <- session.get(ManageAccountV2SessionKeys.NewAccountingPeriodForSuccess)
        prev    <- ManageAccountV2SessionKeys.parsePeriodJson(prevStr)
        newP    <- ManageAccountV2SessionKeys.parsePeriodJson(newStr)
      } yield (prev, newP)) match {
        case Some(((prevStart, prevEnd), (newStart, newEnd))) =>
          sessionRepository.get(request.userId).flatMap { userAnswers =>
            val updatedAnswers = userAnswers match {
              case Some(answers) => answers.setOrException(ManageGroupDetailsStatusPage, ManageGroupDetailsStatus.InProgress)
              case None          => UserAnswers(request.userId).setOrException(ManageGroupDetailsStatusPage, ManageGroupDetailsStatus.InProgress)
            }
            sessionRepository.set(updatedAnswers).flatMap { _ =>
              updateGroupDetailsInBackground(request.userId, request.subscriptionLocalData, request.enrolments)
                .map(_ =>
                  Redirect(controllers.routes.WaitingRoomController.onPageLoad(ManageGroupDetails))
                    .withSession(request.session + (ManageAccountV2SessionKeys.IsAgentForSuccess -> request.isAgent.toString))
                )
            }
          }
        case _ =>
          Future.successful(Redirect(controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad()))
      }
    }

  private def updateGroupDetailsInBackground(userId: String, subscriptionData: models.subscription.SubscriptionLocalData, enrolments: Set[Enrolment])(using
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
          setStatusOnFailure(userId, ManageGroupDetailsStatus.FailedInternalIssueError)
        case _: Exception =>
          setStatusOnFailure(userId, ManageGroupDetailsStatus.FailException)
        case MissingReferenceNumberError =>
          setStatusOnFailure(userId, ManageGroupDetailsStatus.FailException)
      }
      .map(_ => ())
  }

  private def setStatusOnFailure(userId: String, status: ManageGroupDetailsStatus)(using ec: ExecutionContext): Future[Unit] =
    sessionRepository
      .get(userId)
      .flatMap { maybeUa =>
        val ua = maybeUa.getOrElse(UserAnswers(userId))
        sessionRepository.set(ua.setOrException(ManageGroupDetailsStatusPage, status))
      }
      .map(_ => ())
}
