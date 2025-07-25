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
import controllers.actions.{IdentifierAction, SubscriptionDataRequiredAction, SubscriptionDataRetrievalAction}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.FopService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ViewHelpers
import views.html.btn.BTNConfirmationView
import views.xml.pdf.BTNConfirmationPdf

import java.time.LocalDate
import javax.inject.{Inject, Named}
import scala.concurrent.ExecutionContext

class BTNConfirmationController @Inject() (
  val controllerComponents:               MessagesControllerComponents,
  getData:                                SubscriptionDataRetrievalAction,
  requireData:                            SubscriptionDataRequiredAction,
  dateHelper:                             ViewHelpers,
  fopService:                             FopService,
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  view:                                   BTNConfirmationView,
  confirmationPdf:                        BTNConfirmationPdf
)(implicit ec:                            ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val submissionDate            = dateHelper.formatDateGDS(LocalDate.now())
    val accountingPeriodStartDate = dateHelper.formatDateGDS(request.subscriptionLocalData.subAccountingPeriod.startDate)

    Ok(view(request.subscriptionLocalData.organisationName, submissionDate, accountingPeriodStartDate, request.isAgent))
  }

  def onDownloadConfirmation: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val currentDate = dateHelper.formatDateGDS(LocalDate.now())
    val startDate   = dateHelper.formatDateGDS(request.subscriptionLocalData.subAccountingPeriod.startDate)

    for {
      pdf <- fopService.render(confirmationPdf.render(currentDate, startDate, request, implicitly).body)
    } yield Ok(pdf)
      .as("application/octet-stream")
      .withHeaders(CONTENT_DISPOSITION -> "attachment; filename=below-threshold-notification-confirmation.pdf")
  }

}
