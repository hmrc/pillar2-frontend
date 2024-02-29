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

package controllers.rfm

import cats.data.OptionT
import cats.implicits.catsSyntaxApplicativeError
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, RfmIdentifierAction}
import controllers.routes
import models.{InternalIssueError, Mode}
import models.subscription.ReadSubscriptionRequestParameters
import pages.{fmDashboardPage, plrReferencePage, rfmRegistrationDatePage, rfmSecurityCheckPage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ReadSubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.rfm.SecurityQuestionsCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class SecurityQuestionsCheckYourAnswersController @Inject() (
  rfmIdentify:              RfmIdentifierAction,
  getData:                  DataRetrievalAction,
  readSubscription:         ReadSubscriptionService,
  requireData:              DataRequiredAction,
  userAnswersConnectors:    UserAnswersConnectors,
  val controllerComponents: MessagesControllerComponents,
  view:                     SecurityQuestionsCheckYourAnswersView
)(implicit appConfig:       FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (rfmIdentify andThen getData andThen requireData) { implicit request =>
    val rfmEnabled = appConfig.rfmAccessEnabled
    if (rfmEnabled) {
      val list = SummaryListViewModel(
        rows = Seq(
          RfmSecurityCheckSummary.row(request.userAnswers),
          RfmRegistrationDateSummary.row(request.userAnswers)
        ).flatten
      )
      if (request.userAnswers.securityQuestionStatus == RowStatus.Completed) {
        Ok(view(list))
      } else {
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    } else {
      Redirect(controllers.routes.UnderConstructionController.onPageLoad)
    }
  }

  def onSubmit: Action[AnyContent] = (rfmIdentify andThen getData andThen requireData).async { implicit request =>
    (for {
      inputPillar2Reference <- OptionT.fromOption[Future](request.userAnswers.get(rfmSecurityCheckPage))
      inputRegistrationDate <- OptionT.fromOption[Future](request.userAnswers.get(rfmRegistrationDatePage))
      _              <- OptionT.liftF(readSubscription.readSubscription(ReadSubscriptionRequestParameters(request.userId, inputPillar2Reference)))
      updatedAnswers <- OptionT(userAnswersConnectors.getUserAnswer(request.userId))
      dashboard      <- OptionT.fromOption[Future](updatedAnswers.get(fmDashboardPage))
    } yield
      if (dashboard.registrationDate.isEqual(inputRegistrationDate.rfmRegistrationDate)) {
        Redirect(controllers.routes.UnderConstructionController.onPageLoad)
      } else {
        Redirect(controllers.rfm.routes.SecurityQuestionsNoMatchController.onPageLoad)
      })
      .recover { case InternalIssueError =>
        Redirect(controllers.rfm.routes.SecurityQuestionsNoMatchController.onPageLoad)
      }
      .getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))

  }

}
