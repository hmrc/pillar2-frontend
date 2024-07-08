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

import cats.implicits._
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{ASAEnrolmentIdentifierAction, EnrolmentIdentifierAction, FeatureFlagActionFactory, SessionDataRequiredAction, SessionDataRetrievalAction}
import forms.AgentClientPillar2ReferenceFormProvider
import models.InternalIssueError
import pages.{AgentClientOrganisationNamePage, AgentClientPillar2ReferencePage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.rfm.AgentView
import views.html._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AgentController @Inject() (
  val controllerComponents:    MessagesControllerComponents,
  val userAnswersConnectors:   UserAnswersConnectors,
  sessionRepository:           SessionRepository,
  subscriptionService:         SubscriptionService,
  view:                        AgentView,
  clientPillarIdView:          AgentClientPillarIdView,
  clientConfirmView:           AgentClientConfirmDetailsView,
  clientNoMatchView:           AgentClientNoMatch,
  agentErrorView:              AgentErrorView,
  agentClientUnauthorisedView: AgentClientUnauthorisedView,
  agentIndividualErrorView:    AgentIndividualErrorView,
  agentOrganisationErrorView:  AgentOrganisationErrorView,
  identify:                    EnrolmentIdentifierAction,
  asaIdentify:                 ASAEnrolmentIdentifierAction,
  featureAction:               FeatureFlagActionFactory,
  getData:                     SessionDataRetrievalAction,
  requireData:                 SessionDataRequiredAction,
  formProvider:                AgentClientPillar2ReferenceFormProvider
)(implicit appConfig:          FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form: Form[String] = formProvider()

  def onPageLoad: Action[AnyContent] = Action { implicit request =>
    Ok(view()).withNewSession
  }

  def onPageLoadClientPillarId: Action[AnyContent] =
    (featureAction.asaAccessAction andThen asaIdentify andThen getData andThen requireData).async { implicit request =>
      val preparedForm = request.userAnswers.get(AgentClientPillar2ReferencePage) match {
        case Some(value) => form.fill(value)
        case None        => form
      }

      Future.successful(Ok(clientPillarIdView(preparedForm)))
    }

  def onSubmitClientPillarId: Action[AnyContent] =
    (featureAction.asaAccessAction andThen asaIdentify andThen getData andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(clientPillarIdView(formWithErrors))),
          value => {
            val result = for {
              updatedAnswers     <- Future.fromTry(request.userAnswers.set(AgentClientPillar2ReferencePage, value))
              _                  <- sessionRepository.set(updatedAnswers)
              subscriptionData   <- subscriptionService.readSubscription(value)
              answersWithOrgName <- Future.fromTry(updatedAnswers.set(AgentClientOrganisationNamePage, subscriptionData.upeDetails.organisationName))
              _                  <- sessionRepository.set(answersWithOrgName)
            } yield Redirect(routes.AgentController.onPageLoadConfirmClientDetails)

            result.recover { case InternalIssueError =>
              Redirect(
                routes.AgentController.onPageLoadNoClientMatch
              )
            }
          }
        )
    }

  def onPageLoadConfirmClientDetails: Action[AnyContent] =
    (featureAction.asaAccessAction andThen asaIdentify andThen getData andThen requireData).async { implicit request =>
      (request.userAnswers.get(AgentClientPillar2ReferencePage), request.userAnswers.get(AgentClientOrganisationNamePage))
        .mapN { (clientPillar2Id, clientUpeName) =>
          Future successful Ok(clientConfirmView(clientUpeName, clientPillar2Id))
        }
        .getOrElse(Future successful Redirect(routes.AgentController.onPageLoadError))
    }

  def onSubmitConfirmClientDetails: Action[AnyContent] =
    (featureAction.asaAccessAction andThen identify andThen getData andThen requireData).async {
      Future successful Redirect(routes.DashboardController.onPageLoad)
    }

  def onPageLoadNoClientMatch: Action[AnyContent] =
    (featureAction.asaAccessAction andThen asaIdentify andThen getData andThen requireData) { implicit request =>
      Ok(clientNoMatchView())
    }

  def onPageLoadError: Action[AnyContent] =
    featureAction.asaAccessAction { implicit request =>
      Ok(agentErrorView())
    }

  def onPageLoadUnauthorised: Action[AnyContent] =
    (featureAction.asaAccessAction andThen asaIdentify andThen getData andThen requireData) { implicit request =>
      Ok(agentClientUnauthorisedView())
    }

  def onPageLoadIndividualError: Action[AnyContent] = featureAction.asaAccessAction { implicit request =>
    Ok(agentIndividualErrorView())
  }

  def onPageLoadOrganisationError: Action[AnyContent] = featureAction.asaAccessAction { implicit request =>
    Ok(agentOrganisationErrorView())
  }

}
