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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, RfmIdentifierAction}
import models.{InternalIssueError, Mode}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.rfm.RfmCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class RfmCheckYourAnswersController @Inject() (
  rfmIdentify:              RfmIdentifierAction,
  getData:                  DataRetrievalAction,
  requireData:              DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  subscriptionService:      SubscriptionService,
  view:                     RfmCheckYourAnswersView,
  countryOptions:           CountryOptions
)(implicit appConfig:       FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(mode: Mode): Action[AnyContent] = (rfmIdentify andThen getData andThen requireData) { implicit request =>
    val rfmEnabled = appConfig.rfmAccessEnabled
    if (rfmEnabled) {
      val list = SummaryListViewModel(
        rows = Seq(
          RfmNameRegistrationSummary.row(request.userAnswers),
          RfmRegisteredAddressSummary.row(request.userAnswers, countryOptions)
        ).flatten
      )
      if (request.userAnswers.rfmNoIdQuestionStatus == RowStatus.Completed) {
        Ok(view(mode, list))
      } else {
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    } else {
      Redirect(controllers.routes.UnderConstructionController.onPageLoad)
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (rfmIdentify andThen getData andThen requireData).async { implicit request =>
    (for {
      newFilingMemberInformation <- OptionT.fromOption[Future](request.userAnswers.getNewFilingMemberDetail)
      subscriptionData           <- OptionT.liftF(subscriptionService.readSubscription(newFilingMemberInformation.plrReference))
      _                          <- OptionT.liftF(subscriptionService.deallocateEnrolment(newFilingMemberInformation.plrReference))
      _                          <- OptionT.liftF(subscriptionService.allocateEnrolment(request.groupId, newFilingMemberInformation.plrReference))
      amendData <- OptionT.liftF(
                     subscriptionService
                       .createAmendObjectForReplacingFilingMember(subscriptionData, newFilingMemberInformation, request.userAnswers)
                   )
      _ <- OptionT.liftF(subscriptionService.amendFilingMemberDetails(request.userAnswers.id, amendData))
    } yield Redirect(controllers.routes.UnderConstructionController.onPageLoad))
      .recover {
        case InternalIssueError =>
          logger.warn("Replace filing member failed")
          Redirect(controllers.routes.UnderConstructionController.onPageLoad)
        case _: Exception =>
          logger.warn("Replace filing member failed as expected a value for RfmUkBased page but could not find one")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
      .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }

}
