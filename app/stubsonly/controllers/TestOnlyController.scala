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

package stubsonly.controllers

import controllers.actions.{DataRetrievalAction, IdentifierAction}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import stubsonly.connectors.TestOnlyConnector
import stubsonly.controllers.actions.TestOnlyAuthorisedAction
import uk.gov.hmrc.http.HeaderCarrier
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

  private val logger = Logger(getClass)
  def clearAllData(): Action[AnyContent] = Action.async { implicit request =>
    testOnlyConnector.clearAllData().map(httpResponse => Ok(httpResponse.body))
  }

  def clearCurrentData(): Action[AnyContent] = identity.async { implicit request =>
    testOnlyConnector.clearCurrentData(request.userId).map(httpResponse => Ok(httpResponse.body))
  }

  def getRegistrationData(): Action[AnyContent] = (identity andThen getData) { implicit request =>
    Ok(Json.toJson(request.userAnswers))
  }

  def getAllRecords(): Action[AnyContent] = Action.async { implicit request =>
    testOnlyConnector.getAllRecords().map(httpResponse => Ok((httpResponse.json)))
  }

  def upsertRecord(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val data = request.body
    implicit val hc: HeaderCarrier = HeaderCarrier()
    testOnlyConnector
      .upsertRecord(id, data)
      .map { _ =>
        Ok("Upsert successful")
      }
      .recover { case e: RuntimeException =>
        logger.error(s"Failed to upsert record with id: $id, data: $data", e)
        InternalServerError("Upsert failed")
      }
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

  def clearSession: Action[AnyContent] = Action { implicit request =>
    Redirect(controllers.eligibility.routes.GroupTerritoriesController.onPageLoad).withNewSession
  }

}
