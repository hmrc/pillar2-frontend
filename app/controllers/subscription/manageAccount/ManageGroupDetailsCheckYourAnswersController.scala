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
import models.longrunningsubmissions.LongRunningSubmission.ManageGroupDetails
import models.requests.SubscriptionDataRequest
import models.subscription.ManageGroupDetailsStatus.*
import models.subscription.{AccountingPeriodDisplay, DisplaySubscriptionV2Response, ManageGroupDetailsStatus, SubscriptionLocalData}
import models.{InternalIssueError, MissingReferenceNumberError, UserAnswers}
import pages.{AgentClientPillar2ReferencePage, ManageGroupDetailsStatusPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json.Json
import play.api.mvc.*
import repositories.SessionRepository
import services.{ReferenceNumberService, SubscriptionService}
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeUtils.*
import viewmodels.checkAnswers.manageAccount.*
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.given
import views.html.subscriptionview.manageAccount.{ManageGroupDetailsCheckYourAnswersView, ManageGroupDetailsMultiPeriodView}

import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

import java.time.LocalDate
import javax.inject.Named
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

class ManageGroupDetailsCheckYourAnswersController @Inject() (
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getData:                                SubscriptionDataRetrievalAction,
  requireData:                            SubscriptionDataRequiredAction,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   ManageGroupDetailsCheckYourAnswersView,
  multiPeriodView:                        ManageGroupDetailsMultiPeriodView,
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
      implicit val msgs: Messages = controllerComponents.messagesApi.preferred(request)
      given SubscriptionDataRequest[AnyContent] = request
      sessionRepository.get(request.userId).flatMap {
        case None          => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        case Some(answers) =>
          answers.get(ManageGroupDetailsStatusPage) match {
            case Some(InProgress) =>
              Future.successful(Redirect(controllers.routes.WaitingRoomController.onPageLoad(ManageGroupDetails)))
            case _ =>
              if appConfig.amendMultipleAccountingPeriods then loadMultiPeriodView(request)
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

  private def loadMultiPeriodView(request: SubscriptionDataRequest[AnyContent])(implicit messages: Messages, ec: ExecutionContext): Future[Result] = {
    given SubscriptionDataRequest[AnyContent] = request
    val plrReference                          = request.subscriptionLocalData.plrReference
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    subscriptionService
      .readDisplaySubscriptionV2(plrReference)
      .flatMap {
        case None =>
          logger.warn(
            s"[ManageGroupDetailsCheckYourAnswers] Display Subscription V2 returned None for plrReference=$plrReference (e.g. 404 from backend/stub)"
          )
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        case Some(v2Response) =>
          val fullPeriods   = v2Response.success.accountingPeriod
          val amendable     = fullPeriods.filter(_.canAmend)
          val sortedPeriods = amendable.sortBy(_.endDate)(Ordering[LocalDate].reverse)
          val locationLabel = if request.isAgent then "mneOrDomestic.agent.checkYourAnswersLabel" else "mneOrDomestic.checkYourAnswersLabel"
          val locationValue = if v2Response.success.upeDetails.domesticOnly then "mneOrDomestic.uk" else "mneOrDomestic.ukAndOther"
          val locationRow   = SummaryListRowViewModel(
            key = locationLabel,
            value = ValueViewModel(HtmlContent(HtmlFormat.escape(messages(locationValue)))),
            actions = Seq(
              ActionItemViewModel("site.change", controllers.subscription.manageAccount.routes.MneOrDomesticController.onPageLoad().url)
                .withVisuallyHiddenText(messages("mneOrDomestic.change.hidden"))
            )
          )
          val locationList    = SummaryListViewModel(rows = Seq(locationRow))
          val periodCardLists = sortedPeriods.zipWithIndex.map { case (period, idx) =>
            val changeUrl = controllers.subscription.manageAccount.routes.ChangeAccountingPeriodController.onPageLoad(idx).url
            val rows      = Seq(
              SummaryListRowViewModel(
                key = "groupAccountingPeriod.amend.checkYourAnswersLabel",
                value = ValueViewModel(HtmlContent(HtmlFormat.escape(""))),
                actions = Seq(
                  ActionItemViewModel("site.change", changeUrl).withVisuallyHiddenText(messages("groupAccountingPeriod.change.hidden"))
                )
              ).withCssClass("no-border-bottom"),
              SummaryListRowViewModel(
                key = "groupAccountingStartDatePeriod.checkYourAnswersLabel",
                value = ValueViewModel(period.startDate.toDateFormat)
              ).withCssClass("no-border-bottom"),
              SummaryListRowViewModel(key = "groupAccountingEndDatePeriod.checkYourAnswersLabel", value = ValueViewModel(period.endDate.toDateFormat))
            )
            SummaryListViewModel(rows = rows)
          }
          val sessionWithPeriods = request.session + (ManageAccountV2SessionKeys.DisplaySubscriptionV2Periods -> Json.toJson(fullPeriods).toString)
          Future.successful(
            Ok(
              multiPeriodView(
                locationList,
                periodCardLists,
                request.isAgent,
                request.subscriptionLocalData.organisationName,
                plrReference,
                emptyState = sortedPeriods.isEmpty
              )
            ).withSession(sessionWithPeriods)
          )
      }
      .recoverWith { case ex =>
        logger.warn(s"[ManageGroupDetailsCheckYourAnswers] Display Subscription V2 failed for plrReference=$plrReference", ex)
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
