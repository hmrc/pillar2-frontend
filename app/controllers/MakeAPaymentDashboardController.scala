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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.MakeAPaymentDashboardView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class MakeAPaymentDashboardController @Inject() (
  identify:                 IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  view:                     MakeAPaymentDashboardView,
  getData:                  DataRetrievalAction,
  requireData:              DataRequiredAction
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.enrolments
      .flatMap(
        _.find(_.key.equalsIgnoreCase("HMRC-PILLAR2-ORG"))
          .flatMap(_.identifiers.find(_.key.equalsIgnoreCase("PLRID")))
          .map(_.value)
          .map { plrID =>
            Ok(view(plrID))
          }
      )
      .getOrElse(
        Redirect(controllers.routes.BookmarkPreventionController.onPageLoad)
      )

  }

}
