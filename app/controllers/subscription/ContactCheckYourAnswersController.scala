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

package controllers.subscription

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.CheckYourAnswersLogicPage
import play.api.i18n.I18nSupport
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers.*
import viewmodels.govuk.summarylist.*
import views.html.subscriptionview.ContactCheckYourAnswersView

class ContactCheckYourAnswersController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  val controllerComponents:  MessagesControllerComponents,
  view:                      ContactCheckYourAnswersView,
  countryOptions:            CountryOptions
)(using appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { request =>
    given Request[AnyContent] = request
    val primaryContactList    = SummaryListViewModel(
      rows = Seq(
        ContactNameComplianceSummary.row(request.userAnswers),
        ContactEmailAddressSummary.row(request.userAnswers),
        ContactByPhoneSummary.row(request.userAnswers),
        ContactCapturePhoneDetailsSummary.row(request.userAnswers)
      ).flatten
    )
    val secondaryContactList = SummaryListViewModel(
      rows = Seq(
        AddSecondaryContactSummary.row(request.userAnswers),
        SecondaryContactNameSummary.row(request.userAnswers),
        SecondaryContactEmailSummary.row(request.userAnswers),
        SecondaryPhonePreferenceSummary.row(request.userAnswers),
        SecondaryPhoneSummary.row(request.userAnswers)
      ).flatten
    )
    val address = SummaryListViewModel(
      rows = Seq(ContactCorrespondenceAddressSummary.row(request.userAnswers, countryOptions)).flatten
    )
    val contactsStatus = request.userAnswers.contactsStatus
    val CheckYourAnswersLogic: Boolean = request.userAnswers.get(CheckYourAnswersLogicPage).isDefined
    if contactsStatus == RowStatus.Completed | contactsStatus == RowStatus.InProgress & CheckYourAnswersLogic then {
      Ok(view(primaryContactList, secondaryContactList, address))
    } else {
      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    Redirect(controllers.routes.TaskListController.onPageLoad)
  }
}
