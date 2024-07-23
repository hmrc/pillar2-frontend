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
import controllers.actions.{FeatureFlagActionFactory, IdentifierAction}
import models.Mode
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.rfm.CheckNewFilingMemberView

import javax.inject.{Inject, Named}
import scala.concurrent.Future

class CheckNewFilingMemberController @Inject() (
  @Named("RfmIdentifier") identify: IdentifierAction,
  featureAction:                    FeatureFlagActionFactory,
  val controllerComponents:         MessagesControllerComponents,
  view:                             CheckNewFilingMemberView
)(implicit appConfig:               FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (featureAction.rfmAccessAction andThen identify) { implicit request =>
    Ok(view(mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = identify.async {
    Future.successful(Redirect(controllers.rfm.routes.UkBasedFilingMemberController.onPageLoad(mode)))
  }
}
