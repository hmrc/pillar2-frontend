/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.subscription.manageAccount

import config.FrontendAppConfig
import controllers.actions.*
import models.subscription.AccountingPeriodV2
import pages.*
import play.api.i18n.I18nSupport
import play.api.mvc.*
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.manageAccount.AmendAccountingPeriodConfirmationView

import javax.inject.{Inject, Named}
import scala.concurrent.ExecutionContext

class AmendAccountingPeriodConfirmationController @Inject() (
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getData:                                SubscriptionDataRetrievalAction,
  requireData:                            SubscriptionDataRequiredAction,
  sessionRepository:                      SessionRepository,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   AmendAccountingPeriodConfirmationView
)(using ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { request =>
      given Request[AnyContent] = request
      sessionRepository.get(request.userId).map {
        case Some(userAnswers) =>
          (for {
            originalPeriods <- userAnswers.get(OriginalAccountingPeriodsPage)
            updatedPeriods  <- userAnswers.get(UpdatedAccountingPeriodsPage)
            timestamp       <- userAnswers.get(AmendAPConfirmationTimestampPage)
          } yield {
            val newPeriods  = computeNewPeriods(originalPeriods, updatedPeriods)
            val hasGapPeriods = newPeriods.size > 1

            Ok(
              view(
                timestamp = timestamp,
                newPeriods = newPeriods,
                hasGapPeriods = hasGapPeriods,
                isAgent = request.isAgent,
                organisationName = request.subscriptionLocalData.organisationName,
                plrReference = request.subscriptionLocalData.plrReference
              )
            )
          }).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

        case None => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

  private def computeNewPeriods(
    original: Seq[AccountingPeriodV2],
    updated:  Seq[AccountingPeriodV2]
  ): Seq[AccountingPeriodV2] = {
    val originalSet = original.map(p => (p.startDate, p.endDate)).toSet
    updated.filterNot(p => originalSet.contains((p.startDate, p.endDate)))
  }
}
