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
import models.*
import models.requests.OptionalDataRequest
import models.subscription.AccountStatus.ActiveAccount
import models.subscription.{AccountStatus, ReadSubscriptionRequestParameters, SubscriptionData}
import pages.*
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.*
import repositories.SessionRepository
import services.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Constants.SubmissionAccountingPeriods
import utils.DateTimeUtils.*
import views.html.HomepageView

import java.time.{Clock, LocalDate}
import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class HomepageController @Inject() (
  val userAnswersConnectors:              UserAnswersConnectors,
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getData:                                DataRetrievalAction,
  val subscriptionService:                SubscriptionService,
  val controllerComponents:               MessagesControllerComponents,
  homepageView:                           HomepageView,
  referenceNumberService:                 ReferenceNumberService,
  sessionRepository:                      SessionRepository,
  osService:                              ObligationsAndSubmissionsService,
  financialDataService:                   FinancialDataService,
  homepageBannerService:                  HomepageBannerService
)(using
  ec:        ExecutionContext,
  appConfig: FrontendAppConfig,
  clock:     Clock
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData).async { (request: OptionalDataRequest[AnyContent]) =>
      given OptionalDataRequest[AnyContent] = request
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
        updatedAnswers5 <- OptionT.liftF(Future.fromTry(updatedAnswers4.remove(ManageGroupDetailsStatusPage)))
        updatedAnswers6 <- OptionT.liftF(Future.fromTry(updatedAnswers5.remove(ManageContactDetailsStatusPage)))
        _               <- OptionT.liftF(sessionRepository.set(updatedAnswers6))
        result          <-
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

  private def displayHomepage(subscriptionData: SubscriptionData, plrReference: String)(using
    request: OptionalDataRequest[?],
    hc:      HeaderCarrier
  ): Future[Result] = {
    val accountStatus = subscriptionData.accountStatus.getOrElse(ActiveAccount)
    sessionRepository.get(request.userId).flatMap { maybeUserAnswers =>
      maybeUserAnswers.getOrElse(UserAnswers(request.userId))
      for {
        obligationsResponse <- osService.handleData(plrReference, LocalDate.now().minusYears(SubmissionAccountingPeriods), LocalDate.now())
        financialData       <-
          financialDataService.retrieveFinancialData(plrReference, LocalDate.now().minusYears(SubmissionAccountingPeriods), LocalDate.now())
      } yield {
        val hasReturnsUnderEnquiry = obligationsResponse.accountingPeriodDetails.exists(_.underEnquiry)
        val returnsStatus          = ObligationsAndSubmissionsService.getDueOrOverdueReturnsStatus(obligationsResponse)
        val paymentsStatus         = FinancialDataService.getPaymentBannerScenario(financialData)
        val notificationArea       = homepageBannerService.determineNotificationArea(returnsStatus, financialData, accountStatus)
        val btnBanner              = homepageBannerService.determineBtnBanner(accountStatus, notificationArea)
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
  }
}
