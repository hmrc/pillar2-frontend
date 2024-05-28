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

import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, RfmIdentifierAction}
import pages.PlrReferencePage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Pillar2Reference
import views.html.rfm.RfmConfirmationView
import utils.ViewHelpers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RfmConfirmationController @Inject() (
  getData:                  DataRetrievalAction,
  rfmIdentify:              RfmIdentifierAction,
  requireData:              DataRequiredAction,
  sessionRepository:        SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view:                     RfmConfirmationView
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {
  val dateHelper = new ViewHelpers()

  def onPageLoad(): Action[AnyContent] = (rfmIdentify andThen getData andThen requireData).async { implicit request =>
    val rfmEnabled = appConfig.rfmAccessEnabled
    if (rfmEnabled) {
      val currentDate = HtmlFormat.escape(dateHelper.formatDateGDSTimeStamp(java.time.LocalDateTime.now))
      sessionRepository.get(request.userAnswers.id).map { optionalUserAnswers =>
        (for {
          userAnswer <- optionalUserAnswers
          pillar2Id <- Pillar2Reference
                         .getPillar2ID(request.enrolments, appConfig.enrolmentKey, appConfig.enrolmentIdentifier)
                         .orElse(userAnswer.get(PlrReferencePage))
        } yield Ok(view(pillar2Id, currentDate.toString()))).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    } else {
      Future.successful(Redirect(controllers.routes.UnderConstructionController.onPageLoad))
    }

  }
}
