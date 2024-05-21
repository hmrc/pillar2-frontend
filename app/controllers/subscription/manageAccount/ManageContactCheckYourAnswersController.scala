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
import cats.implicits.catsSyntaxApplicativeError
import config.FrontendAppConfig
import controllers.actions.{AgentIdentifierAction, IdentifierAction, SubscriptionDataRequiredAction, SubscriptionDataRetrievalAction}
import controllers.routes
import models.UnexpectedResponse
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ReferenceNumberService, SubscriptionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers.manageAccount._
import viewmodels.govuk.summarylist._
import views.html.subscriptionview.manageAccount.ManageContactCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}
class ManageContactCheckYourAnswersController @Inject() (
  identify:                 IdentifierAction,
  agentIdentifierAction:    AgentIdentifierAction,
  getData:                  SubscriptionDataRetrievalAction,
  requireData:              SubscriptionDataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view:                     ManageContactCheckYourAnswersView,
  countryOptions:           CountryOptions,
  subscriptionService:      SubscriptionService,
  referenceNumberService:   ReferenceNumberService
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(clientPillar2Id: Option[String] = None): Action[AnyContent] =
    (identifierAction(clientPillar2Id, agentIdentifierAction, identify) andThen getData andThen requireData) { implicit request =>
      val primaryContactList = SummaryListViewModel(
        rows = Seq(
          ContactNameComplianceSummary.row(clientPillar2Id),
          ContactEmailAddressSummary.row(clientPillar2Id),
          ContactByTelephoneSummary.row(clientPillar2Id),
          ContactCaptureTelephoneDetailsSummary.row(clientPillar2Id)
        ).flatten
      ).withCssClass("govuk-!-margin-bottom-9")

      val secondaryContactList = SummaryListViewModel(
        rows = Seq(
          AddSecondaryContactSummary.row(clientPillar2Id),
          SecondaryContactNameSummary.row(clientPillar2Id),
          SecondaryContactEmailSummary.row(clientPillar2Id),
          SecondaryTelephonePreferenceSummary.row(clientPillar2Id),
          SecondaryTelephoneSummary.row(clientPillar2Id)
        ).flatten
      ).withCssClass("govuk-!-margin-bottom-9")

      val address = SummaryListViewModel(
        rows = Seq(ContactCorrespondenceAddressSummary.row(clientPillar2Id, countryOptions)).flatten
      ).withCssClass("govuk-!-margin-bottom-9")

      Ok(view(primaryContactList, secondaryContactList, address, clientPillar2Id))
    }

  def onSubmit(clientPillar2Id: Option[String] = None): Action[AnyContent] =
    (identifierAction(clientPillar2Id, agentIdentifierAction, identify) andThen getData andThen requireData) async { implicit request =>
      (for {
        referenceNumber <- OptionT
                             .fromOption[Future](clientPillar2Id)
                             .orElse(OptionT.fromOption[Future](referenceNumberService.get(None, enrolments = Some(request.enrolments))))
        _ <- OptionT.liftF(subscriptionService.amendContactOrGroupDetails(request.userId, referenceNumber, request.subscriptionLocalData))
      } yield Redirect(controllers.routes.DashboardController.onPageLoad()))
        .recover { case UnexpectedResponse =>
          Redirect(routes.ViewAmendSubscriptionFailedController.onPageLoad(clientPillar2Id))
        }
        .getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))
    }

}
