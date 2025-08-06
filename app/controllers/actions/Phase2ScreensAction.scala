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

package controllers.actions

import config.FrontendAppConfig
import models.requests.IdentifierRequest
import play.api.mvc.{ActionRefiner, Result, Results}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Phase2ScreensActionImpl @Inject() (
  config:                        FrontendAppConfig
)(implicit val executionContext: ExecutionContext)
    extends Phase2ScreensAction {

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, IdentifierRequest[A]]] =
    if (config.phase2ScreensEnabled) {
      Future.successful(Right(request))
    } else {
      Future.successful(Left(Results.Redirect(controllers.routes.DashboardController.onPageLoad)))
    }
}

trait Phase2ScreensAction extends ActionRefiner[IdentifierRequest, IdentifierRequest]
