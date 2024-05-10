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
import forms.SecondaryContactNameFormProvider
import models.Mode
import navigation.AmendSubscriptionNavigator
import pages.SubSecondaryContactNamePage
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.manageAccount.SecondaryContactNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SecondaryContactNameController @Inject() (
  val subscriptionConnector: SubscriptionConnector,
  identify:                  IdentifierAction,
  getData:                   SubscriptionDataRetrievalAction,
  requireData:               SubscriptionDataRequiredAction,
  navigator:                 AmendSubscriptionNavigator,
  formProvider:              SecondaryContactNameFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      SecondaryContactNameView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.subscriptionLocalData.get(SubSecondaryContactNamePage) match {
      case Some(v) => form.fill(v)
      case None    => form
    }
    Ok(view(preparedForm, mode, request.isAgent))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.isAgent))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.subscriptionLocalData.set(SubSecondaryContactNamePage, value))
            _              <- subscriptionConnector.save(request.userId, Json.toJson(updatedAnswers))
          } yield Redirect(navigator.nextPage(SubSecondaryContactNamePage, mode, updatedAnswers))
      )
  }

}
