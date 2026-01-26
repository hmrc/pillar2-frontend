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

package services

import cats.data.{EitherT, OptionT}
import cats.syntax.either.given
import cats.syntax.functor.given
import cats.syntax.semigroupal.given
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import models.*
import models.longrunningsubmissions.LongRunningSubmission.Registration
import models.subscription.SubscriptionStatus
import models.subscription.SubscriptionStatus.*
import pages.*
import play.api.Logging
import play.api.libs.concurrent.Futures
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import repositories.SessionRepository
import uk.gov.hmrc.http.{GatewayTimeoutException, HeaderCarrier, HttpException}
import utils.DateTimeUtils.*

import java.time.{Clock, LocalDate, ZonedDateTime}
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}
import scala.util.chaining.*

@Singleton
class SubscriptionOrchestrationService @Inject() (
  subscriptionService:   SubscriptionService,
  userAnswersConnectors: UserAnswersConnectors,
  sessionRepository:     SessionRepository,
  futures:               Futures
)(using ec: ExecutionContext, appConfig: FrontendAppConfig, clock: Clock)
    extends Logging {

  def onSubmit(userId: String, userAnswers: UserAnswers)(using hc: HeaderCarrier): Future[Result] =
    if userAnswers.finalStatusCheck then {
      subscriptionService.getCompanyName(userAnswers) match {
        case Left(errorRedirect) => Future.successful(errorRedirect)
        case Right(companyName)  =>
          // The waiting room needs SubscriptionStatusPage populated to load. We also set everything else we can here.
          val preRedirectSteps = EitherT
            .pure[Future, SubscriptionStatus](userAnswers)
            .flatMap(ua => EitherT.fromOption(ua.get(SubMneOrDomesticPage), ifNone = FailedWithNoMneOrDomesticValueFoundError))
            .semiflatMap { mneOrDom =>
              val sessionToPersist = UserAnswers(userId)
                .setOrException(UpeNameRegistrationPage, companyName)
                .setOrException(SubMneOrDomesticPage, mneOrDom)
                .setOrException(SubscriptionStatusPage, RegistrationInProgress)
              sessionRepository.set(sessionToPersist).as(sessionToPersist)
            }

          // We "discard" the actual submission future, running this purely for its side effects of updating the session.
          preRedirectSteps
            .product(EitherT.liftF(subscriptionService.createSubscription(userAnswers)))
            .semiflatMap { case (userAnswersFromSession, plr) =>
              val answersToSet = userAnswersFromSession
                .setOrException(PlrReferencePage, plr)
                .setOrException(RegistrationConfirmationPageDate, LocalDate.now(clock).toDateFormat)
                .setOrException(RegistrationConfirmationPageTimestamp, ZonedDateTime.now(clock).toTimeGmtFormat)
              sessionRepository.set(answersToSet).as((answersToSet, plr))
            }
            .semiflatTap(_ => userAnswersConnectors.remove(userId))
            .value
            .recover(onSubmitErrorHandling.andThen(_.asLeft))
            .pipe(EitherT.apply)
            .foldF(
              failureSubscriptionStatus =>
                OptionT(sessionRepository.get(userId))
                  .getOrElse(UserAnswers(userId))
                  .flatMap { preFailureAnswers =>
                    sessionRepository.set(preFailureAnswers.setOrException(SubscriptionStatusPage, failureSubscriptionStatus))
                  },
              (persistedAnswers, plr) => {
                checkUntilSubscriptionResolution(plr = plr, userId = userId)
                // We want to redirect away from waiting room as soon as the submission succeeded, even if we can't read it back yet.
                sessionRepository.set(persistedAnswers.setOrException(SubscriptionStatusPage, SuccessfullyCompletedSubscription))
              }
            )

          // Send user to waiting room even before submission has started.
          preRedirectSteps.value.as(Redirect(controllers.routes.WaitingRoomController.onPageLoad(Registration)))
      }
    } else {
      Future.successful(Redirect(controllers.subscription.routes.InprogressTaskListController.onPageLoad))
    }

  private def checkUntilSubscriptionResolution(plr: String, userId: String)(using hc: HeaderCarrier): Future[Unit] =
    pollForSubscriptionData(plr)
      .flatMap { _ =>
        for {
          updatedAnswers:   Option[UserAnswers] <- sessionRepository.get(userId)
          completedAnswers: UserAnswers         <- updatedAnswers.fold[Future[UserAnswers]] {
                                             val message = s"Could not find user answers for $plr after creating subscription."
                                             logger.error(message)
                                             Future.failed(new Exception(message))
                                           }(ua => Future.fromTry(ua.set(SubscriptionStatusPage, SuccessfullyCompletedSubscription)))
          _ <- sessionRepository.set(completedAnswers)
        } yield ()
      }
      .recover(logger.error(s"Encountered error in background task while checking for subscription resolution", _))

  private def pollForSubscriptionData(plrReference: String)(using hc: HeaderCarrier): Future[Unit] = {
    val maxAttempts = {
      if appConfig.subscriptionPollingIntervalSeconds <= 0 then {
        logger.error("Invalid subscriptionPollingIntervalSeconds configuration: must be greater than 0")
        throw new IllegalArgumentException("subscriptionPollingIntervalSeconds must be greater than 0")
      }
      appConfig.subscriptionPollingTimeoutSeconds / appConfig.subscriptionPollingIntervalSeconds
    }
    val delaySeconds = appConfig.subscriptionPollingIntervalSeconds

    def attemptRead(attempt: Int): Future[Unit] =
      if attempt >= maxAttempts then {
        Future.failed(new RuntimeException("Subscription polling timeout"))
      } else {
        subscriptionService
          .readSubscription(plrReference)
          .map(_ => ())
          .recoverWith { case _ =>
            if attempt + 1 < maxAttempts then {
              futures.delayed(delaySeconds.seconds)(attemptRead(attempt + 1))
            } else {
              Future.failed(new RuntimeException("Subscription polling timeout"))
            }
          }
      }

    attemptRead(0)
  }

  private val onSubmitErrorHandling: PartialFunction[Throwable, SubscriptionStatus] = {
    case _: GatewayTimeoutException =>
      logger.error("SUBSCRIPTION_FAILURE: Subscription failed due to a Gateway timeout")
      FailedWithInternalIssueError
    case InternalIssueError =>
      logger.error("SUBSCRIPTION_FAILURE: Subscription failed due to failed call to the backend")
      FailedWithInternalIssueError
    case DuplicateSubmissionError =>
      logger.error("Subscription failed due to a Duplicate Submission")
      FailedWithDuplicatedSubmission
    case UnprocessableEntityError =>
      logger.error("Subscription failed due to a business validation error")
      FailedWithUnprocessableEntity
    case DuplicateSafeIdError =>
      logger.error("Subscription failed due to a Duplicate SafeId for UPE and NFM")
      FailedWithDuplicatedSafeIdError
    case error: HttpException =>
      logger.error(s"SUBSCRIPTION_FAILURE: Subscription failed due to HTTP error ${error.responseCode}", error)
      FailedWithInternalIssueError
    case error: Exception =>
      logger.error(s"SUBSCRIPTION_FAILURE: Subscription failed due to unexpected error", error)
      FailedWithInternalIssueError
  }
}

