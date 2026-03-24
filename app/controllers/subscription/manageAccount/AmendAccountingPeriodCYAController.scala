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
import models.subscription.{AccountingPeriod, AccountingPeriodV2}
import pages.NewAccountingPeriodPage
import play.api.i18n.I18nSupport
import play.api.mvc.*
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import play.api.i18n.Messages
import utils.AmendAccountingPeriodDurationFormatter
import utils.DateTimeUtils
import views.html.subscriptionview.manageAccount.AmendAccountingPeriodCYAView

import java.time.LocalDate
import javax.inject.{Inject, Named}
import scala.concurrent.ExecutionContext

class AmendAccountingPeriodCYAController @Inject() (
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  checkAmendMultipleAPScreens:            AmendMultipleAccountingPeriodScreensAction,
  getData:                                SubscriptionDataRetrievalAction,
  requireData:                            SubscriptionDataRequiredAction,
  sessionRepository:                      SessionRepository,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   AmendAccountingPeriodCYAView
)(using ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen checkAmendMultipleAPScreens andThen getData andThen requireData).async { request =>
      given Request[AnyContent] = request
      sessionRepository.get(request.userId).map { maybeUserAnswers =>
        given Messages = request.messages
        (
          maybeUserAnswers.flatMap(_.get(NewAccountingPeriodPage)),
          request.subscriptionLocalData.accountingPeriods
        ) match {
          case (Some(newPeriod), Some(allPeriods)) =>
            val affected              = findAffectedPeriods(newPeriod.startDate, newPeriod.endDate, allPeriods)
            val predicted             = predictMicroPeriods(newPeriod, affected)
            val newDurationText       = AmendAccountingPeriodDurationFormatter.formatInclusivePeriod(newPeriod.startDate, newPeriod.endDate)
            val predictedWithDuration = predicted.map { p =>
              (p, AmendAccountingPeriodDurationFormatter.formatInclusivePeriod(p.startDate, p.endDate))
            }
            Ok(
              view(
                newPeriod,
                newDurationText,
                predictedWithDuration,
                request.isAgent,
                request.subscriptionLocalData.organisationName,
                request.subscriptionLocalData.plrReference
              )
            )
          case _ =>
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
      }
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen checkAmendMultipleAPScreens andThen getData andThen requireData) { _ =>
      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }

  private def findAffectedPeriods(
    newStart:   LocalDate,
    newEnd:     LocalDate,
    allPeriods: Seq[AccountingPeriodV2]
  ): Seq[AccountingPeriodV2] =
    allPeriods.filter(p => !p.startDate.isAfter(newEnd) && !p.endDate.isBefore(newStart))

  private def predictMicroPeriods(
    newPeriod: AccountingPeriod,
    affected:  Seq[AccountingPeriodV2]
  ): Seq[AccountingPeriod] =
    if affected.isEmpty then Seq.empty
    else {
      val earliestStart = affected.map(_.startDate).min
      val latestEnd     = affected.map(_.endDate).max
      val today         = DateTimeUtils.today

      val gapBefore: Option[AccountingPeriod] =
        if newPeriod.startDate.isAfter(earliestStart) then Some(AccountingPeriod(earliestStart, newPeriod.startDate.minusDays(1)))
        else None

      val gapAfter: Option[AccountingPeriod] =
        if newPeriod.endDate.isBefore(latestEnd) then Some(AccountingPeriod(newPeriod.endDate.plusDays(1), latestEnd))
        else None

      val openEnded: Seq[AccountingPeriod] =
        if newPeriod.endDate.isAfter(latestEnd) then generateOpenEndedPeriods(newPeriod.endDate.plusDays(1), today)
        else Seq.empty

      gapBefore.toSeq ++ gapAfter.toSeq ++ openEnded
    }

  private def generateOpenEndedPeriods(from: LocalDate, until: LocalDate): Seq[AccountingPeriod] =
    LazyList
      .unfold(from) { cur =>
        Option.when(!cur.isAfter(until)) {
          val periodEnd = cur.plusMonths(12).minusDays(1)
          (AccountingPeriod(cur, periodEnd), periodEnd.plusDays(1))
        }
      }
      .toSeq
}
