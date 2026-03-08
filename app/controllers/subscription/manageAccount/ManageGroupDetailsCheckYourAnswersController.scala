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
import pages.{AgentClientPillar2ReferencePage, ManageGroupDetailsStatusPage, SubAddSecondaryContactPage}
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
              if appConfig.amendMultipleAccountingPeriods then
                logger.info(
                  s"[ManageGroupDetailsCheckYourAnswers] amendMultipleAccountingPeriods=true, loading multi-period view for plrReference=${request.subscriptionLocalData.plrReference}"
                )
                loadMultiPeriodView(request)
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

    def singlePeriodResult: Result = {
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

    subscriptionService
      .readDisplaySubscriptionV2(plrReference)
      .flatMap {
        case None =>
          logger.warn(
            s"[ManageGroupDetailsCheckYourAnswers] Display Subscription V2 returned None for plrReference=$plrReference (e.g. 404 from backend/stub), falling back to single-period view"
          )
          Future.successful(singlePeriodResult)
        case Some(v2Response) =>
          logger.info(
            s"[ManageGroupDetailsCheckYourAnswers] Display Subscription V2 success for plrReference=$plrReference, rendering multi-period view (${v2Response.success.accountingPeriod.size} periods)"
          )
          val fullPeriods            = v2Response.success.accountingPeriod
          val amendableWithFullIndex = fullPeriods.zipWithIndex.filter(_._1.canAmend).sortBy(_._1.endDate)(Ordering[LocalDate].reverse)
          val completedWithFullIndex = fullPeriods.zipWithIndex.filter(!_._1.canAmend).sortBy(_._1.endDate)(Ordering[LocalDate].reverse)
          val locationLabel          = if request.isAgent then "mneOrDomestic.agent.checkYourAnswersLabel" else "mneOrDomestic.checkYourAnswersLabel"
          val locationValue          = if v2Response.success.upeDetails.domesticOnly then "mneOrDomestic.uk" else "mneOrDomestic.ukAndOther"
          val locationRow            = SummaryListRowViewModel(
            key = locationLabel,
            value = ValueViewModel(HtmlContent(HtmlFormat.escape(messages(locationValue)))),
            actions = Seq(
              ActionItemViewModel("site.change", controllers.subscription.manageAccount.routes.MneOrDomesticController.onPageLoad().url)
                .withVisuallyHiddenText(messages("mneOrDomestic.change.hidden"))
            )
          )
          val locationList                                           = SummaryListViewModel(rows = Seq(locationRow))
          def periodContentOnlyList(period: AccountingPeriodDisplay) = SummaryListViewModel(
            rows = Seq(
              SummaryListRowViewModel(
                key = "groupAccountingStartDatePeriod.checkYourAnswersLabel",
                value = ValueViewModel(period.startDate.toDateFormat)
              ).withCssClass("no-border-bottom"),
              SummaryListRowViewModel(key = "groupAccountingEndDatePeriod.checkYourAnswersLabel", value = ValueViewModel(period.endDate.toDateFormat))
            )
          )
          def amendableCardList(period: AccountingPeriodDisplay, fullIdx: Int) = {
            val changeUrl = controllers.subscription.manageAccount.routes.ChangeAccountingPeriodController.onPageLoad(fullIdx).url
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
          def completedCardList(period: AccountingPeriodDisplay, fullIdx: Int) = {
            val viewUrl = controllers.subscription.manageAccount.routes.ViewAccountingPeriodController.onPageLoad(fullIdx).url
            val rows    = Seq(
              SummaryListRowViewModel(
                key = "groupAccountingPeriod.amend.checkYourAnswersLabel",
                value = ValueViewModel(HtmlContent(HtmlFormat.escape(""))),
                actions = Seq(
                  ActionItemViewModel("site.view", viewUrl).withVisuallyHiddenText(messages("groupAccountingPeriod.view.hidden"))
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
          val (previousWithIndex, microWithIndex) = completedWithFullIndex.partition(_._1.canAmendStartDate)
          val currentPeriodChangeUrl              = amendableWithFullIndex.headOption.map { case (_, idx) =>
            controllers.subscription.manageAccount.routes.ChangeAccountingPeriodController.onPageLoad(idx).url
          }
          val periodCardLists             = amendableWithFullIndex.headOption.toSeq.map { case (period, _) => periodContentOnlyList(period) }
          val previousPeriodCardListsFull = amendableWithFullIndex.drop(1).map { case (period, fullIdx) => amendableCardList(period, fullIdx) } ++
            previousWithIndex.map { case (period, idx) => completedCardList(period, idx) }
          val (previousPeriodTitleChangeUrl, previousPeriodTitleIsView) =
            if previousPeriodCardListsFull.size == 1 then
              if amendableWithFullIndex.drop(1).nonEmpty then
                (
                  Some(
                    controllers.subscription.manageAccount.routes.ChangeAccountingPeriodController
                      .onPageLoad(amendableWithFullIndex.drop(1).head._2)
                      .url
                  ),
                  false
                )
              else
                (
                  Some(controllers.subscription.manageAccount.routes.ViewAccountingPeriodController.onPageLoad(previousWithIndex.head._2).url),
                  true
                )
            else (None, false)
          val previousPeriodCardLists =
            if previousPeriodCardListsFull.size == 1 then
              val period = if amendableWithFullIndex.drop(1).nonEmpty then amendableWithFullIndex.drop(1).head._1 else previousWithIndex.head._1
              Seq(periodContentOnlyList(period))
            else previousPeriodCardListsFull
          val microPeriodCardLists     = microWithIndex.map { case (period, idx) => completedCardList(period, idx) }
          val completedPeriodCardLists = completedWithFullIndex.map { case (period, fullIdx) => completedCardList(period, fullIdx) }
          val authorisedOfficialList   = SummaryListViewModel(
            rows = Seq(
              ContactNameComplianceSummary.row(),
              ContactEmailAddressSummary.row(),
              ContactByPhoneSummary.row(),
              ContactCapturePhoneDetailsSummary.row()
            ).flatten
          )
          val noSecondaryContact = SummaryListViewModel(
            rows = Seq(AddSecondaryContactSummary.row()).flatten
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
          val contactDetailsList = request.subscriptionLocalData.get(SubAddSecondaryContactPage) match {
            case Some(true) => secondaryContactList
            case _          => noSecondaryContact
          }
          val agentGroupDetailsList = if request.isAgent then {
            val upe  = v2Response.success.upeDetails
            val addr = if upe.domesticOnly then messages("mneOrDomestic.uk") else messages("mneOrDomestic.ukAndOther")
            Some(
              SummaryListViewModel(
                rows = Seq(
                  SummaryListRowViewModel(
                    key = "manageFurtherGroupDetails.reportingCompany",
                    value = ValueViewModel(upe.organisationName),
                    actions = Seq(
                      ActionItemViewModel("site.change", controllers.subscription.manageAccount.routes.MneOrDomesticController.onPageLoad().url)
                        .withVisuallyHiddenText(messages("manageFurtherGroupDetails.reportingCompany"))
                    )
                  ),
                  SummaryListRowViewModel(
                    key = "manageFurtherGroupDetails.groupRegistrationDate",
                    value = ValueViewModel(upe.registrationDate.toDateFormat)
                  ),
                  SummaryListRowViewModel(
                    key = "manageFurtherGroupDetails.ukBasedBusinessAddresses",
                    value = ValueViewModel(HtmlContent(HtmlFormat.escape(addr)))
                  )
                )
              )
            )
          } else None
          val changeNextPeriodUrl =
            if request.isAgent && amendableWithFullIndex.nonEmpty then
              Some(controllers.subscription.manageAccount.routes.ChangeAccountingPeriodController.onPageLoad(amendableWithFullIndex.head._2).url)
            else None
          val changeGroupDetailsUrl =
            if request.isAgent then Some(controllers.subscription.manageAccount.routes.MneOrDomesticController.onPageLoad().url) else None
          val showViewAllPeriodsLink = completedPeriodCardLists.nonEmpty
          val sessionWithPeriods = request.session + (ManageAccountV2SessionKeys.DisplaySubscriptionV2Periods -> Json.toJson(fullPeriods).toString)
          Future.successful(
            Ok(
              multiPeriodView(
                locationList,
                periodCardLists,
                request.isAgent,
                request.subscriptionLocalData.organisationName,
                plrReference,
                emptyState = amendableWithFullIndex.isEmpty,
                authorisedOfficialList,
                contactDetailsList,
                completedPeriodCardLists,
                previousPeriodCardLists,
                microPeriodCardLists,
                agentGroupDetailsList,
                changeNextPeriodUrl,
                showViewAllPeriodsLink,
                changeGroupDetailsUrl,
                currentPeriodChangeUrl = currentPeriodChangeUrl,
                previousPeriodTitleChangeUrl = previousPeriodTitleChangeUrl,
                previousPeriodTitleIsView = previousPeriodTitleIsView
              )
            ).withSession(sessionWithPeriods)
          )
      }
      .recoverWith { case ex =>
        logger.warn(
          s"[ManageGroupDetailsCheckYourAnswers] Display Subscription V2 failed for plrReference=$plrReference, falling back to single-period view",
          ex
        )
        Future.successful(singlePeriodResult)
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
