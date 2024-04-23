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
import views.html.rfm.RfmSaveProgressInformView

class RfmSaveProgressInformController @Inject() (
  rfmIdentify:              RfmIdentifierAction,
  getData:                  DataRetrievalAction,
  requireData:              DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view:                     RfmSaveProgressInformView
)(implicit appConfig:       FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (rfmIdentify andThen getData andThen requireData) { implicit request =>
    val rfmEnabled = appConfig.rfmAccessEnabled
    if (rfmEnabled) {
      if (request.userAnswers.securityQuestionStatus == RowStatus.Completed) {
        Ok(view())
      } else {
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    } else {
      Redirect(controllers.routes.UnderConstructionController.onPageLoad)
    }
  }
}
