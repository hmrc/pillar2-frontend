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

import config.FrontendAppConfig
import controllers.actions._
import controllers.filteredAccountingPeriodDetails
import forms.BTNChooseAccountingPeriodFormProvider
import models.Mode
import pages.BTNChooseAccountingPeriodPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.btn.BTNChooseAccountingPeriodView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class BTNChooseAccountingPeriodController @Inject() (
  override val messagesApi:               MessagesApi,
  sessionRepository:                      SessionRepository,
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getSubscriptionData:                    SubscriptionDataRetrievalAction,
  requireSubscriptionData:                SubscriptionDataRequiredAction,
  requireObligationData:                  ObligationsAndSubmissionsDataRetrievalAction,
  btnStatus:                              BTNStatusAction,
  formProvider:                           BTNChooseAccountingPeriodFormProvider,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   BTNChooseAccountingPeriodView,
  checkPhase2Screens:                     Phase2ScreensAction
)(implicit ec:                            ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen checkPhase2Screens andThen getSubscriptionData andThen requireSubscriptionData andThen btnStatus.subscriptionRequest andThen requireObligationData)
      .async { implicit request =>
        sessionRepository.get(request.userId).flatMap {
          case Some(userAnswers) =>
            val accountingPeriods = filteredAccountingPeriodDetails.zipWithIndex
            val form              = formProvider()
            val preparedForm = userAnswers
              .get(BTNChooseAccountingPeriodPage)
              .flatMap { chosenPeriod =>
                accountingPeriods.find(_._1 == chosenPeriod).map { case (_, index) =>
                  form.fill(index)
                }
              }
              .getOrElse(form)
            Future.successful(Ok(view(preparedForm, mode, request.isAgent, request.subscriptionLocalData.organisationName, accountingPeriods)))
          case None =>
            logger.error("user answers not found")
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
      }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen checkPhase2Screens andThen getSubscriptionData andThen requireSubscriptionData andThen btnStatus.subscriptionRequest andThen requireObligationData)
      .async { implicit request =>
        sessionRepository.get(request.userId).flatMap {
          case Some(userAnswers) =>
            val accountingPeriods = filteredAccountingPeriodDetails.zipWithIndex
            val form              = formProvider()
            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future.successful(
                    BadRequest(
                      view(
                        formWithErrors,
                        mode,
                        request.isAgent,
                        request.subscriptionLocalData.organisationName,
                        accountingPeriods
                      )
                    )
                  ),
                value =>
                  accountingPeriods.find { case (_, index) => index == value } match {
                    case Some((chosenPeriod, chosenIndex)) =>
                      for {
                        updatedAnswers <- Future.fromTry(userAnswers.set(BTNChooseAccountingPeriodPage, chosenPeriod))
                        _              <- sessionRepository.set(updatedAnswers)
                      } yield {
                        val shouldShowWarning = chosenPeriod.underEnquiry ||
                          accountingPeriods
                            .take(chosenIndex)
                            .exists(_._1.underEnquiry)

                        if (shouldShowWarning) {
                          Redirect(routes.BTNUnderEnquiryWarningController.onPageLoad)
                        } else {
                          Redirect(controllers.btn.routes.BTNAccountingPeriodController.onPageLoad(mode))
                        }
                      }
                    case None =>
                      Future.successful(Redirect(controllers.btn.routes.BTNProblemWithServiceController.onPageLoad))
                  }
              )
          case None =>
            logger.error("user answers not found")
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
      }
}
