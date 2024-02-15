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
import controllers.actions.RfmIdentifierAction
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject

class AuthenticateController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  rfmIdentify:              RfmIdentifierAction
)(implicit val appConfig:   FrontendAppConfig)
    extends FrontendBaseController {

  def rfmAuthenticate: Action[AnyContent] = rfmIdentify { _ =>
    val rfmAccessEnabled: Boolean = appConfig.rfmAccessEnabled
    if (rfmAccessEnabled) {
      // redirect to security question 1 screen
      Redirect(controllers.routes.UnderConstructionController.onPageLoad)
    } else {
      Redirect(controllers.routes.UnderConstructionController.onPageLoad)
    }
  }

}
