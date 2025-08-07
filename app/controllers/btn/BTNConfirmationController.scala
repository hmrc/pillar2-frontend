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
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ViewHelpers
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
  checkPhase2Screens:                     Phase2ScreensAction
)(implicit ec:                            ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen checkPhase2Screens andThen getData andThen requireData) { implicit request =>
    val submissionDate            = ViewHelpers.formatDateGDS(LocalDate.now())
    val accountingPeriodStartDate = ViewHelpers.formatDateGDS(request.subscriptionLocalData.subAccountingPeriod.startDate)

    Ok(view(request.subscriptionLocalData.organisationName, submissionDate, accountingPeriodStartDate, request.isAgent))
  }
}
