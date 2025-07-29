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
import cats.implicits._
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models._
import models.obligationsandsubmissions.ObligationStatus
import models.obligationsandsubmissions.ObligationType.{GIR, UKTR}
import models.obligationsandsubmissions.{AccountingPeriodDetails, ObligationsAndSubmissionsSuccess}
import models.requests.OptionalDataRequest
import models.subscription.{ReadSubscriptionRequestParameters, SubscriptionData}
import models.{InternalIssueError, UserAnswers}
import pages._
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.SessionRepository
import services.{ObligationsAndSubmissionsService, ReferenceNumberService, SubscriptionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
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
        subscriptionData <- OptionT.liftF(
                              subscriptionService
                                .readAndCacheSubscription(ReadSubscriptionRequestParameters(request.userId, referenceNumber))
                                .recover {
                                  case InternalIssueError =>
                                    logger.info(s"DashboardController - subscription is in progress for PLR reference: $referenceNumber")
                                    throw new RuntimeException("REGISTRATION_IN_PROGRESS:" + referenceNumber)
                                  case other => throw other
                                }
                            )
        result <- OptionT.liftF(displayHomepage(subscriptionData, referenceNumber))
      } yield result)
        .recover {
          case ex: RuntimeException if ex.getMessage.startsWith("REGISTRATION_IN_PROGRESS:") =>
            val plrRef = ex.getMessage.drop("REGISTRATION_IN_PROGRESS:".length)
            Redirect(controllers.routes.RegistrationInProgressController.onPageLoad(plrRef))
          case InternalIssueError =>
            logger.error(
              "DashboardController - read subscription failed as no valid Json was returned from the controller"
            )
            Redirect(routes.ViewAmendSubscriptionFailedController.onPageLoad)
          case _ =>
            logger.error("DashboardController - read subscription failed as no valid Json was returned from the controller")
            Redirect(controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad)
        }
        .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

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
                if (subscriptionData.accountStatus.exists(_.inactive)) btnBannerDate(response) else None,
                getDueOrOverdueReturnsStatus(response) match {
                  case None => None
                  case Some(value) =>
                    value match {
                      case Due        => Some("Due")
                      case Overdue    => Some("Overdue")
                      case Incomplete => Some("Incomplete")
                    }
                },
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

  private def btnBannerDate(response: ObligationsAndSubmissionsSuccess): Option[LocalDate] = {
    val accountingPeriods = response.accountingPeriodDetails

    if (
      accountingPeriods.head.obligations
        .find(_.obligationType == UKTR)
        .get
        .submissions
        .nonEmpty
    ) {
      Some(accountingPeriods.head.endDate)
    } else {
      accountingPeriods.find(_.obligations.head.submissions.nonEmpty).map(_.endDate)
    }
  }

  def getDueOrOverdueReturnsStatus(obligationsAndSubmissions: ObligationsAndSubmissionsSuccess): Option[DueAndOverdueReturnBannerScenario] = {

    def periodStatus(period: AccountingPeriodDetails): Option[DueAndOverdueReturnBannerScenario] = {
      val uktrObligation = period.obligations.find(_.obligationType == UKTR)
      val girObligation  = period.obligations.find(_.obligationType == GIR)
      val dueDatePassed  = period.dueDate.isBefore(LocalDate.now())

      (uktrObligation, girObligation) match {
        case (Some(uktr), Some(gir)) =>
          (uktr.status, gir.status, dueDatePassed) match {
            case (ObligationStatus.Open, ObligationStatus.Open, false)      => Some(Due)
            case (ObligationStatus.Open, ObligationStatus.Fulfilled, false) => Some(Due)
            case (ObligationStatus.Open, ObligationStatus.Open, true)       => Some(Overdue)
            case (ObligationStatus.Open, ObligationStatus.Fulfilled, true)  => Some(Incomplete)
            case (ObligationStatus.Fulfilled, ObligationStatus.Open, true)  => Some(Incomplete)
            case _                                                          => None
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
        case _ => None
      }
    }
    obligationsAndSubmissions.accountingPeriodDetails
      .flatMap(periodStatus)
      .maxOption

  }
}
