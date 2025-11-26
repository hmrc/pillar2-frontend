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

package controllers.payments

import config.FrontendAppConfig
import connectors.OPSConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.UserAnswers
import models.requests.OptionalDataRequest
import pages.AgentClientPillar2ReferencePage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.ReferenceNumberService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{LegacyMakeAPaymentDashboardView, MakeAPaymentDashboardView}

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class MakeAPaymentDashboardController @Inject() (
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  val controllerComponents:               MessagesControllerComponents,
  referenceNumberService:                 ReferenceNumberService,
  sessionRepository:                      SessionRepository,
  payByBankAccountView:                   MakeAPaymentDashboardView,
  legacyView:                             LegacyMakeAPaymentDashboardView,
  getData:                                DataRetrievalAction,
  opsConnector:                           OPSConnector
)(implicit appConfig: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def extractReferenceNumber(userAnswers: Option[UserAnswers])(implicit request: OptionalDataRequest[AnyContent]) =
    userAnswers
      .flatMap(_.get(AgentClientPillar2ReferencePage))
      .orElse(referenceNumberService.get(userAnswers, request.enrolments))

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData).async { implicit request: OptionalDataRequest[AnyContent] =>
      (for {
        userAnswers     <- sessionRepository.get(request.userId)
        referenceNumber <-
          extractReferenceNumber(userAnswers).fold(Future.failed[String](new RuntimeException("Reference number not found")))(Future.successful)
        view = if appConfig.enablePayByBankAccount then payByBankAccountView(referenceNumber) else legacyView(referenceNumber)
      } yield Ok(view)).recover(_ => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }

  def onRedirect(): Action[AnyContent] = (identify andThen getData).async { implicit request: OptionalDataRequest[AnyContent] =>
    (for {
      userAnswers     <- sessionRepository.get(request.userId)
      referenceNumber <-
        extractReferenceNumber(userAnswers).fold(Future.failed[String](new RuntimeException("Reference number not found")))(Future.successful)
      redirect <- opsConnector.getRedirectLocation(referenceNumber)
    } yield Redirect(redirect, SEE_OTHER)).recover(_ => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }
}
