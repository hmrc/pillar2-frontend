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

import cats.data.OptionT
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models._
import models.obligationsandsubmissions.ObligationType.{GIR, UKTR}
import models.obligationsandsubmissions.SubmissionType.UKTR_CREATE
import models.obligationsandsubmissions._
import models.requests.OptionalDataRequest
import models.subscription.{ReadSubscriptionRequestParameters, SubscriptionData}
import pages._
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.SessionRepository
import services.{ObligationsAndSubmissionsService, ReferenceNumberService, SubscriptionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Constants.RECEIVED_PERIOD_IN_DAYS
import views.html.{DashboardView, HomepageView}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class DashboardController @Inject() (
  val userAnswersConnectors:              UserAnswersConnectors,
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getData:                                DataRetrievalAction,
  val subscriptionService:                SubscriptionService,
  val controllerComponents:               MessagesControllerComponents,
  dashboardView:                          DashboardView,
  homepageView:                           HomepageView,
  referenceNumberService:                 ReferenceNumberService,
  sessionRepository:                      SessionRepository,
  osService:                              ObligationsAndSubmissionsService
)(implicit ec:                            ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData).async { implicit request: OptionalDataRequest[AnyContent] =>
      (for {
        mayBeUserAnswer <- OptionT.liftF(sessionRepository.get(request.userId))
        userAnswers = mayBeUserAnswer.getOrElse(UserAnswers(request.userId))
        referenceNumber <- OptionT
                             .fromOption[Future](userAnswers.get(AgentClientPillar2ReferencePage))
                             .orElse(OptionT.fromOption[Future](referenceNumberService.get(Some(userAnswers), request.enrolments)))
        updatedAnswers  <- OptionT.liftF(Future.fromTry(userAnswers.set(PlrReferencePage, referenceNumber)))
        updatedAnswers1 <- OptionT.liftF(Future.fromTry(updatedAnswers.set(RedirectToASAHome, false)))
        updatedAnswers2 <- OptionT.liftF(Future.fromTry(updatedAnswers1.remove(RepaymentsStatusPage)))
        updatedAnswers3 <- OptionT.liftF(Future.fromTry(updatedAnswers2.remove(RepaymentCompletionStatus)))
        updatedAnswers4 <- OptionT.liftF(Future.fromTry(updatedAnswers3.remove(RfmStatusPage)))
        updatedAnswers5 <- OptionT.liftF(Future.fromTry(updatedAnswers4.remove(RepaymentsWaitingRoomVisited)))
        _               <- OptionT.liftF(sessionRepository.set(updatedAnswers5))
        result <-
          OptionT.liftF {
            subscriptionService
              .maybeReadSubscription(referenceNumber)
              .flatMap {
                case Some(_) =>
                  subscriptionService
                    .cacheSubscription(ReadSubscriptionRequestParameters(request.userId, referenceNumber))
                    .flatMap(displayHomepage(_, referenceNumber))
                case None =>
                  Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
              }
              .recover { case UnprocessableEntityError =>
                Redirect(controllers.routes.RegistrationInProgressController.onPageLoad(referenceNumber))
              }
          }
      } yield result).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }

  private def displayHomepage(subscriptionData: SubscriptionData, plrReference: String)(implicit
    request:                                    OptionalDataRequest[_],
    hc:                                         HeaderCarrier
  ): Future[Result] =
    if (appConfig.newHomepageEnabled) {
      val sevenAPs = 7 * ChronoUnit.DAYS.between(subscriptionData.accountingPeriod.startDate, subscriptionData.accountingPeriod.endDate)
      sessionRepository.get(request.userId).flatMap { maybeUserAnswers =>
        maybeUserAnswers.getOrElse(UserAnswers(request.userId))
        osService
          .handleData(plrReference, LocalDate.now().minusDays(sevenAPs), LocalDate.now())
          .map { response =>
            Ok(
              homepageView(
                subscriptionData.upeDetails.organisationName,
                subscriptionData.upeDetails.registrationDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
                subscriptionData.accountStatus.exists(_.inactive),
                getDueOrOverdueReturnsStatus(response).map(_.toString),
                plrReference,
                isAgent = request.isAgent
              )
            )
          }
      }
    } else {
      Future.successful(
        Ok(
          dashboardView(
            subscriptionData.upeDetails.organisationName,
            subscriptionData.upeDetails.registrationDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
            plrReference,
            inactiveStatus = subscriptionData.accountStatus.exists(_.inactive),
            agentView = request.isAgent
          )
        )
      )
    }

  def getDueOrOverdueReturnsStatus(obligationsAndSubmissions: ObligationsAndSubmissionsSuccess): Option[DueAndOverdueReturnBannerScenario] = {

    def periodStatus(period: AccountingPeriodDetails): Option[DueAndOverdueReturnBannerScenario] =
      if (period.obligations.isEmpty) {
        None
      } else {
        val uktrObligation       = period.obligations.find(_.obligationType == UKTR)
        val girObligation        = period.obligations.find(_.obligationType == GIR)
        val dueDatePassed        = period.dueDate.isBefore(LocalDate.now())
        val hasAnyOpenObligation = period.obligations.exists(_.status == ObligationStatus.Open)
        val isInReceivedPeriod = period.obligations
          .filter(_.status == ObligationStatus.Fulfilled)
          .flatMap(_.submissions)
          .filter(submission =>
            submission.submissionType == UKTR_CREATE
              || submission.submissionType == SubmissionType.GIR
          )
          .maxByOption(_.receivedDate)
          .exists { submission =>
            ChronoUnit.DAYS.between(submission.receivedDate.toLocalDate, LocalDate.now()) <= RECEIVED_PERIOD_IN_DAYS
          }

        (uktrObligation, girObligation) match {
          case (Some(uktr), Some(gir)) =>
            (uktr.status, gir.status, dueDatePassed, isInReceivedPeriod) match {
              case (ObligationStatus.Open, ObligationStatus.Open, false, _)          => Some(Due)
              case (ObligationStatus.Open, ObligationStatus.Fulfilled, false, _)     => Some(Due)
              case (ObligationStatus.Fulfilled, ObligationStatus.Open, false, _)     => Some(Due)
              case (ObligationStatus.Open, ObligationStatus.Open, true, _)           => Some(Overdue)
              case (ObligationStatus.Open, ObligationStatus.Fulfilled, true, _)      => Some(Incomplete)
              case (ObligationStatus.Fulfilled, ObligationStatus.Open, true, _)      => Some(Incomplete)
              case (ObligationStatus.Fulfilled, ObligationStatus.Fulfilled, _, true) => Some(Received)
              case _ if hasAnyOpenObligation && !dueDatePassed                       => Some(Due)
              case _ if hasAnyOpenObligation && dueDatePassed                        => Some(Overdue)
              case _                                                                 => None
            }
          case (Some(uktr), None) =>
            (uktr.status, dueDatePassed) match {
              case (ObligationStatus.Open, false) => Some(Due)
              case (ObligationStatus.Open, true)  => Some(Overdue)
              case _                              => None
            }
          case (None, Some(gir)) =>
            (gir.status, dueDatePassed) match {
              case (ObligationStatus.Open, false) => Some(Due)
              case (ObligationStatus.Open, true)  => Some(Overdue)
              case _                              => None
            }
          case _ if hasAnyOpenObligation && !dueDatePassed => Some(Due)
          case _ if hasAnyOpenObligation && dueDatePassed  => Some(Overdue)
          case _                                           => None
        }
      }

    obligationsAndSubmissions.accountingPeriodDetails
      .flatMap(periodStatus)
      .maxOption

  }
}
