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

package controllers

import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.{subMneOrDomesticPage, subPrimaryContactNamePage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Pillar2Reference, Pillar2SessionKeys}
import viewmodels.checkAnswers.GroupAccountingPeriodStartDateSummary.dateHelper
import views.html.RegistrationConfirmationView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RegistrationConfirmationController @Inject() (
  getData:                  DataRetrievalAction,
  identify:                 IdentifierAction,
  requireData:              DataRequiredAction,
  sessionRepository:        SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view:                     RegistrationConfirmationView
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val optionPillar2Reference = Pillar2Reference.getPillar2ID(request.enrolments).orElse(request.session.get("plrId"))
    val currentDate            = HtmlFormat.escape(dateHelper.formatDateGDS(java.time.LocalDate.now))
    sessionRepository.get(request.userAnswers.id).map { optionalUserAnswers =>
      (for {
        pillar2Id  <- optionPillar2Reference
        userAnswer <- optionalUserAnswers
        mneOrDom   <- userAnswer.get(subMneOrDomesticPage)
      } yield Ok(view(pillar2Id, currentDate.toString(), mneOrDom))).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }

}
