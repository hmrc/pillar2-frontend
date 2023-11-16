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
import models.registration.RegistrationInfo
import models.subscription.ReadSubscriptionRequestParameters
import pages.{UpeRegInformationPage, upeNameRegistrationPage}
import play.api.Logging
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
    with I18nSupport
    with Logging {
  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val userId         = request.userId
    val identifierName = "PLRID"
    val plrReference = request.enrolments
      .flatMap(_.find(_.key.equalsIgnoreCase(identifierName)))
      .flatMap(_.identifiers.find(_.key.equalsIgnoreCase(identifierName)))
      .map(_.value)
      .getOrElse(identifierName)

    val readSubscriptionParameters = ReadSubscriptionRequestParameters(userId, plrReference)

    readSubscriptionService.readSubscription(readSubscriptionParameters).flatMap {
      case Right(userAnswers) =>
        val organisationName = userAnswers.get[String](upeNameRegistrationPage).getOrElse("Default Organisation Name")

        val registrationDateFormatted = userAnswers
          .get[RegistrationInfo](UpeRegInformationPage)
          .flatMap(_.registrationDate)
          .map(_.format(DateTimeFormatter.ofPattern("d MMMM yyyy")))
          .getOrElse("Default Date")

        Future.successful(Ok(view(organisationName, registrationDateFormatted, plrReference)))

      case Left(error) =>
        logger.error(s"Error retrieving subscription  $error")
        Future.successful(InternalServerError("Subscription not found in user answers"))
    }
  }
}
