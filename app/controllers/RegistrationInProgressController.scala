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
import models.subscription.ReadSubscriptionRequestParameters
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RegistrationInProgressView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RegistrationInProgressController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identify:                 IdentifierAction,
  view:                     RegistrationInProgressView,
  subscriptionService:      SubscriptionService
)(implicit appConfig:       FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(plrReference: String): Action[AnyContent] = identify.async { implicit request =>
    subscriptionService
      .maybeReadAndCacheSubscription(ReadSubscriptionRequestParameters(request.userId, plrReference))
      .map { maybeData =>
        if (maybeData.isDefined) {
          Redirect(controllers.routes.DashboardController.onPageLoad)
        } else {
          Ok(view(plrReference))
        }
      }
      .recover { case ex =>
        logger.error(s"RegistrationInProgressController - error retrieving subscription data for PLR reference: $plrReference", ex)
        InternalServerError("Error retrieving subscription data")
      }
  }
}
