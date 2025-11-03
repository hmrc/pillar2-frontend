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

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions._
import models.MneOrDomestic.Uk
import models.audit.{ApiResponseData, ApiResponseFailure}
import models.btn.{BTNRequest, BTNStatus}
import models.obligationsandsubmissions.AccountingPeriodDetails
import models.subscription.AccountingPeriod
import pages._
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.BTNService
import services.audit.AuditService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.btn.{BTNCannotReturnView, CheckYourAnswersView}

import java.time.ZonedDateTime
import javax.inject.Named
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  getData:                                SubscriptionDataRetrievalAction,
  requireData:                            SubscriptionDataRequiredAction,
  btnStatus:                              BTNStatusAction,
  sessionRepository:                      SessionRepository,
  view:                                   CheckYourAnswersView,
  cannotReturnView:                       BTNCannotReturnView,
  btnService:                             BTNService,
  val controllerComponents:               MessagesControllerComponents,
  auditService:                           AuditService,
  checkPhase2Screens:                     Phase2ScreensAction,
  @Named("EnrolmentIdentifier") identify: IdentifierAction
)(implicit ec:                            ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] =
    (identify andThen checkPhase2Screens andThen getData andThen requireData andThen btnStatus.subscriptionRequest).async { implicit request =>
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

  def onSubmit: Action[AnyContent] = (identify andThen checkPhase2Screens andThen getData andThen requireData).async { implicit request =>
    sessionRepository.get(request.userId).flatMap {
      case Some(userAnswers) =>
        val subAccountingPeriod: AccountingPeriod =
          request.subscriptionLocalData.subAccountingPeriod
        val btnPayload = BTNRequest(
          accountingPeriodFrom = subAccountingPeriod.startDate,
          accountingPeriodTo = subAccountingPeriod.endDate
        )

        implicit val pillar2Id: String = request.subscriptionLocalData.plrReference

        val setProcessingF: Future[Unit] = for {
          updatedAnswers <- Future.fromTry(userAnswers.set(BTNStatus, BTNStatus.processing))
          _              <- sessionRepository.set(updatedAnswers)
        } yield ()

        setProcessingF.foreach { _ =>
          btnService
            .submitBTN(btnPayload)
            .flatMap { resp =>
              sessionRepository.get(request.userId).flatMap {
                case Some(latest) =>
                  resp.result match {
                    case Right(_) =>
                      for {
                        submittedAnswers <- Future.fromTry(latest.set(BTNStatus, BTNStatus.submitted))
                        _                <- sessionRepository.set(submittedAnswers)
                        _ <- auditService.auditBTN(
                               pillarReference = pillar2Id,
                               accountingPeriod = subAccountingPeriod,
                               entitiesInsideAndOutsideUK = userAnswers.get(EntitiesInsideOutsideUKPage).getOrElse(false),
                               response = ApiResponseData.fromBtnResponse(resp)
                             )
                      } yield ()
                    case Left(_) =>
                      for {
                        errorAnswers <- Future.fromTry(latest.set(BTNStatus, BTNStatus.error))
                        _            <- sessionRepository.set(errorAnswers)
                        _ <- auditService.auditBTN(
                               pillarReference = pillar2Id,
                               accountingPeriod = subAccountingPeriod,
                               entitiesInsideAndOutsideUK = userAnswers.get(EntitiesInsideOutsideUKPage).getOrElse(false),
                               response = ApiResponseData.fromBtnResponse(resp)
                             )
                      } yield ()
                  }
                case None =>
                  Future.successful(())
              }
            }
            .recover { err: Throwable =>
              sessionRepository.get(request.userId).flatMap {
                case Some(latest) =>
                  for {
                    errorAnswers <- Future.fromTry(latest.set(BTNStatus, BTNStatus.error))
                    _            <- sessionRepository.set(errorAnswers)
                    _ <- auditService.auditBTN(
                           pillarReference = pillar2Id,
                           accountingPeriod = subAccountingPeriod,
                           entitiesInsideAndOutsideUK = userAnswers.get(EntitiesInsideOutsideUKPage).getOrElse(false),
                           response = ApiResponseFailure(
                             statusCode = INTERNAL_SERVER_ERROR,
                             processedAt = ZonedDateTime.now(),
                             errorCode = "InternalIssueError",
                             responseMessage = err.getMessage
                           )
                         )
                  } yield ()
                case None =>
                  Future.successful(())
              }
            }
        }

        Future.successful(Redirect(routes.BTNWaitingRoomController.onPageLoad))
      case None =>
        logger.error("user answers not found")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }

  def cannotReturnKnockback: Action[AnyContent] = (identify andThen checkPhase2Screens) { implicit request =>
    BadRequest(cannotReturnView())
  }
}
