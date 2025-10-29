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

import config.FrontendAppConfig
import controllers.actions._
import controllers.filteredAccountingPeriodDetails
import pages.BTNChooseAccountingPeriodPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeUtils._
import views.html.btn.BTNConfirmationView

import java.time.LocalDate
import javax.inject.{Inject, Named}
import scala.concurrent.ExecutionContext

class BTNConfirmationController @Inject() (
  val controllerComponents:               MessagesControllerComponents,
  getData:                                SubscriptionDataRetrievalAction,
  requireData:                            SubscriptionDataRequiredAction,
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  view:                                   BTNConfirmationView,
  checkPhase2Screens:                     Phase2ScreensAction,
  sessionRepository:                      SessionRepository,
  requireObligationData:                  ObligationsAndSubmissionsDataRetrievalAction
)(implicit ec:                            ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen checkPhase2Screens andThen getData andThen requireData andThen requireObligationData).async {
    implicit request =>
      sessionRepository.get(request.userId).map {
        case Some(userAnswers) =>
          val submissionDate:            String = LocalDate.now().toDateFormat
          val accountingPeriodStartDate: String = request.subscriptionLocalData.subAccountingPeriod.startDate.toDateFormat

          val showUnderEnquiryWarning = userAnswers
            .get(BTNChooseAccountingPeriodPage)
            .map { chosenPeriod =>
              val accountingPeriods = filteredAccountingPeriodDetails
              chosenPeriod.underEnquiry ||
              accountingPeriods
                .filter(_.startDate.isAfter(chosenPeriod.startDate))
                .exists(_.underEnquiry)
            }
            .getOrElse(false)

          Ok(
            view(request.subscriptionLocalData.organisationName, submissionDate, accountingPeriodStartDate, request.isAgent, showUnderEnquiryWarning)
          )
        case None =>
          val submissionDate:            String = LocalDate.now().toDateFormat
          val accountingPeriodStartDate: String = request.subscriptionLocalData.subAccountingPeriod.startDate.toDateFormat
          Ok(view(request.subscriptionLocalData.organisationName, submissionDate, accountingPeriodStartDate, request.isAgent, false))
      }
  }
}
