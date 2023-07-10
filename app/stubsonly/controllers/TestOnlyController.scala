/*
 * Copyright 2023 HM Revenue & Customs
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

package stubsonly.controllers

import controllers.actions.{DataRetrievalAction, IdentifierAction}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import stubsonly.connectors.TestOnlyConnector
import stubsonly.controllers.actions.TestOnlyAuthorisedAction
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestOnlyController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identity:                 IdentifierAction,
  testOnlyAuthorise:        TestOnlyAuthorisedAction,
  getData:                  DataRetrievalAction,
  testOnlyConnector:        TestOnlyConnector
)(implicit val ec:          ExecutionContext)
    extends FrontendBaseController {

  def clearAllData(): Action[AnyContent] = Action.async { implicit request =>
    testOnlyConnector.clearAllData().map(httpResponse => Ok(httpResponse.body))
  }

  def clearCurrentData(): Action[AnyContent] = Action.async { implicit request =>
    testOnlyConnector.clearCurrentData().map(httpResponse => Ok(httpResponse.body))
  }

  def getRegistrationData(): Action[AnyContent] = (identity andThen getData) { implicit request =>
    Ok(Json.toJson(request.userAnswers))
  }

  def deEnrol(): Action[AnyContent] = testOnlyAuthorise.async { implicit request =>
    request.pillar2Reference match {
      case Some(reference) =>
        testOnlyConnector
          .deEnrol(request.groupId, reference)
          .map(httpResponse => Ok(httpResponse.body))
      case None => Future.successful(Ok("No Pillar2 enrolment found"))
    }

  }

}
