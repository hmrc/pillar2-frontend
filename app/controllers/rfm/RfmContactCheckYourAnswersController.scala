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
import models.{Mode, UserAnswers}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.rfm.RfmContactCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class RfmContactCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  getData:                  DataRetrievalAction,
  rfmIdentify:              RfmIdentifierAction,
  requireData:              DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view:                     RfmContactCheckYourAnswersView,
  countryOptions:           CountryOptions
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (rfmIdentify andThen getData andThen requireData) { implicit request =>
    implicit val userAnswers: UserAnswers = request.userAnswers

    val rfmEnabled = appConfig.rfmAccessEnabled
    if (rfmEnabled) {
      val rfmPrimaryContactList = SummaryListViewModel(
        rows = Seq(
          RfmPrimaryContactNameSummary.row(request.userAnswers),
          RfmPrimaryContactEmailSummary.row(request.userAnswers),
          RfmContactByTelephoneSummary.row(request.userAnswers),
          RfmCapturePrimaryTelephoneSummary.row(request.userAnswers)
        ).flatten
      ).withCssClass("govuk-!-margin-bottom-9")
      val rfmSecondaryContactList = SummaryListViewModel(
        rows = Seq(
          RfmAddSecondaryContactSummary.row(request.userAnswers),
          RfmSecondaryContactNameSummary.row(request.userAnswers),
          RfmSecondaryContactEmailSummary.row(request.userAnswers),
          RfmSecondaryTelephonePreferenceSummary.row(request.userAnswers),
          RfmSecondaryTelephoneSummary.row(request.userAnswers)
        ).flatten
      ).withCssClass("govuk-!-margin-bottom-9")
      val address = SummaryListViewModel(
        rows = Seq(RfmContactAddressSummary.row(request.userAnswers, countryOptions)).flatten
      )
      if (request.userAnswers.rfmContactDetailStatus) {
        Ok(view(rfmCorporatePositionSummaryList, rfmPrimaryContactList, rfmSecondaryContactList, address))
      } else {
        Redirect(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad)
      }
    } else {
      Redirect(controllers.routes.UnderConstructionController.onPageLoad)
    }
  }

  def onSubmit(): Action[AnyContent] = (rfmIdentify andThen getData andThen requireData) { implicit request =>
    if (request.userAnswers.rfmContactDetailStatus && request.userAnswers.rfmNewFilingMemberDetailsStatus) {
      Redirect(controllers.routes.UnderConstructionController.onPageLoad)
    } else {
      Redirect(controllers.rfm.routes.RfmIncompleteDataController.onPageLoad)
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
