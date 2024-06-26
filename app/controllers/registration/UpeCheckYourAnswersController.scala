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

package controllers.registration

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.CheckYourAnswersLogicPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.registrationview.UpeCheckYourAnswersView

class UpeCheckYourAnswersController @Inject() (
  identify:                 IdentifierAction,
  getData:                  DataRetrievalAction,
  requireData:              DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view:                     UpeCheckYourAnswersView,
  countryOptions:           CountryOptions
)(implicit appConfig:       FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val upeStatus = request.userAnswers.upeStatus
    val CheckYourAnswersLogic: Boolean = request.userAnswers.get(CheckYourAnswersLogicPage).isDefined
    if (upeStatus == RowStatus.Completed | upeStatus == RowStatus.InProgress & CheckYourAnswersLogic) {
      val list = SummaryListViewModel(
        rows = Seq(
          UpeNameRegistrationSummary.row(request.userAnswers),
          UpeRegisteredAddressSummary.row(request.userAnswers, countryOptions),
          UpeContactNameSummary.row(request.userAnswers),
          UpeContactEmailSummary.row(request.userAnswers),
          UpeTelephonePreferenceSummary.row(request.userAnswers),
          UPEContactTelephoneSummary.row(request.userAnswers)
        ).flatten
      )
      Ok(view(list))
    } else {
      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

}
