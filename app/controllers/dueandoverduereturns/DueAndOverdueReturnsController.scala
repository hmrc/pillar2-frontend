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

package controllers.dueandoverduereturns

import cats.data.OptionT
import config.FrontendAppConfig
import controllers.actions.*
import controllers.routes.JourneyRecoveryController
import models.UserAnswers
import pages.AgentClientPillar2ReferencePage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{ObligationsAndSubmissionsService, ReferenceNumberService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.Constants.SubmissionAccountingPeriods
import views.html.dueandoverduereturns.DueAndOverdueReturnsView

import java.time.LocalDate
import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class DueAndOverdueReturnsController @Inject() (
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  val controllerComponents:               MessagesControllerComponents,
  referenceNumberService:                 ReferenceNumberService,
  getData:                                DataRetrievalAction,
  requireData:                            DataRequiredAction,
  obligationsAndSubmissionsService:       ObligationsAndSubmissionsService,
  view:                                   DueAndOverdueReturnsView,
  sessionRepository:                      SessionRepository
)(implicit
  appConfig: FrontendAppConfig,
  ec:        ExecutionContext
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      (for {
        maybeUserAnswer <- OptionT.liftF(sessionRepository.get(request.userId))
        userAnswers = maybeUserAnswer.getOrElse(UserAnswers(request.userId))
        referenceNumber <- OptionT
                             .fromOption[Future](userAnswers.get(AgentClientPillar2ReferencePage))
                             .orElse(OptionT.fromOption[Future](referenceNumberService.get(Some(userAnswers), request.enrolments)))
        fromDate = LocalDate.now().minusYears(SubmissionAccountingPeriods)
        toDate   = LocalDate.now()
        data <- OptionT.liftF(obligationsAndSubmissionsService.handleData(referenceNumber, fromDate, toDate))
      } yield Ok(view(data, fromDate, toDate, request.isAgent))).value
        .map(_.getOrElse(Redirect(JourneyRecoveryController.onPageLoad())))
        .recover { case e =>
          logger.error(s"Error calling obligationsAndSubmissionsService.handleData: ${e.getMessage}", e)
          Redirect(JourneyRecoveryController.onPageLoad())
        }
    }
}
