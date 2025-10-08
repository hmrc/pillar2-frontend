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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models._
import models.subscription.SubscriptionStatus
import models.subscription.SubscriptionStatus._
import pages._
import pages.pdf.{PdfRegistrationDatePage, PdfRegistrationTimeStampPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.concurrent.Futures
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.http.{GatewayTimeoutException, HeaderCarrier, HttpException}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeUtils.{getCurrentDate, getCurrentTimeGMT}
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

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
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    implicit val userAnswers: UserAnswers = request.userAnswers
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

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    if (request.userAnswers.finalStatusCheck) {
      subscriptionService.getCompanyName(request.userAnswers) match {
        case Left(errorRedirect) => errorRedirect
        case Right(companyName) =>
          val subscriptionStatus: Future[WithName with SubscriptionStatus] =
            request.userAnswers
              .get(SubMneOrDomesticPage)
              .map { mneOrDom =>
                (for {
                  plr <- subscriptionService.createSubscription(request.userAnswers)
                  dataToSave = UserAnswers(request.userId)
                                 .setOrException(UpeNameRegistrationPage, companyName)
                                 .setOrException(SubMneOrDomesticPage, mneOrDom)
                                 .setOrException(PlrReferencePage, plr)
                                 .setOrException(PdfRegistrationDatePage, getCurrentDate)
                                 .setOrException(PdfRegistrationTimeStampPage, getCurrentTimeGMT)
                  _ <- sessionRepository.set(dataToSave)
                  _ <- userAnswersConnectors.remove(request.userId)
                } yield {
                  pollForSubscriptionData(plr, request.userId)
                    .map { _ =>
                      Redirect(controllers.routes.RegistrationConfirmationController.onPageLoad)
                    }
                    .recover { case _ =>
                      Redirect(controllers.routes.RegistrationInProgressController.onPageLoad(plr))
                    }
                  SuccessfullyCompletedSubscription
                })
                  .recover {
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
              .getOrElse(Future.successful(FailedWithNoMneOrDomesticValueFoundError))

          for {
            updatedSubscriptionStatus <- subscriptionStatus
            optionalSessionData       <- sessionRepository.get(request.userAnswers.id)
            sessionData = optionalSessionData.getOrElse(UserAnswers(request.userId))
            updatedSessionData <- Future.fromTry(sessionData.set(SubscriptionStatusPage, updatedSubscriptionStatus))
            _                  <- sessionRepository.set(updatedSessionData)
          } yield (): Unit

          Redirect(controllers.routes.RegistrationWaitingRoomController.onPageLoad())
      }
    } else {
      Redirect(controllers.subscription.routes.InprogressTaskListController.onPageLoad)
    }
  }

  private def setCheckYourAnswersLogic(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Unit] =
    for {
      updatedAnswers      <- Future.fromTry(userAnswers.set(CheckYourAnswersLogicPage, true))
      _                   <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
      optionalSessionData <- sessionRepository.get(updatedAnswers.id)
      sessionData = optionalSessionData.getOrElse(UserAnswers(updatedAnswers.id))
      updatedSessionData <- Future.fromTry(sessionData.remove(SubscriptionStatusPage))
      _                  <- sessionRepository.set(updatedSessionData)
    } yield (): Unit

  private def addressSummaryList(implicit messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(ContactCorrespondenceAddressSummary.row(userAnswers, countryOptions)).flatten
    ).withCssClass("govuk-!-margin-bottom-6")

  private def secondaryContactSummaryList(implicit messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        AddSecondaryContactSummary.row(userAnswers),
        SecondaryContactNameSummary.row(userAnswers),
        SecondaryContactEmailSummary.row(userAnswers),
        SecondaryPhonePreferenceSummary.row(userAnswers),
        SecondaryPhoneSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  private def primaryContactSummaryList(implicit messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        ContactNameComplianceSummary.row(userAnswers),
        ContactEmailAddressSummary.row(userAnswers),
        ContactByPhoneSummary.row(userAnswers),
        ContactCapturePhoneDetailsSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  private def nfmSummaryList(implicit messages: Messages, userAnswers: UserAnswers) =
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

  private def upeSummaryList(implicit messages: Messages, userAnswers: UserAnswers) =
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

  private def groupDetailSummaryList(implicit messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        MneOrDomesticSummary.row(userAnswers),
        GroupAccountingPeriodSummary.row(userAnswers),
        GroupAccountingPeriodStartDateSummary.row(userAnswers),
        GroupAccountingPeriodEndDateSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  private def pollForSubscriptionData(plrReference: String, userId: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    val maxAttempts = {
      if (appConfig.subscriptionPollingIntervalSeconds <= 0) {
        logger.error("Invalid subscriptionPollingIntervalSeconds configuration: must be greater than 0")
        throw new IllegalArgumentException("subscriptionPollingIntervalSeconds must be greater than 0")
      }
      appConfig.subscriptionPollingTimeoutSeconds / appConfig.subscriptionPollingIntervalSeconds
    }
    val delaySeconds = appConfig.subscriptionPollingIntervalSeconds

    def attemptRead(attempt: Int): Future[Unit] =
      if (attempt >= maxAttempts) {
        Future.failed(new RuntimeException("Subscription polling timeout"))
      } else {
        subscriptionService
          .readSubscription(plrReference)
          .map(_ => ())
          .recoverWith { case _ =>
            if (attempt + 1 < maxAttempts) {
              futures.delayed(delaySeconds.seconds)(attemptRead(attempt + 1))
            } else {
              Future.failed(new RuntimeException("Subscription polling timeout"))
            }
          }
      }

    attemptRead(0)
  }
}
