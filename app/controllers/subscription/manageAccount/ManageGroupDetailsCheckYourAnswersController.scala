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
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes
import models.subscription.AmendSubscriptionRequestParameters
import models.{BadRequestError, DuplicateSubmissionError, InternalServerError_, MneOrDomestic, NotFoundError, ServiceUnavailableError, SubscriptionCreateError, UnprocessableEntityError}
import pages.{SubAccountingPeriodPage, SubMneOrDomesticPage}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AmendSubscriptionService, ReadSubscriptionService, ReferenceNumberService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.Pillar2SessionKeys
import viewmodels.checkAnswers.manageAccount._
import viewmodels.govuk.summarylist._
import views.html.subscriptionview.manageAccount.ManageGroupDetailsCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}
class ManageGroupDetailsCheckYourAnswersController @Inject() (
  identify:                    IdentifierAction,
  getData:                     DataRetrievalAction,
  requireData:                 DataRequiredAction,
  val controllerComponents:    MessagesControllerComponents,
  val readSubscriptionService: ReadSubscriptionService,
  referenceNumberService:      ReferenceNumberService,
  view:                        ManageGroupDetailsCheckYourAnswersView,
  amendSubscriptionService:    AmendSubscriptionService,
  val userAnswersConnectors:   UserAnswersConnectors
)(implicit ec:                 ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) async { implicit request =>
    (for {
      plrReference <- OptionT.fromOption[Future](referenceNumberService.get(None, request.enrolments))
      subData      <- OptionT.liftF(readSubscriptionService.readSubscription(plrReference))
    } yield {
      val booleanToModel   = if (subData.upeDetails.domesticOnly) MneOrDomestic.Uk else MneOrDomestic.UkAndOther
      val accountingPeriod = request.userAnswers.get(SubAccountingPeriodPage).getOrElse(subData.accountingPeriod)
      val list = SummaryListViewModel(
        rows = Seq(
          MneOrDomesticSummary.row(request.userAnswers.get(SubMneOrDomesticPage) getOrElse booleanToModel),
          GroupAccountingPeriodSummary.row(),
          GroupAccountingPeriodStartDateSummary.row(accountingPeriod),
          GroupAccountingPeriodEndDateSummary.row(accountingPeriod)
        )
      )
      Ok(view(list))
    }).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }
  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    val showErrorScreens = appConfig.showErrorScreens

    amendSubscriptionService.amendSubscription(AmendSubscriptionRequestParameters(request.userId)).flatMap {
      case Right(s) =>
        userAnswersConnectors.remove(request.userId).map { _ =>
          logger.info(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Redirecting to Dashboard from group details ")
          Redirect(controllers.routes.DashboardController.onPageLoad)
        }
      case Left(error) if showErrorScreens =>
        val errorMessage = error match {
          case BadRequestError =>
            s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Bad request error."
          case NotFoundError =>
            s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - No subscription data found."
          case DuplicateSubmissionError =>
            s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Duplicate submission detected."
          case UnprocessableEntityError =>
            s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Unprocessable entity error."
          case InternalServerError_ =>
            s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Internal server error."
          case ServiceUnavailableError =>
            s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Service Unavailable error."
          case SubscriptionCreateError =>
            s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Subscription creation error."
        }
        logger.error(errorMessage)
        Future.successful(Redirect(routes.ViewAmendSubscriptionFailedController.onPageLoad))

      case _ => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }

  }

}
