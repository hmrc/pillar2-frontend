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

package controllers.subscription

import config.FrontendAppConfig
import controllers.actions.IdentifierAction
import models._
import pages.PlrReferencePage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.EmptyStateHomepageView
import views.html.errors.SubscriptionFailureView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SubscriptionFailureController @Inject() (
  identify:                 IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  view:                     SubscriptionFailureView,
  emptyStateHomepageView:   EmptyStateHomepageView,
  sessionRepository:        SessionRepository
)(implicit appConfig:       FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = identify { implicit request =>
    Ok(view(appConfig.supportUrl))
  }

  def emptyStatePage: Action[AnyContent] = identify.async { implicit request =>
    sessionRepository.get(request.userId).map { maybeUserAnswers =>
      val userAnswers  = maybeUserAnswers.getOrElse(UserAnswers(request.userId))
      val plrReference = userAnswers.get(PlrReferencePage).getOrElse("")
      Ok(emptyStateHomepageView(plrReference, request.isAgent))
    }
  }

}
