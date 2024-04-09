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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, RfmIdentifierAction}
import models.Mode
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.rfm.ContactDetailsCheckYourAnswersView

class ContactDetailsCheckYourAnswersController @Inject() (
  rfmIdentify:              RfmIdentifierAction,
  getData:                  DataRetrievalAction,
  requireData:              DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  countryOptions:           CountryOptions,
  view:                     ContactDetailsCheckYourAnswersView
)(implicit appConfig:       FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (rfmIdentify andThen getData andThen requireData) { implicit request =>
    val primaryContactList = SummaryListViewModel(
      rows = Seq(
        RfmPrimaryContactNameSummary.row(request.userAnswers),
        RfmPrimaryContactEmailSummary.row(request.userAnswers),
        RfmContactByTelephoneSummary.row(request.userAnswers),
        RfmCapturePrimaryTelephoneSummary.row(request.userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

    val secondaryContactList = SummaryListViewModel(
      rows = Seq(
        RfmAddSecondaryContactSummary.row(request.userAnswers),
        RfmSecondaryContactNameSummary.row(request.userAnswers),
        RfmSecondaryContactEmailSummary.row(request.userAnswers),
        RfmSecondaryContactByTelephoneSummary.row(request.userAnswers),
        RfmCaptureSecondaryTelephoneSummary.row(request.userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

    val contactAddress = SummaryListViewModel(
      rows = Seq(
        RfmContactAddressSummary.row(request.userAnswers, countryOptions)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

    if (request.userAnswers.rfmContactDetailStatus) {
      Ok(view(primaryContactList, secondaryContactList, contactAddress))
    } else {
      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

}
