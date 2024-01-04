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
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.subscription.AmendSubscriptionRequestParameters
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.AmendSubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers.manageAccount._
import viewmodels.govuk.summarylist._
import views.html.subscriptionview.manageAccount.ManageGroupDetailsCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}
class ManageGroupDetailsCheckYourAnswersController @Inject() (
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  val controllerComponents:  MessagesControllerComponents,
  countryOptions:            CountryOptions,
  view:                      ManageGroupDetailsCheckYourAnswersView,
  amendSubscriptionService:  AmendSubscriptionService,
  val userAnswersConnectors: UserAnswersConnectors
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val list = SummaryListViewModel(
      rows = Seq(
        MneOrDomesticSummary.row(request.userAnswers),
        GroupAccountingPeriodSummary.row(request.userAnswers),
        GroupAccountingPeriodStartDateSummary.row(request.userAnswers),
        GroupAccountingPeriodEndDateSummary.row(request.userAnswers)
      ).flatten
    )
    Ok(view(list))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) async { implicit request =>
    amendSubscriptionService.amendSubscription(AmendSubscriptionRequestParameters(request.userId)).flatMap {
      case Right(s) =>
        userAnswersConnectors.remove(request.userId)
        logger.info(s"Redirecting to Dashboard from group details ")
        Future.successful(Redirect(controllers.routes.DashboardController.onPageLoad))
      case _ => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }

  }

}
