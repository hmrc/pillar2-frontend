/*
 * Copyright 2025 HM Revenue & Customs
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
import controllers.actions.{IdentifierAction, SubscriptionDataRequiredAction, SubscriptionDataRetrievalAction}
import models.longrunningsubmissions.LongRunningSubmission.ManageContactDetails
import models.requests.SubscriptionDataRequest
import models.subscription.{ManageContactDetailsStatus, SubscriptionLocalData}
import models.{InternalIssueError, UnexpectedResponse, UserAnswers}
import pages.{AgentClientPillar2ReferencePage, ManageContactDetailsStatusPage, SubAddSecondaryContactPage}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.*
import repositories.SessionRepository
import services.{ReferenceNumberService, SubscriptionService}
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers.manageAccount.*
import viewmodels.govuk.summarylist.*
import views.html.subscriptionview.manageAccount.ManageContactCheckYourAnswersView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class ManageContactCheckYourAnswersController @Inject() (
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getData:                                SubscriptionDataRetrievalAction,
  requireData:                            SubscriptionDataRequiredAction,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   ManageContactCheckYourAnswersView,
  countryOptions:                         CountryOptions,
  sessionRepository:                      SessionRepository,
  subscriptionService:                    SubscriptionService,
  referenceNumberService:                 ReferenceNumberService
)(using ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async { request =>
    given SubscriptionDataRequest[AnyContent] = request
    sessionRepository.get(request.userId).flatMap {
      case None          => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      case Some(answers) =>
        answers.get(ManageContactDetailsStatusPage) match {
          case Some(ManageContactDetailsStatus.InProgress) =>
            Future.successful(
              Redirect(controllers.routes.WaitingRoomController.onPageLoad(ManageContactDetails))
            )
          case _ =>
            val primaryContactList = SummaryListViewModel(
              rows = Seq(
                ContactNameComplianceSummary.row(),
                ContactEmailAddressSummary.row(),
                ContactByPhoneSummary.row(),
                ContactCapturePhoneDetailsSummary.row()
              ).flatten
            )

            val noSecondaryContact = SummaryListViewModel(
              rows = Seq(
                AddSecondaryContactSummary.row()
              ).flatten
            )

            val secondaryContactList = SummaryListViewModel(
              rows = Seq(
                AddSecondaryContactSummary.row(),
                SecondaryContactNameSummary.row(),
                SecondaryContactEmailSummary.row(),
                SecondaryPhonePreferenceSummary.row(),
                SecondaryPhoneSummary.row()
              ).flatten
            )

            val secondaryContactRows = request.subscriptionLocalData.get(SubAddSecondaryContactPage) match {
              case Some(true) => secondaryContactList
              case _          => noSecondaryContact
            }

            val address = SummaryListViewModel(
              rows = Seq(ContactCorrespondenceAddressSummary.row(countryOptions)).flatten
            )

            Future.successful(
              Ok(view(primaryContactList, secondaryContactRows, address, request.isAgent, request.subscriptionLocalData.organisationName))
            )
        }
    }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { request =>
    given Request[AnyContent] = request
    logger.info(s"[ManageContactCheckYourAnswers] Submission started for user ${request.userId}")
    sessionRepository.get(request.userId).flatMap { userAnswers =>
      userAnswers.flatMap(_.get(ManageContactDetailsStatusPage)) match {
        case Some(ManageContactDetailsStatus.SuccessfullyCompleted) =>
          Future.successful(Redirect(controllers.routes.WaitingRoomController.onPageLoad(ManageContactDetails)))
        case _ =>
          for {
            userAnswers <- sessionRepository.get(request.userId)
            updatedAnswers = userAnswers match {
                               case Some(answers) => answers.setOrException(ManageContactDetailsStatusPage, ManageContactDetailsStatus.InProgress)
                               case None          =>
                                 UserAnswers(request.userId).setOrException(ManageContactDetailsStatusPage, ManageContactDetailsStatus.InProgress)
                             }
            _ <- sessionRepository.set(updatedAnswers)
          } yield {
            updateSubscriptionInBackground(request.userId, request.subscriptionLocalData, request.enrolments)
            Redirect(controllers.routes.WaitingRoomController.onPageLoad(ManageContactDetails))
          }
      }
    }
  }

  private def updateSubscriptionInBackground(userId: String, subscriptionData: SubscriptionLocalData, enrolments: Set[Enrolment])(using
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
                                    answers.setOrException(ManageContactDetailsStatusPage, ManageContactDetailsStatus.SuccessfullyCompleted)
                                  case None =>
                                    UserAnswers(userId)
                                      .setOrException(ManageContactDetailsStatusPage, ManageContactDetailsStatus.SuccessfullyCompleted)
                                }
      _ <- OptionT.liftF(sessionRepository.set(updatedAnswersOnSuccess))
    } yield ()

    result.value
      .recoverWith {
        case InternalIssueError =>
          logger.error(s"[ManageContactCheckYourAnswers] Subscription update failed for $userId due to InternalIssueError")
          setStatusOnFailure(userId, ManageContactDetailsStatus.FailedInternalIssueError)
        case UnexpectedResponse =>
          logger.error(s"[ManageContactCheckYourAnswers] Subscription update failed for $userId due to UnexpectedResponse")
          setStatusOnFailure(userId, ManageContactDetailsStatus.FailException)
        case e: Exception =>
          logger.error(s"[ManageContactCheckYourAnswers] Subscription update failed for $userId due to generic Exception: ${e.getMessage}", e)
          setStatusOnFailure(userId, ManageContactDetailsStatus.FailException)
      }
      .map(_ => ())
  }

  private def setStatusOnFailure(userId: String, status: ManageContactDetailsStatus)(using ec: ExecutionContext): Future[Option[Unit]] =
    sessionRepository
      .get(userId)
      .flatMap { maybeUa =>
        val userAnswersToUpdate = maybeUa.getOrElse(UserAnswers(userId))
        sessionRepository.set(userAnswersToUpdate.setOrException(ManageContactDetailsStatusPage, status))
      }
      .map(_ => None)
}
