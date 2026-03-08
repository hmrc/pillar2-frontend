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

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.IdentifierAction
import play.api.i18n.I18nSupport
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeUtils.*
import views.html.subscriptionview.manageAccount.AccountingPeriodChangeSuccessView

import javax.inject.Named
import scala.concurrent.{ExecutionContext, Future}

class ManageGroupDetailsSubmissionCompleteController @Inject() (
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   AccountingPeriodChangeSuccessView
)(using ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = identify.async { implicit request =>
    implicit val messages: play.api.i18n.Messages = controllerComponents.messagesApi.preferred(request)
    val session                                 = request.session
    (for {
      prevStr <- session.get(ManageAccountV2SessionKeys.PreviousAccountingPeriodForSuccess)
      newStr  <- session.get(ManageAccountV2SessionKeys.NewAccountingPeriodForSuccess)
      prev    <- ManageAccountV2SessionKeys.parsePeriodJson(prevStr)
      newP    <- ManageAccountV2SessionKeys.parsePeriodJson(newStr)
    } yield (prev, newP)) match {
      case Some(((prevStart, prevEnd), (newStart, newEnd))) =>
        val isAgent = session.get(ManageAccountV2SessionKeys.IsAgentForSuccess).contains("true")
        val result = Ok(
          view(
            newStart = newStart.toDateFormat,
            newEnd = newEnd.toDateFormat,
            previousStart = prevStart.toDateFormat,
            previousEnd = prevEnd.toDateFormat,
            isAgent = isAgent
          )
        )
        val clearedSession = session
          .-(ManageAccountV2SessionKeys.PreviousAccountingPeriodForSuccess)
          .-(ManageAccountV2SessionKeys.NewAccountingPeriodForSuccess)
          .-(ManageAccountV2SessionKeys.IsAgentForSuccess)
        Future.successful(result.withSession(clearedSession))
      case _ =>
        Future.successful(Redirect(controllers.routes.HomepageController.onPageLoad()))
    }
  }
}
