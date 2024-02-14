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
import forms.RfmStartPageFormProvider
import models.Confirmation
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.Future
import play.api.data.Form
import views.html.rfm.StartPageView

class StartPageController @Inject() (
  formProvider:             RfmStartPageFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view:                     StartPageView,
  rfmIdentify:              RfmIdentifierAction
)(implicit val appConfig:   FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Set[Confirmation]] = formProvider("rfm.startPage.error")

  def onPageLoad: Action[AnyContent] = Action.async { implicit request =>
    val rfmAccessEnabled: Boolean = appConfig.rfmAccessEnabled
    if (rfmAccessEnabled) {
      Future.successful(Ok(view(form)))
    } else {
      Future.successful(Redirect(controllers.routes.UnderConstructionController.onPageLoad))
    }
  }

  def onSubmit: Action[AnyContent] = Action.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        error => Future.successful(BadRequest(view(error))),
        _ => Future.successful(Redirect(controllers.routes.UnderConstructionController.onPageLoad.url))
      )
  }

  def rfmAuthenticate: Action[_] = rfmIdentify { implicit request =>
    val rfmAccessEnabled: Boolean = appConfig.rfmAccessEnabled
    if (rfmAccessEnabled) {
      // redirect to
      Redirect(???)
    } else {
      Redirect(controllers.routes.UnderConstructionController.onPageLoad)
    }
  }

}
