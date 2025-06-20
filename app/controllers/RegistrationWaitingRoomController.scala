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
import controllers.actions.{IdentifierAction, SessionDataRequiredAction, SessionDataRetrievalAction}
import models.subscription.SubscriptionStatus._
import pages.SubscriptionStatusPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.registrationview.RegistrationWaitingRoomView

import javax.inject.Inject

class RegistrationWaitingRoomController @Inject() (
  getData:                  SessionDataRetrievalAction,
  identify:                 IdentifierAction,
  requireData:              SessionDataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view:                     RegistrationWaitingRoomView
)(implicit appConfig:       FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers
      .get(SubscriptionStatusPage) match {
      case Some(SuccessfullyCompletedSubscription)        => Redirect(routes.RegistrationConfirmationController.onPageLoad)
      case Some(RegistrationInProgress)                   => Ok(view(Some(RegistrationInProgress)))
      case Some(FailedWithDuplicatedSubmission)           => Redirect(controllers.subscription.routes.SubscriptionFailureController.onPageLoad)
      case Some(FailedWithUnprocessableEntity)            => Redirect(controllers.subscription.routes.SubscriptionFailureController.onPageLoad)
      case Some(FailedWithInternalIssueError)             => Redirect(controllers.subscription.routes.SubscriptionFailedController.onPageLoad)
      case Some(FailedWithNoMneOrDomesticValueFoundError) => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      case Some(FailedWithDuplicatedSafeIdError)          => Redirect(controllers.subscription.routes.DuplicateSafeIdController.onPageLoad)
      case s                                              => Ok(view(s))
    }

  }
}
