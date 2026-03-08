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

package controllers.subscription.manageAccount

import config.FrontendAppConfig
import controllers.actions.{IdentifierAction, SubscriptionDataRequiredAction, SubscriptionDataRetrievalAction}
import models.subscription.AccountingPeriodDisplay
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeUtils.*
import views.html.subscriptionview.manageAccount.ViewAccountingPeriodView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class ViewAccountingPeriodController @Inject() (
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getData:                                SubscriptionDataRetrievalAction,
  requireData:                            SubscriptionDataRequiredAction,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   ViewAccountingPeriodView
)(using ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(index: Int): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      implicit val messages: play.api.i18n.Messages = controllerComponents.messagesApi.preferred(request)
      request.session.get(ManageAccountV2SessionKeys.DisplaySubscriptionV2Periods) match {
        case None =>
          Future.successful(Redirect(controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad()))
        case Some(jsonStr) =>
          Json.parse(jsonStr).asOpt[Seq[AccountingPeriodDisplay]] match {
            case Some(periods) if index >= 0 && index < periods.size =>
              val period = periods(index)
              Future.successful(
                Ok(view(period.startDate.toDateFormat, period.endDate.toDateFormat))
              )
            case _ =>
              Future.successful(Redirect(controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad()))
          }
      }
    }
}
