/*
 * Copyright 2026 HM Revenue & Customs
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
import config.FrontendAppConfig
import controllers.actions.*
import models.*
import models.longrunningsubmissions.LongRunningSubmission
import models.subscription.*
import pages.*
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.*
import repositories.SessionRepository
import services.{ReferenceNumberService, SubscriptionService}
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.DateTimeUtils.*
import utils.{AmendAccountingPeriodDurationFormatter, DateTimeUtils}
import views.html.subscriptionview.manageAccount.AmendAccountingPeriodCYAView

import java.time.{LocalDate, ZonedDateTime}
import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future, blocking}

class AmendAccountingPeriodCYAController @Inject() (
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  checkAmendMultipleAPScreens:            AmendMultipleAccountingPeriodScreensAction,
  getData:                                SubscriptionDataRetrievalAction,
  requireData:                            SubscriptionDataRequiredAction,
  sessionRepository:                      SessionRepository,
  subscriptionService:                    SubscriptionService,
  referenceNumberService:                 ReferenceNumberService,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   AmendAccountingPeriodCYAView
)(using ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen checkAmendMultipleAPScreens andThen getData andThen requireData).async { request =>
      given Request[AnyContent] = request
      sessionRepository.get(request.userId).map { maybeUserAnswers =>
        given Messages = request.messages
        (
          maybeUserAnswers.flatMap(_.get(NewAccountingPeriodPage)),
          request.subscriptionLocalData.get(SubAccountingPeriodPage),
          request.subscriptionLocalData.accountingPeriods
        ) match {
          case (Some(newPeriod), Some(existingPeriod), Some(allPeriods)) =>
            val affected              = findAffectedPeriods(newPeriod.startDate, newPeriod.endDate, allPeriods)
            val predicted             = predictMicroPeriods(newPeriod, affected)
            val newDurationText       = AmendAccountingPeriodDurationFormatter.formatInclusivePeriod(newPeriod.startDate, newPeriod.endDate)
            val predictedWithDuration = predicted.map { p =>
              (p, AmendAccountingPeriodDurationFormatter.formatInclusivePeriod(p.startDate, p.endDate))
            }
            val sameDateEntered: Boolean = (existingPeriod.startDate, existingPeriod.endDate) == (newPeriod.startDate, newPeriod.endDate)

            Ok(
              view(
                newPeriod,
                newDurationText,
                predictedWithDuration,
                request.isAgent,
                request.subscriptionLocalData.organisationName,
                request.subscriptionLocalData.plrReference,
                sameDateEntered
              )
            )
          case _ =>
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
      }
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen checkAmendMultipleAPScreens andThen getData andThen requireData).async { request =>
      given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      sessionRepository.get(request.userId).flatMap { maybeUserAnswers =>
        val maybeNewPeriod = maybeUserAnswers.flatMap(_.get(NewAccountingPeriodPage))
        val allPeriods     = request.subscriptionLocalData.accountingPeriods.getOrElse(Seq.empty)

        (maybeNewPeriod, allPeriods) match {
          case (Some(newPeriod), periods) if periods.nonEmpty =>
            for {
              ua           <- Future.successful(maybeUserAnswers.getOrElse(UserAnswers(request.userId)))
              withOriginal <- Future.fromTry(ua.set(OriginalAccountingPeriodsPage, periods))
              withStatus   <- Future.fromTry(withOriginal.set(AmendAccountingPeriodStatusPage, AmendAccountingPeriodStatus.InProgress))
              _            <- sessionRepository.set(withStatus)
            } yield {
              amendAccountingPeriodsInBackground(
                request.userId,
                request.subscriptionLocalData,
                request.enrolments,
                allPeriods,
                newPeriod
              )
              logger.info(s"[AmendAccountingPeriodCYA] Redirecting to waiting room for ${request.userId}")
              Redirect(controllers.routes.WaitingRoomController.onPageLoad(LongRunningSubmission.AmendAccountingPeriod))
            }
          case _ =>
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
      }
    }

  private def amendAccountingPeriodsInBackground(
    userId:           String,
    subscriptionData: SubscriptionLocalData,
    enrolments:       Set[Enrolment],
    allPeriods:       Seq[AccountingPeriodV2],
    newPeriod:        AccountingPeriod
  )(using hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    val affected = findAffectedPeriods(newPeriod.startDate, newPeriod.endDate, allPeriods)

    val result = for {
      userAnswers     <- OptionT.liftF(sessionRepository.get(userId))
      referenceNumber <- OptionT
                           .fromOption[Future](userAnswers.flatMap(_.get(AgentClientPillar2ReferencePage)))
                           .orElse(OptionT.fromOption[Future](referenceNumberService.get(None, enrolments = Some(enrolments))))
      _ <- OptionT.liftF(
             subscriptionService.amendAccountingPeriods(userId, referenceNumber, subscriptionData, affected, newPeriod)
           )
      updatedPeriods <- OptionT.liftF(
                          readSubscriptionWithRetry(userId, referenceNumber)
                        )
      timestamp = ZonedDateTime.now(DateTimeUtils.utcZoneId).toDateAtTimeFormat
      latestAnswers <- OptionT.liftF(sessionRepository.get(userId))
      answersToUpdate = latestAnswers.getOrElse(UserAnswers(userId))
      withUpdatedPeriods <- OptionT.liftF(Future.fromTry(answersToUpdate.set(UpdatedAccountingPeriodsPage, updatedPeriods)))
      withTimestamp      <- OptionT.liftF(Future.fromTry(withUpdatedPeriods.set(AmendAPConfirmationTimestampPage, timestamp)))
      withStatus         <- OptionT.liftF(
                      Future.fromTry(withTimestamp.set(AmendAccountingPeriodStatusPage, AmendAccountingPeriodStatus.SuccessfullyCompleted))
                    )
      _ <- OptionT.liftF(sessionRepository.set(withStatus))
    } yield ()

    result
      .getOrElseF(Future.failed(MissingReferenceNumberError))
      .recoverWith { case e: Throwable =>
        val status = e match {
          case InternalIssueError | UnprocessableEntityError => AmendAccountingPeriodStatus.FailedInternalIssueError
          case _                                             => AmendAccountingPeriodStatus.FailException
        }
        logger.error(s"[AmendAccountingPeriodCYA] Amendment failed for $userId: ${e.getClass.getSimpleName}", e)
        setStatusOnFailure(userId, status)
      }
      .map(_ => ())
  }

  private def readSubscriptionWithRetry(
    userId:       String,
    plrReference: String,
    attempt:      Int = 0
  )(using hc: HeaderCarrier): Future[Seq[AccountingPeriodV2]] =
    subscriptionService
      .readSubscriptionV2AndSave(userId, plrReference)
      .map(_.accountingPeriods.getOrElse(Seq.empty))
      .recoverWith {
        case UnprocessableEntityError | RetryableGatewayError if attempt < appConfig.amendAPReadRetryMaxAttempts =>
          logger.warn(
            s"[AmendAccountingPeriodCYA] readSubscriptionV2 retry ${attempt + 1}/${appConfig.amendAPReadRetryMaxAttempts} for $userId"
          )
          Future(blocking(Thread.sleep(appConfig.amendAPReadRetryDelaySeconds * 1000L)))
            .flatMap(_ => readSubscriptionWithRetry(userId, plrReference, attempt + 1))
      }

  private def setStatusOnFailure(userId: String, status: AmendAccountingPeriodStatus)(using ec: ExecutionContext): Future[Option[Unit]] =
    sessionRepository
      .get(userId)
      .flatMap { maybeUa =>
        val userAnswersToUpdate = maybeUa.getOrElse(UserAnswers(userId))
        sessionRepository.set(userAnswersToUpdate.setOrException(AmendAccountingPeriodStatusPage, status))
      }
      .map(_ => None)

  private[manageAccount] def findAffectedPeriods(
    newStart:   LocalDate,
    newEnd:     LocalDate,
    allPeriods: Seq[AccountingPeriodV2]
  ): Seq[AccountingPeriodV2] =
    allPeriods.filter(p => !p.startDate.isAfter(newEnd) && !p.endDate.isBefore(newStart))

  private def predictMicroPeriods(
    newPeriod: AccountingPeriod,
    affected:  Seq[AccountingPeriodV2]
  ): Seq[AccountingPeriod] =
    if affected.isEmpty then Seq.empty
    else {
      val earliestStart = affected.map(_.startDate).min
      val latestEnd     = affected.map(_.endDate).max
      val today         = DateTimeUtils.today

      val gapBefore: Option[AccountingPeriod] =
        if newPeriod.startDate.isAfter(earliestStart) then Some(AccountingPeriod(earliestStart, newPeriod.startDate.minusDays(1)))
        else None

      val gapAfter: Option[AccountingPeriod] =
        if newPeriod.endDate.isBefore(latestEnd) then Some(AccountingPeriod(newPeriod.endDate.plusDays(1), latestEnd))
        else None

      val openEnded: Seq[AccountingPeriod] =
        if newPeriod.endDate.isAfter(latestEnd) then generateOpenEndedPeriods(newPeriod.endDate.plusDays(1), today)
        else Seq.empty

      gapBefore.toSeq ++ gapAfter.toSeq ++ openEnded
    }

  private def generateOpenEndedPeriods(from: LocalDate, until: LocalDate): Seq[AccountingPeriod] =
    LazyList
      .unfold(from) { cur =>
        Option.when(!cur.isAfter(until)) {
          val periodEnd = cur.plusMonths(12).minusDays(1)
          (AccountingPeriod(cur, periodEnd), periodEnd.plusDays(1))
        }
      }
      .toSeq
}
