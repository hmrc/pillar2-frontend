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

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.{IdentifierAction, SubscriptionDataRequiredAction, SubscriptionDataRetrievalAction}
import controllers.routes
import models.subscription.AmendSubscriptionRequestParameters
import models.{BadRequestError, DuplicateSubmissionError, InternalServerError_, NotFoundError, ServiceUnavailableError, SubscriptionCreateError, UnprocessableEntityError}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AmendSubscriptionService, SubscriptionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Pillar2SessionKeys
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers.manageAccount._
import viewmodels.govuk.summarylist._
import views.html.subscriptionview.manageAccount.ManageContactCheckYourAnswersView

import scala.concurrent.ExecutionContext
class ManageContactCheckYourAnswersController @Inject() (
  identify:                 IdentifierAction,
  getData:                  SubscriptionDataRetrievalAction,
  requireData:              SubscriptionDataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view:                     ManageContactCheckYourAnswersView,
  countryOptions:           CountryOptions,
  amendSubscriptionService: AmendSubscriptionService,
  subscriptionService: SubscriptionService
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val primaryContactList = SummaryListViewModel(
      rows = Seq(
        ContactNameComplianceSummary.row(request.subscriptionLocalData),
        ContactEmailAddressSummary.row(request.subscriptionLocalData),
        ContactByTelephoneSummary.row(request.subscriptionLocalData),
        ContactCaptureTelephoneDetailsSummary.row(request.subscriptionLocalData)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

    val secondaryContactList = SummaryListViewModel(
      rows = Seq(
        AddSecondaryContactSummary.row(request.subscriptionLocalData),
        SecondaryContactNameSummary.row(request.subscriptionLocalData),
        SecondaryContactEmailSummary.row(request.subscriptionLocalData),
        SecondaryTelephonePreferenceSummary.row(request.subscriptionLocalData),
        SecondaryTelephoneSummary.row(request.subscriptionLocalData)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

    val address = SummaryListViewModel(
      rows = Seq(ContactCorrespondenceAddressSummary.row(request.subscriptionLocalData, countryOptions)).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

    if (request.subscriptionLocalData.manageContactDetailStatus) {
      Ok(view(primaryContactList, secondaryContactList, address))
    } else {
      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) async { implicit request =>
    val showErrorScreens = appConfig.showErrorScreens

    subscriptionService.amendSubscription(request.userId, ???, request.subscriptionLocalData).map {
      case Right(_) =>
        logger.info(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Redirecting to Dashboard from contact details")
        Redirect(controllers.routes.DashboardController.onPageLoad)

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
        Redirect(routes.ViewAmendSubscriptionFailedController.onPageLoad)

      case _ =>
        logger.error(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - An error occurred during amend subscription processing")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

}
