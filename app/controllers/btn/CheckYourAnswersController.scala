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

package controllers.btn

import cats.syntax.functor.*
import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.*
import models.MneOrDomestic.Uk
import models.btn.BTNRequest
import models.longrunningsubmissions.LongRunningSubmission.BTN
import models.obligationsandsubmissions.AccountingPeriodDetails
import models.subscription.AccountingPeriod
import pages.*
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.*
import repositories.SessionRepository
import services.BtnSubmissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.*
import viewmodels.govuk.summarylist.*
import views.html.btn.{BTNCannotReturnView, CheckYourAnswersView}

import java.time.Clock
import javax.inject.Named
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  getData:                                SubscriptionDataRetrievalAction,
  requireData:                            SubscriptionDataRequiredAction,
  btnStatus:                              BTNStatusAction,
  sessionRepository:                      SessionRepository,
  view:                                   CheckYourAnswersView,
  cannotReturnView:                       BTNCannotReturnView,
  btnSubmissionService:                   BtnSubmissionService,
  val controllerComponents:               MessagesControllerComponents,
  @Named("EnrolmentIdentifier") identify: IdentifierAction
)(using ec: ExecutionContext, appConfig: FrontendAppConfig, clock: Clock)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData andThen btnStatus.subscriptionRequest).async { request =>
      given Request[AnyContent] = request
      sessionRepository.get(request.userId).map {
        case Some(userAnswers) =>
          userAnswers.get(EntitiesInsideOutsideUKPage) match {
            case Some(true) =>
              val maybeAccountingPeriodDetails: Option[AccountingPeriodDetails] = userAnswers.get(BTNChooseAccountingPeriodPage)

              val accountingPeriod: AccountingPeriod =
                maybeAccountingPeriodDetails
                  .map { accountingPeriodDetails =>
                    logger.info("Using AccountingPeriod from User Answers.")
                    AccountingPeriod(
                      startDate = accountingPeriodDetails.startDate,
                      endDate = accountingPeriodDetails.endDate,
                      dueDate = Some(accountingPeriodDetails.dueDate)
                    )
                  }
                  .getOrElse {
                    logger.info("No AccountingPeriod in User Answers. Using SubscriptionLocalData.")
                    request.subscriptionLocalData.subAccountingPeriod
                  }

              val summaryList = SummaryListViewModel(
                rows = Seq(
                  SubAccountingPeriodSummary.row(accountingPeriod, maybeAccountingPeriodDetails.isDefined),
                  BTNEntitiesInsideOutsideUKSummary.row(userAnswers, request.subscriptionLocalData.subMneOrDomestic == Uk)
                ).flatten
              ).withCssClass("govuk-!-margin-bottom-9")

              Ok(view(summaryList, request.isAgent, request.subscriptionLocalData.organisationName))
            case _ =>
              Redirect(controllers.routes.IndexController.onPageLoad)
          }
        case None =>
          logger.error("user answers not found")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

  def onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData).async { request =>
    given Request[AnyContent] = request
    sessionRepository.get(request.userId).flatMap {
      case Some(userAnswers) =>
        val subAccountingPeriod: AccountingPeriod =
          request.subscriptionLocalData.subAccountingPeriod
        val btnPayload = BTNRequest(
          accountingPeriodFrom = subAccountingPeriod.startDate,
          accountingPeriodTo = subAccountingPeriod.endDate
        )

        btnSubmissionService
          .startSubmission(
            userId = request.userId,
            userAnswers = userAnswers,
            pillar2Id = request.subscriptionLocalData.plrReference,
            accountingPeriod = subAccountingPeriod,
            btnPayload = btnPayload
          )
          .as(Redirect(controllers.routes.WaitingRoomController.onPageLoad(BTN)))
      case None =>
        logger.error("user answers not found")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }

  def cannotReturnKnockback: Action[AnyContent] = identify { request =>
    given Request[AnyContent] = request
    BadRequest(cannotReturnView())
  }
}
