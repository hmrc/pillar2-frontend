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

import cats.data.OptionT
import config.FrontendAppConfig
import controllers.actions._
import controllers.filteredAccountingPeriodDetails
import models.{Mode, UserAnswers}
import pages.{AgentClientPillar2ReferencePage, BTNChooseAccountingPeriodPage}
import play.api.i18n.I18nSupport
import play.api.i18n.Lang.logger
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{ObligationsAndSubmissionsService, ReferenceNumberService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.Constants.SUBMISSION_ACCOUNTING_PERIODS
import views.html.btn.BTNBeforeStartView

import java.time.LocalDate.now
import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class BTNBeforeStartController @Inject() (
  val controllerComponents:               MessagesControllerComponents,
  view:                                   BTNBeforeStartView,
  getData:                                DataRetrievalAction,
  requireData:                            DataRequiredAction,
  referenceNumberService:                 ReferenceNumberService,
  obligationsAndSubmissionsService:       ObligationsAndSubmissionsService,
  sessionRepository:                      SessionRepository,
  checkPhase2Screens:                     Phase2ScreensAction,
  @Named("EnrolmentIdentifier") identify: IdentifierAction
)(implicit appConfig:                     FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen checkPhase2Screens andThen getData andThen requireData).async { implicit request =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      (for {
        maybeUserAnswer <- OptionT.liftF(sessionRepository.get(request.userId))
        userAnswers = maybeUserAnswer.getOrElse(UserAnswers(request.userId))
        plrId <- OptionT
                   .fromOption[Future](userAnswers.get(AgentClientPillar2ReferencePage))
                   .orElse(OptionT.fromOption[Future](referenceNumberService.get(Some(userAnswers), request.enrolments)))
        osData <- OptionT.liftF(obligationsAndSubmissionsService.handleData(plrId, now.minusYears(SUBMISSION_ACCOUNTING_PERIODS), now))
      } yield {
        val filteredAps = filteredAccountingPeriodDetails(osData.accountingPeriodDetails)
        if (filteredAps.size > 1) {
          Ok(view(request.isAgent, hasMultipleAccountingPeriods = true, mode))
        } else {
          request.userAnswers
            .set(BTNChooseAccountingPeriodPage, filteredAps.head)
            .map(sessionRepository.set)

          Ok(view(request.isAgent, hasMultipleAccountingPeriods = false, mode))
        }
      }).value
        .map(_.getOrElse(Redirect(controllers.btn.routes.BTNProblemWithServiceController.onPageLoad)))
        .recover { case e =>
          logger.error(s"Error calling obligationsAndSubmissionsService.handleData: ${e.getMessage}", e)
          Redirect(controllers.btn.routes.BTNProblemWithServiceController.onPageLoad)
        }
    }
}
