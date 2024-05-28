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
import connectors.SubscriptionConnector
import controllers.actions._
import forms.MneOrDomesticFormProvider
import models.Mode
import navigation.AmendSubscriptionNavigator
import pages.SubMneOrDomesticPage
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.manageAccount.MneOrDomesticView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MneOrDomesticController @Inject() (
  val subscriptionConnector: SubscriptionConnector,
  identify:                  IdentifierAction,
  agentIdentifierAction:     AgentIdentifierAction,
  getData:                   SubscriptionDataRetrievalAction,
  requireData:               SubscriptionDataRequiredAction,
  navigator:                 AmendSubscriptionNavigator,
  formProvider:              MneOrDomesticFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      MneOrDomesticView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(clientPillar2Id: Option[String] = None): Action[AnyContent] =
    (identifierAction(clientPillar2Id, agentIdentifierAction, identify) andThen getData) { implicit request =>
      val preparedForm = request.maybeSubscriptionLocalData.flatMap(_.get(SubMneOrDomesticPage)) match {
        case Some(value) => form.fill(value)
        case None        => form
      }
      Ok(view(preparedForm, clientPillar2Id))
    }

  def onSubmit(clientPillar2Id: Option[String] = None): Action[AnyContent] =
    (identifierAction(clientPillar2Id, agentIdentifierAction, identify) andThen getData andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, clientPillar2Id))),
          value =>
            for {
              updatedAnswers <-
                Future
                  .fromTry(request.subscriptionLocalData.set(SubMneOrDomesticPage, value))
              _ <- subscriptionConnector.save(request.userId, Json.toJson(updatedAnswers))
            } yield Redirect(navigator.nextPage(SubMneOrDomesticPage, clientPillar2Id, updatedAnswers))
        )
    }

}
