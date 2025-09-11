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
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.{CheckYourAnswersLogicPage, UpeCapturePhonePage, UpeCyaCompletedPage, UpePhonePreferencePage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.registrationview.UpeCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class UpeCheckYourAnswersController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  val controllerComponents:  MessagesControllerComponents,
  view:                      UpeCheckYourAnswersView,
  countryOptions:            CountryOptions
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val phonePref = request.userAnswers.get(UpePhonePreferencePage)
    val upeStatus = phonePref match {
      case Some(true) => request.userAnswers.get(UpeCapturePhonePage).isDefined
      case _          => phonePref.isDefined
    }

    val CheckYourAnswersLogic: Boolean = request.userAnswers.get(CheckYourAnswersLogicPage).isDefined
    if (upeStatus | CheckYourAnswersLogic) {
      val list = SummaryListViewModel(
        rows = Seq(
          UpeNameRegistrationSummary.row(request.userAnswers),
          UpeRegisteredAddressSummary.row(request.userAnswers, countryOptions),
          UpeContactNameSummary.row(request.userAnswers),
          UpeContactEmailSummary.row(request.userAnswers),
          UpePhonePreferenceSummary.row(request.userAnswers),
          UPEContactPhoneSummary.row(request.userAnswers)
        ).flatten
      )
      Ok(view(list))
    } else {
      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    for {
      updatedAnswers <-
        Future.fromTry(request.userAnswers.set(UpeCyaCompletedPage, true))
      _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
    } yield Redirect(controllers.routes.TaskListController.onPageLoad.url)
  }
}
