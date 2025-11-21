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

package controllers.bta

import config.FrontendAppConfig
import controllers.actions.IdentifierAction
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.bta.NoPlrIdGuidanceView

import javax.inject.Inject

class NoPlrIdGuidanceController @Inject() (
  identify:                 IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  view:                     NoPlrIdGuidanceView
)(implicit appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = identify { implicit request =>
    val btaAccessEnabled: Boolean = appConfig.btaAccessEnabled
    if btaAccessEnabled then {
      Ok(view())
    } else {
      Redirect(controllers.routes.ErrorController.pageNotFoundLoad)
    }
  }

}
