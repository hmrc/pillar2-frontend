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

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, FeatureFlagActionFactory, IdentifierAction}
import models.Mode
import navigation.ReplaceFilingMemberNavigator
import pages.RfmCheckYourAnswersPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.rfm.RfmCheckYourAnswersView

import javax.inject.Named
import scala.concurrent.Future

class RfmCheckYourAnswersController @Inject() (
  @Named("RfmIdentifier") identify: IdentifierAction,
  getData:                          DataRetrievalAction,
  requireData:                      DataRequiredAction,
  featureAction:                    FeatureFlagActionFactory,
  navigator:                        ReplaceFilingMemberNavigator,
  val controllerComponents:         MessagesControllerComponents,
  view:                             RfmCheckYourAnswersView,
  countryOptions:                   CountryOptions
)(implicit appConfig:               FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (featureAction.rfmAccessAction andThen identify andThen getData andThen requireData) {
    implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          RfmNameRegistrationSummary.row(request.userAnswers),
          RfmRegisteredAddressSummary.row(request.userAnswers, countryOptions)
        ).flatten
      )
      if (request.userAnswers.rfmNoIdQuestionStatus == RowStatus.Completed) {
        Ok(view(mode, list))
      } else {
        Redirect(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    Future.successful(Redirect(navigator.nextPage(RfmCheckYourAnswersPage, mode, request.userAnswers)))
  }

}
