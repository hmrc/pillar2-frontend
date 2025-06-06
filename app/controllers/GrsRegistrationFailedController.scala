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
import controllers.actions.IdentifierAction
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.registrationview.{RegistrationFailedNfmView, RegistrationFailedRfmView, RegistrationFailedUpeView}

import javax.inject.{Inject, Named}

class GrsRegistrationFailedController @Inject() (
  identify:                            IdentifierAction,
  @Named("RfmIdentifier") rfmIdentify: IdentifierAction,
  val controllerComponents:            MessagesControllerComponents,
  upeView:                             RegistrationFailedUpeView,
  nfmView:                             RegistrationFailedNfmView,
  rfmView:                             RegistrationFailedRfmView
)(implicit appConfig:                  FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoadUpe: Action[AnyContent] = identify { implicit request =>
    Ok(upeView())
  }

  def onPageLoadNfm: Action[AnyContent] = identify { implicit request =>
    Ok(nfmView())
  }

  def onPageLoadRfm: Action[AnyContent] = rfmIdentify { implicit request =>
    Ok(rfmView())
  }
}
