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
import controllers.actions._
import forms.AgentClientPillar2ReferenceFormProvider
import models.{ApiError, UserAnswers}
import pages._
import play.api.Logging
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html._
import views.html.rfm.AgentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AgentController @Inject() (
  val controllerComponents:          MessagesControllerComponents,
  val userAnswersConnectors:         UserAnswersConnectors,
  sessionRepository:                 SessionRepository,
  subscriptionService:               SubscriptionService,
  agentView:                         AgentView,
  clientPillarIdView:                AgentClientPillarIdView,
  clientConfirmView:                 AgentClientConfirmDetailsView,
  clientNoMatchView:                 AgentClientNoMatch,
  agentErrorView:                    AgentErrorView,
  agentClientUnauthorisedView:       AgentClientUnauthorisedView,
  agentIndividualErrorView:          AgentIndividualErrorView,
  agentOrganisationErrorView:        AgentOrganisationErrorView,
  enrolmentIdentifierAction:         EnrolmentIdentifierAction,
  asaEnrolmentIdentifierAction:      ASAEnrolmentIdentifierAction,
  sessionDataRetrievalAction:        SessionDataRetrievalAction,
  sessionDataRequiredAction:         SessionDataRequiredAction,
  agentClientPillar2RefFormProvider: AgentClientPillar2ReferenceFormProvider
)(implicit appConfig:                FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val agentClientPillar2RefForm: Form[String] = agentClientPillar2RefFormProvider()

  def onPageLoad: Action[AnyContent] = Action { implicit request =>
    Ok(agentView()).withNewSession
  }

  def onPageLoadClientPillarId: Action[AnyContent] =
    (asaEnrolmentIdentifierAction andThen sessionDataRetrievalAction andThen sessionDataRequiredAction).async { implicit request =>
      for {
        answersWithoutSubmissionFlag <- Future.fromTry(request.userAnswers.remove(AgentClientConfirmationSubmittedPage))
        answersWithRedirectFlag      <- Future.fromTry(answersWithoutSubmissionFlag.set(RedirectToASAHome, true))
        _                            <- sessionRepository.set(answersWithRedirectFlag)
      } yield {
        val preparedForm = request.userAnswers.get(UnauthorisedClientPillar2ReferencePage) match {
          case Some(pillarId) => agentClientPillar2RefForm.fill(pillarId)
          case None           => agentClientPillar2RefForm
        }
        Ok(clientPillarIdView(preparedForm))
      }
    }

  def onSubmitClientPillarId: Action[AnyContent] =
    (asaEnrolmentIdentifierAction andThen sessionDataRetrievalAction andThen sessionDataRequiredAction).async { implicit request =>
      agentClientPillar2RefForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            logger.info(s"Agent has submitted client's Pillar 2 ID form with errors: ${formWithErrors.errors}")
            Future.successful(BadRequest(clientPillarIdView(formWithErrors)))
          },
          pillarIdValue => {
            logger.info(s"Agent submitted client's Pillar ID: $pillarIdValue")

            val result = for {
              answersWithoutSubmissionFlag <- Future.fromTry(request.userAnswers.remove(AgentClientConfirmationSubmittedPage))
              answersWithPillarId          <- Future.fromTry(answersWithoutSubmissionFlag.set(UnauthorisedClientPillar2ReferencePage, pillarIdValue))
              _                            <- sessionRepository.set(answersWithPillarId)
              subscriptionData             <- subscriptionService.readSubscription(pillarIdValue)
              answersWithOrgName <-
                Future.fromTry(answersWithPillarId.set(AgentClientOrganisationNamePage, subscriptionData.upeDetails.organisationName))
              _ <- sessionRepository.set(answersWithOrgName)
            } yield Redirect(routes.AgentController.onPageLoadConfirmClientDetails)

            result.recover { case _: ApiError =>
              logger.info(s"Could not find Agent's client with Pillar ID: $pillarIdValue")
              Redirect(routes.AgentController.onPageLoadNoClientMatch)
            }
          }
        )
    }

  def onPageLoadConfirmClientDetails: Action[AnyContent] =
    (asaEnrolmentIdentifierAction andThen sessionDataRetrievalAction andThen sessionDataRequiredAction).async { implicit request =>
      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(RedirectToASAHome, true))
        _              <- sessionRepository.set(updatedAnswers)
      } yield (request.userAnswers.get(UnauthorisedClientPillar2ReferencePage), request.userAnswers.get(AgentClientOrganisationNamePage))
        .mapN { (clientPillar2Id, clientUpeName) =>
          Ok(clientConfirmView(clientUpeName, clientPillar2Id))
        }
        .getOrElse(Redirect(routes.AgentController.onPageLoadError))
    }

  def onSubmitConfirmClientDetails: Action[AnyContent] =
    (enrolmentIdentifierAction andThen sessionDataRetrievalAction andThen sessionDataRequiredAction).async { implicit request =>
      val userAnswers: UserAnswers = request.userAnswers

      val submitted    = userAnswers.get(AgentClientConfirmationSubmittedPage)
      val unauthorised = userAnswers.get(UnauthorisedClientPillar2ReferencePage)

      (submitted, unauthorised) match {
        case (Some(true), _) =>
          logger.info("Agent has already submitted client's Pillar ID")
          Future.successful(Redirect(routes.DashboardController.onPageLoad))

        case (_, Some(newClientPillarId)) =>
          for {
            answersWithConfirmedClient <- Future.fromTry(userAnswers.set(AgentClientPillar2ReferencePage, newClientPillarId))
            answersWithoutUnauthorised <- Future.fromTry(answersWithConfirmedClient.remove(UnauthorisedClientPillar2ReferencePage))
            answersWithSubmissionFlag  <- Future.fromTry(answersWithoutUnauthorised.set(AgentClientConfirmationSubmittedPage, true))
            _                          <- sessionRepository.set(answersWithSubmissionFlag)
          } yield {
            logger.info("Agent has confirmed client's Pillar 2 ID successfully")
            Redirect(routes.DashboardController.onPageLoad)
          }

        case _ =>
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def onPageLoadNoClientMatch: Action[AnyContent] =
    (asaEnrolmentIdentifierAction andThen sessionDataRetrievalAction andThen sessionDataRequiredAction) { implicit request =>
      Ok(clientNoMatchView())
    }

  def onPageLoadError: Action[AnyContent] = Action { implicit request =>
    Ok(agentErrorView())
  }

  def onPageLoadUnauthorised: Action[AnyContent] =
    (asaEnrolmentIdentifierAction andThen sessionDataRetrievalAction andThen sessionDataRequiredAction) { implicit request =>
      Ok(agentClientUnauthorisedView())
    }

  def onPageLoadIndividualError: Action[AnyContent] = Action { implicit request =>
    Ok(agentIndividualErrorView())
  }

  def onPageLoadOrganisationError: Action[AnyContent] = Action { implicit request =>
    Ok(agentOrganisationErrorView())
  }

}
