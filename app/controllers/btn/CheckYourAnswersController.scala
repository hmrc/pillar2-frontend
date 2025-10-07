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
import models.audit.ApiResponseData
import models.btn.{BTNRequest, BTNStatus}
import models.UserAnswers
import models.obligationsandsubmissions.AccountingPeriodDetails
import models.subscription.AccountingPeriod
import pages._
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.{BTNService, ObligationsAndSubmissionsService}
import services.audit.AuditService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.btn.{BTNCannotReturnView, CheckYourAnswersView}

import java.time.format.DateTimeFormatter
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
  obligationsAndSubmissionsService:       ObligationsAndSubmissionsService,
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
                      duetDate = Some(accountingPeriodDetails.dueDate)
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

        implicit val pillar2Id: String        = request.subscriptionLocalData.plrReference
        implicit val hc:        HeaderCarrier = uk.gov.hmrc.play.http.HeaderCarrierConverter.fromRequestAndSession(request, request.session)

       
        checkUnderEnquiry(subAccountingPeriod).flatMap { isUnderEnquiry =>
          if (isUnderEnquiry) {
            logger.info(
              s"BTN submission warning - accounting period under enquiry for period ${subAccountingPeriod.startDate} to ${subAccountingPeriod.endDate}"
            )
            Future.successful(Redirect(routes.BTNUnderEnquiryWarningController.onPageLoad))
          } else {
            submitBTNRequest(userAnswers, subAccountingPeriod, btnPayload, pillar2Id, request.userId)
          }
        }
      case None =>
        logger.error("user answers not found")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }

  private def checkUnderEnquiry(accountingPeriod: AccountingPeriod)(implicit hc: HeaderCarrier, pillar2Id: String): Future[Boolean] =
    obligationsAndSubmissionsService
      .handleData(pillar2Id, accountingPeriod.startDate, accountingPeriod.endDate)
      .map { obligationsData =>
        obligationsData.accountingPeriodDetails
          .find(period =>
            period.startDate == accountingPeriod.startDate &&
              period.endDate == accountingPeriod.endDate
          )
          .exists(_.underEnquiry)
      }
      .recover { case ex =>
        logger.warn(s"Failed to check underEnquiry status: ${ex.getMessage}")
        false 
      }

  private def submitBTNRequest(
    userAnswers:         UserAnswers,
    subAccountingPeriod: AccountingPeriod,
    btnPayload:          BTNRequest,
    pillar2Id:           String,
    userId:              String
  )(implicit hc:         HeaderCarrier): Future[Result] = {
    val setProcessingF: Future[Unit] = for {
      updatedAnswers <- Future.fromTry(userAnswers.set(BTNStatus, BTNStatus.processing))
      _              <- sessionRepository.set(updatedAnswers)
    } yield ()

    setProcessingF.foreach { _ =>
      btnService
        .submitBTN(btnPayload)(hc, pillar2Id)
        .flatMap { resp =>
          sessionRepository.get(userId).flatMap {
            case Some(latest) =>
              for {
                submittedAnswers <- Future.fromTry(latest.set(BTNStatus, BTNStatus.submitted))
                _                <- sessionRepository.set(submittedAnswers)
                _ <- auditService.auditBTN(
                       pillarReference = pillar2Id,
                       accountingPeriod = subAccountingPeriod.toString,
                       entitiesInsideAndOutsideUK = userAnswers.get(EntitiesInsideOutsideUKPage).getOrElse(false),
                       apiResponseData = ApiResponseData(
                         statusCode = CREATED,
                         processingDate = resp.processingDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                         errorCode = None,
                         responseMessage = "Success"
                       )
                     )
              } yield ()
            case None =>
              Future.successful(())
          }
        }
        .recover { case err =>
          sessionRepository.get(userId).flatMap {
            case Some(latest) =>
              for {
                errorAnswers <- Future.fromTry(latest.set(BTNStatus, BTNStatus.error))
                _            <- sessionRepository.set(errorAnswers)
                _ <- auditService.auditBTN(
                       pillarReference = pillar2Id,
                       accountingPeriod = subAccountingPeriod.toString,
                       entitiesInsideAndOutsideUK = userAnswers.get(EntitiesInsideOutsideUKPage).getOrElse(false),
                       apiResponseData = ApiResponseData(
                         statusCode = INTERNAL_SERVER_ERROR,
                         processingDate = java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                         errorCode = Some("InternalIssueError"),
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
  }

  def cannotReturnKnockback: Action[AnyContent] = (identify andThen checkPhase2Screens) { implicit request =>
    BadRequest(cannotReturnView())
  }
}
