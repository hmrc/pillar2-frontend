/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.subscriptionview.ContactCheckYourAnswersView
import scala.concurrent.ExecutionContext

class ContactCheckYourAnswersController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  val controllerComponents:  MessagesControllerComponents,
  view:                      ContactCheckYourAnswersView,
  countryOptions:            CountryOptions
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
//    request.userAnswers.get(subAddSecondaryContactPage).map { provided =>
//      if (provided)
//        (for {
//          primaryName <- request.userAnswers.get(subPrimaryContactNamePage)
//          primaryEmail <- request.userAnswers.get(subPrimaryEmailPage)
//          primaryEmail <- request.userAnswers.get(subPrimaryEmailPage)
//
//        })
//    }
    val list = SummaryListViewModel(
      rows = Seq(UseContactPrimarySummary.row(request.userAnswers), UseContactPrimarySummary.row(request.userAnswers)).flatten
    )
    val address = SummaryListViewModel(
      rows = Seq(ContactCorrespondenceAddressSummary.row(request.userAnswers, countryOptions)).flatten
    )
    Ok(view())

  }
}
/*
UseContactPrimarySummary.row(request.userAnswers),
ContactNameComplianceSummary.row(request.userAnswers),
ContactEmailAddressSummary.row(request.userAnswers),
ContactByTelephoneSummary.row(request.userAnswers),
ContactCaptureTelephoneDetailsSummary.row(request.userAnswers),
AddSecondaryContactSummary.row(request.userAnswers),
SecondaryContactNameSummary.row(request.userAnswers),
SecondaryContactEmailSummary.row(request.userAnswers),
SecondaryTelephonePreferenceSummary.row(request.userAnswers),
SecondaryTelephoneSummary.row(request.userAnswers),
CaptureSubscriptionAddressAddressSummary.row(request.userAnswers, countryOptions)
 */
