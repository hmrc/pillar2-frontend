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
import controllers.actions.IdentifierAction
import models.rfm.RfmStatus
import models.rfm.RfmStatus.SuccessfullyCompleted
import play.api.i18n.I18nSupport
import play.api.mvc.*
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.rfm.RfmContactDetailsRegistrationView

import javax.inject.{Inject, Named}
import scala.concurrent.ExecutionContext

class RfmContactDetailsRegistrationController @Inject() (
  @Named("RfmIdentifier") identify: IdentifierAction,
  val controllerComponents:         MessagesControllerComponents,
  sessionRepository:                SessionRepository,
  view:                             RfmContactDetailsRegistrationView
)(using appConfig: FrontendAppConfig, executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = identify.async { request =>
    given Request[AnyContent] = request
    sessionRepository.get(request.userId).map {
      case Some(mongo) =>
        val rfmStatus = (mongo.data \ "rfmStatus").validate[RfmStatus].asOpt
        if rfmStatus.contains(SuccessfullyCompleted) then Redirect(controllers.rfm.routes.RfmCannotReturnAfterConfirmationController.onPageLoad)
        else Ok(view())
      case None =>
        Ok(view())
    }
  }
}
