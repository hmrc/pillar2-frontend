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

package controllers.subscription

import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.*
import forms.AddSecondaryContactFormProvider
import models.Mode
import navigation.SubscriptionNavigator
import pages.{SubAddSecondaryContactPage, SubPrimaryContactNamePage, SubPrimaryEmailPage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.AddSecondaryContactView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddSecondaryContactController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  navigator:                 SubscriptionNavigator,
  formProvider:              AddSecondaryContactFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      AddSecondaryContactView
)(using ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { request =>
    given Request[AnyContent] = request
    (for {
      _           <- request.userAnswers.get(SubPrimaryEmailPage)
      contactName <- request.userAnswers.get(SubPrimaryContactNamePage)
    } yield {
      val form: Form[Boolean] = formProvider(contactName)
      val preparedForm = request.userAnswers.get(SubAddSecondaryContactPage) match {
        case Some(value) => form.fill(value)
        case None        => form
      }
      Ok(view(preparedForm, contactName, mode))
    })
      .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { request =>
    given Request[AnyContent] = request
    request.userAnswers
      .get(SubPrimaryContactNamePage)
      .map { contactName =>
        val form = formProvider(contactName)
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, contactName, mode))),
            wantsToNominateSecondaryContact =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(SubAddSecondaryContactPage, wantsToNominateSecondaryContact))
                _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(navigator.nextPage(SubAddSecondaryContactPage, mode, updatedAnswers))
          )
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }

}
