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
import cats.implicits.catsStdInstancesForFuture
import config.FrontendAppConfig
import controllers.actions._
import controllers.filteredAccountingPeriodDetails
import models.{Mode, UserAnswers}
import pages.PlrReferencePage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{ObligationsAndSubmissionsService, SubscriptionService}
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
  obligationsAndSubmissionsService:       ObligationsAndSubmissionsService,
  subscriptionService:                    SubscriptionService,
  sessionRepository:                      SessionRepository,
  checkPhase2Screens:                     Phase2ScreensAction,
  @Named("EnrolmentIdentifier") identify: IdentifierAction
)(implicit appConfig:                     FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen checkPhase2Screens andThen getData andThen requireData).async { implicit request =>
      implicit val hc: HeaderCarrier =
        HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      (
        for {
          maybeUserAnswer <- OptionT.liftF(sessionRepository.get(request.userId))
          userAnswers = maybeUserAnswer.getOrElse(UserAnswers(request.userId))
          maybeSubscriptionData <- OptionT.liftF(subscriptionService.getSubscriptionCache(request.userId))
          updatedAnswers        <- OptionT.liftF(Future.fromTry(userAnswers.set(PlrReferencePage, maybeSubscriptionData.plrReference)))
          _                     <- OptionT.liftF(sessionRepository.set(updatedAnswers))
        } yield maybeSubscriptionData
      ).value
        .flatMap {
          case Some(subscriptionData) =>
            obligationsAndSubmissionsService
              .handleData(subscriptionData.plrReference, now.minusYears(SUBMISSION_ACCOUNTING_PERIODS), now)
              .map(success => filteredAccountingPeriodDetails(success.accountingPeriodDetails).size > 1)
              .map(hasMultipleAccountingPeriods => Ok(view(request.isAgent, hasMultipleAccountingPeriods, mode)))
          case None =>
            Future.successful(Redirect(controllers.btn.routes.BTNProblemWithServiceController.onPageLoad))
        }
        .recover { case _ =>
          Redirect(controllers.btn.routes.BTNProblemWithServiceController.onPageLoad)
        }
    }
}
