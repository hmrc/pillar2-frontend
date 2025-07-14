/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.submissionhistory

import cats.data.OptionT
import pages.AgentClientPillar2ReferencePage
import config.FrontendAppConfig
import controllers.actions._
import models.UserAnswers
import play.api.i18n.I18nSupport
import play.api.i18n.Lang.logger
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.obligationsandsubmissions.ObligationsAndSubmissionsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Constants.SUBMISSION_ACCOUNTING_PERIODS
import views.html.submissionhistory.{SubmissionHistoryNoSubmissionsView, SubmissionHistoryView}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import services.ReferenceNumberService

class SubmissionHistoryController @Inject() (
  val controllerComponents:         MessagesControllerComponents,
  referenceNumberService:           ReferenceNumberService,
  obligationsAndSubmissionsService: ObligationsAndSubmissionsService,
  getData:                          DataRetrievalAction,
  requireData:                      DataRequiredAction,
  view:                             SubmissionHistoryView,
  viewNoSubmissions:                SubmissionHistoryNoSubmissionsView,
  sessionRepository:                SessionRepository,
  identify:                         IdentifierAction
)(implicit ec:                      ExecutionContext, config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    (for {
      maybeUserAnswer <- OptionT.liftF(sessionRepository.get(request.userId))
      userAnswers = maybeUserAnswer.getOrElse(UserAnswers(request.userId))
      referenceNumber <- OptionT
                           .fromOption[Future](userAnswers.get(AgentClientPillar2ReferencePage))
                           .orElse(OptionT.fromOption[Future](referenceNumberService.get(Some(userAnswers), request.enrolments)))

      fromDate  = LocalDate.now().minusYears(SUBMISSION_ACCOUNTING_PERIODS)
      toDate    = LocalDate.now()
      data <- OptionT.liftF(obligationsAndSubmissionsService.handleData(referenceNumber, fromDate, toDate))
    } yield
      if (data.accountingPeriodDetails.exists(_.obligations.exists(_.submissions.nonEmpty))) {
        Ok(view(data.accountingPeriodDetails, request.isAgent))
      } else {
        Ok(viewNoSubmissions(request.isAgent))
      }).value
      .map(_.getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad(None))))
      .recover { case e =>
        logger.error(s"Error calling obligationsAndSubmissionsService.handleData: ${e.getMessage}", e)
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad(None))
      }
  }
}
