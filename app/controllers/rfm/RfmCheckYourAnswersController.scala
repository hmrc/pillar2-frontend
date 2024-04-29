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
import pages.RfmGrsDataPage
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
      contactDetail    <- OptionT.fromOption[Future](request.userAnswers.getNewFilingMemberDetail)
      subscriptionData <- OptionT.liftF(subscriptionService.readSubscription(contactDetail.plrReference))
      // deregister the old filing member here
      safeId <- OptionT
                  .fromOption[Future](request.userAnswers.get(RfmGrsDataPage).map(_.companyId))
                  .orElse(OptionT.liftF(subscriptionService.registerNewFilingMember(request.userAnswers.id)))
      //grouID will come from auth and then you use that to unalocate
      amendData = subscriptionService.createAmendObjectForReplacingFilingMember(safeId, subscriptionData, contactDetail, request.userAnswers)

      _ <- OptionT.liftF(subscriptionService.amendFilingMemberDetails(request.userId, ???))
    } yield Redirect(controllers.routes.UnderConstructionController.onPageLoad))
      .recover { case InternalIssueError =>
        logger.warn("Replace filing member failed")
        Redirect(controllers.routes.UnderConstructionController.onPageLoad)
      }
      .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }

}
