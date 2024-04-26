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
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, FeatureFlagActionFactory, IdentifierAction}
import forms.AgentClientPillar2ReferenceFormProvider
import models.InternalIssueError
import pages.AgentClientPillar2ReferencePage
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{AgentClientNoMatch, AgentClientPillarIdView}
import views.html.rfm.AgentView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class AgentController @Inject() (
  val controllerComponents:           MessagesControllerComponents,
  val userAnswersConnectors:          UserAnswersConnectors,
  subscriptionService:                SubscriptionService,
  view:                               AgentView,
  clientPillarIdView:                 AgentClientPillarIdView,
  clientNoMatchView:                  AgentClientNoMatch,
  @Named("AgentIdentifier") identify: IdentifierAction,
  featureAction:                      FeatureFlagActionFactory,
  getData:                            DataRetrievalAction,
  requireData:                        DataRequiredAction,
  formProvider:                       AgentClientPillar2ReferenceFormProvider
)(implicit appConfig:                 FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad: Action[AnyContent] = Action { implicit request =>
    Ok(view()).withNewSession
  }

  def onPageLoadClientPillarId: Action[AnyContent] = (featureAction.asaAccessAction andThen identify andThen getData andThen requireData).async {
    implicit request =>
      val preparedForm = request.userAnswers.get(AgentClientPillar2ReferencePage) match {
        case Some(value) => form.fill(value)
        case None        => form
      }

      Future.successful(Ok(clientPillarIdView(preparedForm)))
  }

  def onSubmitClientPillarId: Action[AnyContent] = (featureAction.asaAccessAction andThen identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(clientPillarIdView(formWithErrors))),
          value => {
            val result = for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(AgentClientPillar2ReferencePage, value))
              _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              _              <- subscriptionService.readSubscription(value)
            } yield Redirect(routes.UnderConstructionController.onPageLoad)

            result.recover { case InternalIssueError =>
              Redirect(
                routes.AgentController.onPageLoadNoClientMatch
              )
            }
          }
        )
  }

  def onPageLoadNoClientMatch: Action[AnyContent] = (featureAction.asaAccessAction andThen identify andThen getData andThen requireData) {
    implicit request =>
      Ok(clientNoMatchView())
  }

}
