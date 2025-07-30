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
import models.subscription.AccountingPeriod
import models.{Mode, UserAnswers}
import pages.PlrReferencePage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{ObligationsAndSubmissionsService, SubscriptionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.btn.BTNBeforeStartView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BTNBeforeStartController @Inject() (
  val controllerComponents:         MessagesControllerComponents,
  view:                             BTNBeforeStartView,
  identify:                         IdentifierAction,
  agentAccess:                      AgentAccessFilterAction,
  getData:                          DataRetrievalAction,
  requireData:                      DataRequiredAction,
  obligationsAndSubmissionsService: ObligationsAndSubmissionsService,
  subscriptionService:              SubscriptionService,
  sessionRepository:                SessionRepository,
  checkPhase2Screens:               Phase2ScreensAction
)(implicit appConfig:               FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen checkPhase2Screens andThen agentAccess andThen getData andThen requireData).async { implicit request =>
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
            multipleAccountingPeriods(
              subscriptionData.subAccountingPeriod,
              subscriptionData.plrReference,
              subscriptionData.accountStatus.forall(_.inactive)
            ).map { hasMultipleAccountingPeriods =>
              Ok(view(request.isAgent, hasMultipleAccountingPeriods, mode))
            }
          case None =>
            Future.successful(Redirect(controllers.btn.routes.BTNProblemWithServiceController.onPageLoad))
        }
        .recover { case _ =>
          Redirect(controllers.btn.routes.BTNProblemWithServiceController.onPageLoad)
        }
    }

  private def multipleAccountingPeriods(
    subAccountPeriod: AccountingPeriod,
    pillar2Id:        String,
    accountStatus:    Boolean
  )(implicit hc:      HeaderCarrier): Future[Boolean] = {
    val now: LocalDate = LocalDate.now()

    obligationsAndSubmissionsService
      .handleData(pillar2Id, subAccountPeriod.startDate, now)
      .map { success =>
        !accountStatus && filteredAccountingPeriodDetails(success.accountingPeriodDetails).size > 1
      }
  }
}
