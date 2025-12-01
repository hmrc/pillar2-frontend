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

import cats.syntax.functor.*
import config.FrontendAppConfig
import controllers.actions.*
import controllers.filteredAccountingPeriodDetails
import models.obligationsandsubmissions.ObligationType.UKTR
import models.obligationsandsubmissions.SubmissionType.{BTN, UKTR_AMEND, UKTR_CREATE}
import models.obligationsandsubmissions.{AccountingPeriodDetails, SubmissionType}
import models.requests.ObligationsAndSubmissionsSuccessDataRequest
import models.{MneOrDomestic, Mode}
import pages.{BTNChooseAccountingPeriodPage, EntitiesInsideOutsideUKPage, SubMneOrDomesticPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.*
import repositories.SessionRepository
import services.audit.AuditService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeUtils.*
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.given
import views.html.btn.{BTNAccountingPeriodView, BTNAlreadyInPlaceView, BTNReturnSubmittedView}

import java.time.LocalDate
import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

class BTNAccountingPeriodController @Inject() (
  val controllerComponents:               MessagesControllerComponents,
  getSubscriptionData:                    SubscriptionDataRetrievalAction,
  requireSubscriptionData:                SubscriptionDataRequiredAction,
  btnStatus:                              BTNStatusAction,
  requireObligationData:                  ObligationsAndSubmissionsDataRetrievalAction,
  accountingPeriodView:                   BTNAccountingPeriodView,
  viewReturnSubmitted:                    BTNReturnSubmittedView,
  btnAlreadyInPlaceView:                  BTNAlreadyInPlaceView,
  sessionRepository:                      SessionRepository,
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  auditService:                           AuditService
)(using ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private def getSummaryList(startDate: LocalDate, endDate: LocalDate)(using messages: Messages): SummaryList =
    SummaryListViewModel(
      rows = Seq(
        SummaryListRowViewModel(
          key = "btn.returnSubmitted.startAccountDate",
          value = ValueViewModel(startDate.toDateFormat)
        ),
        SummaryListRowViewModel(
          key = "btn.returnSubmitted.endAccountDate",
          value = ValueViewModel(endDate.toDateFormat)
        )
      )
    )

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getSubscriptionData andThen requireSubscriptionData andThen btnStatus.subscriptionRequest andThen requireObligationData)
      .async { request =>
        given ObligationsAndSubmissionsSuccessDataRequest[AnyContent] = request
        sessionRepository.get(request.userId).flatMap {
          case Some(userAnswers) =>
            val accountingPeriodDetails: Future[AccountingPeriodDetails] = userAnswers.get(BTNChooseAccountingPeriodPage) match {
              case Some(details) =>
                Future.successful(details)
              case None =>
                filteredAccountingPeriodDetails match {
                  case singleAccountingPeriod :: Nil =>
                    Future.successful(singleAccountingPeriod)
                  case e =>
                    throw new RuntimeException(s"Expected one single accounting period but received: $e")
                }
            }

            accountingPeriodDetails
              .flatMap { period =>
                if lastSubmissionType(period, Set(BTN)) then {
                  auditService
                    .auditBtnAlreadySubmitted(
                      request.subscriptionLocalData.plrReference,
                      request.subscriptionLocalData.subAccountingPeriod,
                      entitiesInsideOutsideUk = userAnswers.get(EntitiesInsideOutsideUKPage).getOrElse(false)
                    )
                    .as(Ok(btnAlreadyInPlaceView()))
                } else if lastSubmissionType(period, Set(UKTR_CREATE, UKTR_AMEND)) then {
                  Future.successful(Ok(viewReturnSubmitted(request.isAgent, period)))
                } else {
                  val currentYear = filteredAccountingPeriodDetails match {
                    case head :: _ if head == period => true
                    case _                           => false
                  }

                  Future.successful(
                    Ok(
                      accountingPeriodView(
                        getSummaryList(period.startDate, period.endDate),
                        mode,
                        request.isAgent,
                        request.subscriptionLocalData.organisationName,
                        filteredAccountingPeriodDetails.size > 1,
                        currentYear
                      )
                    )
                  )
                }
              }
              .recover { case _ =>
                Redirect(controllers.btn.routes.BTNProblemWithServiceController.onPageLoad)
              }
          case None =>
            logger.error("user answers not found")
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
      }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getSubscriptionData).async { request =>
    request.maybeSubscriptionLocalData
      .flatMap(_.get(SubMneOrDomesticPage))
      .map { answer =>
        if answer == MneOrDomestic.UkAndOther then {
          Future.successful(Redirect(controllers.btn.routes.BTNEntitiesInsideOutsideUKController.onPageLoad(mode)))
        } else {
          Future.successful(Redirect(controllers.btn.routes.BTNEntitiesInUKOnlyController.onPageLoad(mode)))
        }
      }
      .getOrElse(Future.successful(Redirect(controllers.btn.routes.BTNProblemWithServiceController.onPageLoad)))
  }

  private def lastSubmissionType(period: AccountingPeriodDetails, submissionTypes: Set[SubmissionType]): Boolean =
    period.obligations
      .find(_.obligationType == UKTR)
      .flatMap(_.submissions.sortBy(_.receivedDate).lastOption)
      .exists(submission => submissionTypes.contains(submission.submissionType))
}
