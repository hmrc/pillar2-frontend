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

package controllers.btn

import config.FrontendAppConfig
import controllers.actions.*
import forms.BTNEntitiesInUKOnlyFormProvider
import models.Mode
import navigation.BTNNavigator
import pages.EntitiesInsideOutsideUKPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.btn.BTNEntitiesInUKOnlyView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class BTNEntitiesInUKOnlyController @Inject() (
  override val messagesApi:               MessagesApi,
  sessionRepository:                      SessionRepository,
  navigator:                              BTNNavigator,
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getSubscriptionData:                    SubscriptionDataRetrievalAction,
  requireSubscriptionData:                SubscriptionDataRequiredAction,
  btnStatus:                              BTNStatusAction,
  formProvider:                           BTNEntitiesInUKOnlyFormProvider,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   BTNEntitiesInUKOnlyView
)(using ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getSubscriptionData andThen requireSubscriptionData andThen btnStatus.subscriptionRequest).async { request =>
      given Request[AnyContent] = request
      sessionRepository.get(request.userId).flatMap {
        case Some(userAnswers) =>
          val form         = formProvider()
          val preparedForm = userAnswers.get(EntitiesInsideOutsideUKPage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          Future.successful(Ok(view(preparedForm, request.isAgent, request.subscriptionLocalData.organisationName, mode)))
        case None =>
          logger.error("user answers not found")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getSubscriptionData andThen requireSubscriptionData).async { request =>
      given Request[AnyContent] = request
      sessionRepository.get(request.userId).flatMap {
        case Some(userAnswers) =>
          val form = formProvider()
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, request.isAgent, request.subscriptionLocalData.organisationName, mode))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(userAnswers.set(EntitiesInsideOutsideUKPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(EntitiesInsideOutsideUKPage, updatedAnswers))
            )
        case None =>
          logger.error("user answers not found")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }
}
