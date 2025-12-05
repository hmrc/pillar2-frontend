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

import cats.data.{EitherT, OptionT}
import cats.syntax.either.given
import cats.syntax.functor.given
import cats.syntax.semigroupal.given
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.*
import models.longrunningsubmissions.LongRunningSubmission.Registration
import models.subscription.SubscriptionStatus
import models.subscription.SubscriptionStatus.*
import pages.*
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.concurrent.Futures
import play.api.libs.json.Json
import play.api.mvc.*
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.http.{GatewayTimeoutException, HeaderCarrier, HttpException}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeUtils.*
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers.*
import viewmodels.govuk.summarylist.*
import views.html.CheckYourAnswersView

import java.time.{Clock, LocalDate, ZonedDateTime}
import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}
import scala.util.chaining.*

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify:                 IdentifierAction,
  getData:                  DataRetrievalAction,
  requireData:              DataRequiredAction,
  subscriptionService:      SubscriptionService,
  val controllerComponents: MessagesControllerComponents,
  userAnswersConnectors:    UserAnswersConnectors,
  sessionRepository:        SessionRepository,
  view:                     CheckYourAnswersView,
  countryOptions:           CountryOptions,
  futures:                  Futures
)(using ec: ExecutionContext, appConfig: FrontendAppConfig, clock: Clock)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async { request =>
    given Request[AnyContent] = request
    given userAnswers: UserAnswers = request.userAnswers
    sessionRepository.get(request.userId).map { optionalUserAnswer =>
      (for {
        userAnswer <- optionalUserAnswer
        _          <- userAnswer.get(PlrReferencePage)
      } yield Redirect(controllers.routes.CannotReturnAfterSubscriptionController.onPageLoad))
        .getOrElse {
          setCheckYourAnswersLogic(userAnswers)
          Ok(
            view(upeSummaryList, nfmSummaryList, groupDetailSummaryList, primaryContactSummaryList, secondaryContactSummaryList, addressSummaryList)
          )
        }
    }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { request =>
    given Request[AnyContent] = request
    if request.userAnswers.finalStatusCheck then {
      subscriptionService.getCompanyName(request.userAnswers) match {
        case Left(errorRedirect) => Future.successful(errorRedirect)
        case Right(companyName)  =>
          // The waiting room needs SubscriptionStatusPage populated to load. We also set everything else we can here.
          val preRedirectSteps = EitherT
            .pure[Future, SubscriptionStatus](request.userAnswers)
            .flatMap(ua => EitherT.fromOption(ua.get(SubMneOrDomesticPage), ifNone = FailedWithNoMneOrDomesticValueFoundError))
            .semiflatMap { mneOrDom =>
              val sessionToPersist = UserAnswers(request.userId)
                .setOrException(UpeNameRegistrationPage, companyName)
                .setOrException(SubMneOrDomesticPage, mneOrDom)
                .setOrException(SubscriptionStatusPage, RegistrationInProgress)
              sessionRepository.set(sessionToPersist).as(sessionToPersist)
            }

          // We "discard" the actual submission future, running this purely for its side effects of updating the session.
          preRedirectSteps
            .product(EitherT.liftF(subscriptionService.createSubscription(request.userAnswers)))
            .semiflatMap { case (userAnswersFromSession, plr) =>
              val answersToSet = userAnswersFromSession
                .setOrException(PlrReferencePage, plr)
                .setOrException(RegistrationConfirmationPageDate, LocalDate.now(clock).toDateFormat)
                .setOrException(RegistrationConfirmationPageTimestamp, ZonedDateTime.now(clock).toTimeGmtFormat)
              sessionRepository.set(answersToSet).as((answersToSet, plr))
            }
            .semiflatTap(_ => userAnswersConnectors.remove(request.userId))
            .value
            .recover(onSubmitErrorHandling.andThen(_.asLeft))
            .pipe(EitherT.apply)
            .foldF(
              failureSubscriptionStatus =>
                OptionT(sessionRepository.get(request.userId))
                  .getOrElse(UserAnswers(request.userId))
                  .flatMap { preFailureAnswers =>
                    sessionRepository.set(preFailureAnswers.setOrException(SubscriptionStatusPage, failureSubscriptionStatus))
                  },
              (persistedAnswers, plr) => {
                checkUntilSubscriptionResolution(plr = plr, userId = request.userId)
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
  }

  private def setCheckYourAnswersLogic(userAnswers: UserAnswers)(using hc: HeaderCarrier): Future[Unit] =
    for {
      updatedAnswers      <- Future.fromTry(userAnswers.set(CheckYourAnswersLogicPage, true))
      _                   <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
      optionalSessionData <- sessionRepository.get(updatedAnswers.id)
      sessionData = optionalSessionData.getOrElse(UserAnswers(updatedAnswers.id))
      updatedSessionData <- Future.fromTry(sessionData.remove(SubscriptionStatusPage))
      _                  <- sessionRepository.set(updatedSessionData)
    } yield (): Unit

  private def addressSummaryList(using messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(ContactCorrespondenceAddressSummary.row(userAnswers, countryOptions)).flatten
    ).withCssClass("govuk-!-margin-bottom-6")

  private def secondaryContactSummaryList(using messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        AddSecondaryContactSummary.row(userAnswers),
        SecondaryContactNameSummary.row(userAnswers),
        SecondaryContactEmailSummary.row(userAnswers),
        SecondaryPhonePreferenceSummary.row(userAnswers),
        SecondaryPhoneSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  private def primaryContactSummaryList(using messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        ContactNameComplianceSummary.row(userAnswers),
        ContactEmailAddressSummary.row(userAnswers),
        ContactByPhoneSummary.row(userAnswers),
        ContactCapturePhoneDetailsSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  private def nfmSummaryList(using messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        NominateFilingMemberYesNoSummary.row(userAnswers),
        NfmNameRegistrationSummary.row(userAnswers),
        NfmRegisteredAddressSummary.row(userAnswers, countryOptions),
        NfmContactNameSummary.row(userAnswers),
        NfmEmailAddressSummary.row(userAnswers),
        NfmPhonePreferenceSummary.row(userAnswers),
        NfmContactPhoneSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyNameNfmSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyRegNfmSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyUtrNfmSummary.row(userAnswers),
        EntityTypePartnershipCompanyNameNfmSummary.row(userAnswers),
        EntityTypePartnershipCompanyRegNfmSummary.row(userAnswers),
        EntityTypePartnershipCompanyUtrNfmSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  private def upeSummaryList(using messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        UpeNameRegistrationSummary.row(userAnswers),
        UpeRegisteredAddressSummary.row(userAnswers, countryOptions),
        UpeContactNameSummary.row(userAnswers),
        UpeContactEmailSummary.row(userAnswers),
        UpePhonePreferenceSummary.row(userAnswers),
        UPEContactPhoneSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyNameUpeSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyRegUpeSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyUtrUpeSummary.row(userAnswers),
        EntityTypePartnershipCompanyNameUpeSummary.row(userAnswers),
        EntityTypePartnershipCompanyRegUpeSummary.row(userAnswers),
        EntityTypePartnershipCompanyUtrUpeSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  private def groupDetailSummaryList(using messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        MneOrDomesticSummary.row(userAnswers),
        GroupAccountingPeriodSummary.row(userAnswers),
        GroupAccountingPeriodStartDateSummary.row(userAnswers),
        GroupAccountingPeriodEndDateSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

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
