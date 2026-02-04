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
import models.obligationsandsubmissions.SubmissionType.{BTN, UKTR_AMEND, UKTR_CREATE}
import models.requests.ObligationsAndSubmissionsSuccessDataRequest
import models.{MneOrDomestic, Mode}
import pages.{EntitiesInsideOutsideUKPage, SubMneOrDomesticPage}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.*
import repositories.SessionRepository
import services.BTNAccountingPeriodService
import services.audit.AuditService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.btn.{BTNAccountingPeriodView, BTNAlreadyInPlaceView, BTNReturnSubmittedView}

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

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
  btnAccountingPeriodService:             BTNAccountingPeriodService,
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  auditService:                           AuditService
)(using ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getSubscriptionData andThen requireSubscriptionData andThen btnStatus.subscriptionRequest andThen requireObligationData)
      .async { request =>
        given ObligationsAndSubmissionsSuccessDataRequest[AnyContent] = request
        sessionRepository.get(request.userId).flatMap {
          case Some(userAnswers) =>
            Future
              .successful(btnAccountingPeriodService.selectAccountingPeriod(userAnswers, filteredAccountingPeriodDetails))
              .flatMap { period =>
                btnAccountingPeriodService
                  .outcome(userAnswers, period, filteredAccountingPeriodDetails, btnTypes = Set(BTN), uktrTypes = Set(UKTR_CREATE, UKTR_AMEND))
                  .match {
                    case BTNAccountingPeriodService.Outcome.BtnAlreadySubmitted =>
                      auditService
                        .auditBtnAlreadySubmitted(
                          request.subscriptionLocalData.plrReference,
                          request.subscriptionLocalData.subAccountingPeriod,
                          entitiesInsideOutsideUk = userAnswers.get(EntitiesInsideOutsideUKPage).getOrElse(false)
                        )
                        .as(Ok(btnAlreadyInPlaceView()))
                    case BTNAccountingPeriodService.Outcome.UktrReturnAlreadySubmitted =>
                      Future.successful(Ok(viewReturnSubmitted(request.isAgent, period)))
                    case BTNAccountingPeriodService.Outcome.ShowAccountingPeriod(summaryList, hasMultipleAccountingPeriods, currentAP) =>
                      Future.successful(
                        Ok(
                          accountingPeriodView(
                            summaryList,
                            mode,
                            request.isAgent,
                            request.subscriptionLocalData.organisationName,
                            hasMultipleAccountingPeriods,
                            currentAP
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
}
