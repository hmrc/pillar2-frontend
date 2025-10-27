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
import models.DueAndOverdueReturnBannerScenario
import models.DueAndOverdueReturnBannerScenario._
import models._
import models.financialdata.PaymentState._
import models.financialdata.{FinancialData, PaymentState}
import models.obligationsandsubmissions._
import models.requests.OptionalDataRequest
import models.subscription.AccountStatus.{ActiveAccount, InactiveAccount}
import models.subscription.{AccountStatus, ReadSubscriptionRequestParameters, SubscriptionData}
import pages._
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.SessionRepository
import services._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Constants.SubmissionAccountingPeriods
import utils.DateTimeUtils.LocalDateOps
import views.html.{DashboardView, HomepageView}

import java.time.{Clock, LocalDate}
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
  osService:                              ObligationsAndSubmissionsService,
  financialDataService:                   FinancialDataService
)(implicit
  ec:        ExecutionContext,
  appConfig: FrontendAppConfig,
  clock:     Clock
) extends FrontendBaseController
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
  ): Future[Result] = {
    val accountStatus = subscriptionData.accountStatus.getOrElse(ActiveAccount)
    if (appConfig.newHomepageEnabled) {
      sessionRepository.get(request.userId).flatMap { maybeUserAnswers =>
        maybeUserAnswers.getOrElse(UserAnswers(request.userId))
        for {
          obligationsResponse <- osService.handleData(plrReference, LocalDate.now().minusYears(SubmissionAccountingPeriods), LocalDate.now())
          financialData <-
            financialDataService.retrieveFinancialData(plrReference, LocalDate.now().minusYears(SubmissionAccountingPeriods), LocalDate.now())
        } yield {
          val hasReturnsUnderEnquiry = obligationsResponse.accountingPeriodDetails.exists(_.underEnquiry)
          val returnsStatus          = getDueOrOverdueReturnsStatus(obligationsResponse)
          val paymentsStatus         = getPaymentBannerScenario(financialData)
          val notificationArea       = determineNotificationArea(returnsStatus, financialData, accountStatus)
          val btnBanner              = determineBtnBanner(accountStatus, notificationArea)
          Ok(
            homepageView(
              subscriptionData.upeDetails.organisationName,
              subscriptionData.upeDetails.registrationDate.toDateFormat,
              btnBanner,
              returnsStatus,
              paymentsStatus,
              notificationArea,
              plrReference,
              request.isAgent,
              hasReturnsUnderEnquiry
            )
          )
        }
      }
    } else {
      Future.successful(
        Ok(
          dashboardView(
            subscriptionData.upeDetails.organisationName,
            subscriptionData.upeDetails.registrationDate.toDateFormat,
            plrReference,
            inactiveStatus = accountStatus == InactiveAccount,
            agentView = request.isAgent
          )
        )
      )
    }
  }

  val getPaymentBannerScenario: FinancialData => Option[OutstandingPaymentBannerScenario] = {
    case PaymentState(PastDueWithInterestCharge(_) | PastDueNoInterest(_) | NotYetDue(_)) => Some(OutstandingPaymentBannerScenario.Outstanding)
    case PaymentState(Paid)                                                               => Some(OutstandingPaymentBannerScenario.Paid)
    case PaymentState(NothingDueNothingRecentlyPaid)                                      => None
  }

  def getDueOrOverdueReturnsStatus(obligationsAndSubmissions: ObligationsAndSubmissionsSuccess): Option[DueAndOverdueReturnBannerScenario] = {

    def periodStatus(period: AccountingPeriodDetails): Option[DueAndOverdueReturnBannerScenario] =
      if (period.obligations.isEmpty) {
        None
      } else {
        val uktrObligation:       Option[Obligation] = period.uktrObligation
        val girObligation:        Option[Obligation] = period.girObligation
        val dueDatePassed:        Boolean            = period.dueDatePassed
        val hasAnyOpenObligation: Boolean            = period.hasAnyOpenObligation
        val isInReceivedPeriod:   Boolean            = period.isInReceivedPeriod

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

  def determineNotificationArea(
    uktr:          Option[DueAndOverdueReturnBannerScenario],
    financialData: FinancialData,
    accountStatus: AccountStatus
  ): DynamicNotificationAreaState = (financialData, uktr, accountStatus) match {
    case (PaymentState(PaymentState.PastDueWithInterestCharge(totalAmountOutstanding)), _, _) =>
      DynamicNotificationAreaState.AccruingInterest(totalAmountOutstanding)

    case (PaymentState(PaymentState.PastDueNoInterest(totalAmountOutstanding)), _, AccountStatus.InactiveAccount) =>
      DynamicNotificationAreaState.OutstandingPaymentsWithBtn(totalAmountOutstanding)

    case (PaymentState(PaymentState.PastDueNoInterest(totalAmountOutstanding)), _, AccountStatus.ActiveAccount) =>
      DynamicNotificationAreaState.OutstandingPayments(totalAmountOutstanding)

    case (PaymentState(PaymentState.NotYetDue(totalAmountOutstanding)), _, _) =>
      DynamicNotificationAreaState.OutstandingPayments(totalAmountOutstanding)

    case (PaymentState(PaymentState.Paid | NothingDueNothingRecentlyPaid), Some(Overdue), _) =>
      DynamicNotificationAreaState.ReturnExpectedNotification.Overdue

    case (PaymentState(PaymentState.Paid | NothingDueNothingRecentlyPaid), Some(Incomplete), _) =>
      DynamicNotificationAreaState.ReturnExpectedNotification.Incomplete

    case (PaymentState(PaymentState.Paid | NothingDueNothingRecentlyPaid), Some(Due), _) =>
      DynamicNotificationAreaState.ReturnExpectedNotification.Due

    case (PaymentState(PaymentState.Paid | NothingDueNothingRecentlyPaid), Some(Received) | None, _) =>
      DynamicNotificationAreaState.NoNotification

  }

  val determineBtnBanner: (AccountStatus, DynamicNotificationAreaState) => BtnBanner = {
    case (InactiveAccount, DynamicNotificationAreaState.OutstandingPaymentsWithBtn(_)) => BtnBanner.Hide
    case (InactiveAccount, _)                                                          => BtnBanner.Show
    case (_, _)                                                                        => BtnBanner.Hide
  }
}
