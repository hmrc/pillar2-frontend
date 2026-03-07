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

import controllers.actions.{IdentifierAction, SubscriptionDataRequiredAction, SubscriptionDataRetrievalAction}
import controllers.routes
import models.requests.SubscriptionDataRequest
import models.subscription.AccountingPeriodDisplay
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class ChangeAccountingPeriodController @Inject() (
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getData:                                SubscriptionDataRetrievalAction,
  requireData:                            SubscriptionDataRequiredAction,
  val controllerComponents:               MessagesControllerComponents
)(using ec: ExecutionContext)
    extends FrontendBaseController {

  def onPageLoad(index: Int): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { request =>
      given SubscriptionDataRequest[AnyContent] = request
      request.session.get(ManageAccountV2SessionKeys.DisplaySubscriptionV2Periods) match {
        case None =>
          Future.successful(Redirect(controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad()))
        case Some(jsonStr) =>
          Json.parse(jsonStr).asOpt[Seq[AccountingPeriodDisplay]] match {
            case None =>
              Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
            case Some(periods) if index >= 0 && index < periods.size =>
              val period      = periods(index)
              val selectedJson = Json.obj(
                "startDate" -> period.startDate.toString,
                "endDate"   -> period.endDate.toString
              )
              Future.successful(
                Redirect(controllers.subscription.manageAccount.routes.GroupAccountingPeriodController.onPageLoad())
                  .withSession(request.session + (ManageAccountV2SessionKeys.DisplaySubscriptionV2Selected -> selectedJson.toString))
              )
            case _ =>
              Future.successful(Redirect(controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad()))
          }
      }
    }
}
