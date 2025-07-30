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

package controllers.repayments

import config.FrontendAppConfig
import controllers.actions._
import models.UserAnswers
import pages._
import pages.pdf.RepaymentConfirmationTimestampPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ViewHelpers
import views.html.repayments.RepaymentsConfirmationView

import javax.inject.{Inject, Named}

class RepaymentConfirmationController @Inject() (
  val controllerComponents:               MessagesControllerComponents,
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  view:                                   RepaymentsConfirmationView,
  getSessionData:                         SessionDataRetrievalAction,
  requireSessionData:                     SessionDataRequiredAction
)(implicit appConfig:                     FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {
  val dateHelper = new ViewHelpers()

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getSessionData andThen requireSessionData) { implicit request =>
      implicit val userAnswers: UserAnswers = request.userAnswers
      val currentDate           = HtmlFormat.escape(dateHelper.getDateTimeGMT)
      val confirmationTimestamp = request.userAnswers.get(RepaymentConfirmationTimestampPage).getOrElse(currentDate.toString())
      userAnswers.get(RepaymentCompletionStatus) match {
        case Some(true) => Ok(view(confirmationTimestamp))
        case _          => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }
}
