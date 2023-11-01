/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers

import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ReadSubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DashboardView

import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
class DashboardController @Inject() (
  getData:                     DataRetrievalAction,
  identify:                    IdentifierAction,
  requireData:                 DataRequiredAction,
  val readSubscriptionService: ReadSubscriptionService,
  val controllerComponents:    MessagesControllerComponents,
  view:                        DashboardView
)(implicit ec:                 ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val userId = request.userAnswers.id
    readSubscriptionService.readSubscription(id = userId, plrReference = "XMPLR0123456789").flatMap {
      case Right(subscription) =>
        val organisationName = subscription.upeDetails.map(_.organisationName).getOrElse("Default Organisation Name")
        val registrationDate =
          subscription.upeDetails.map(_.registrationDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))).getOrElse("Default Date")
        val plrRef = subscription.formBundleNumber.getOrElse("XMPLR0123456789")
        Future.successful(Ok(view(organisationName, registrationDate, plrRef)))

      case Left(error) =>
        Future.successful(InternalServerError("Subscription not found in user answers"))
    }
  }

}
