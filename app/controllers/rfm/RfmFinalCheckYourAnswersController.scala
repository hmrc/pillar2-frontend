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

import javax.inject.Inject
import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, RfmIdentifierAction}
import models.UserAnswers
import connectors.UserAnswersConnectors
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import utils.RowStatus
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.rfm.RfmFinalCheckYourAnswersView

import scala.concurrent.ExecutionContext

class RfmFinalCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  getData:                  DataRetrievalAction,
  rfmIdentify:              RfmIdentifierAction,
  requireData:              DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  userAnswersConnectors:    UserAnswersConnectors,
  view:                     RfmFinalCheckYourAnswersView,
  countryOptions:           CountryOptions
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (rfmIdentify andThen getData andThen requireData) { implicit request =>
    implicit val userAnswers: UserAnswers = request.userAnswers

    val rfmEnabled = appConfig.rfmAccessEnabled
    if (rfmEnabled) {
      Ok(view(rfmCorporatePositionSummaryList))
    } else {
      Redirect(controllers.routes.UnderConstructionController.onPageLoad)
    }
  }

  private def rfmCorporatePositionSummaryList(implicit messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        RfmCorporatePositionSummary.row(userAnswers),
        RfmNameRegistrationSummary.row(userAnswers),
        RfmRegisteredAddressSummary.row(userAnswers, countryOptions),
        EntityTypeIncorporatedCompanyNameRfmSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyRegRfmSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyUtrRfmSummary.row(userAnswers),
        EntityTypePartnershipCompanyNameRfmSummary.row(userAnswers),
        EntityTypePartnershipCompanyRegRfmSummary.row(userAnswers),
        EntityTypePartnershipCompanyUtrRfmSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

}
