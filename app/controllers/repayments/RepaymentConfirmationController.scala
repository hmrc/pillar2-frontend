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

package controllers.repayments

import config.FrontendAppConfig
import controllers.actions.*
import models.UserAnswers
import pages.*
import play.api.i18n.I18nSupport
import play.api.mvc.*
import services.SubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.repayments.RepaymentsConfirmationView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class RepaymentConfirmationController @Inject() (
  val controllerComponents:               MessagesControllerComponents,
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  val subscriptionService:                SubscriptionService,
  view:                                   RepaymentsConfirmationView,
  getSessionData:                         SessionDataRetrievalAction,
  requireSessionData:                     SessionDataRequiredAction
)(using appConfig: FrontendAppConfig, executionContext: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getSessionData andThen requireSessionData).async { request =>
      given Request[AnyContent] = request
      given userAnswers: UserAnswers = request.userAnswers
      (for {
        confirmationTimestamp <- userAnswers.get(RepaymentConfirmationPage)
        completionStatus      <- userAnswers.get(RepaymentCompletionStatus) if completionStatus
        plrRef                <- userAnswers.get(PlrReferencePage)
      } yield subscriptionService.maybeReadSubscription(plrRef).map {
        case Some(subscription) =>
          val orgName = subscription.upeDetails.organisationName
          Ok(view(confirmationTimestamp, plrRef, orgName, request.request.isAgent))
        case None =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }).getOrElse(
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      )
    }
}
