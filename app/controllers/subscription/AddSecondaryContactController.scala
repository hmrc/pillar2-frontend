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
import controllers.actions._
import forms.AddSecondaryContactFormProvider
import models.Mode
import pages.{subAddSecondaryContactPage, subPrimaryContactNamePage, subPrimaryEmailPage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.AddSecondaryContactView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddSecondaryContactController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              AddSecondaryContactFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      AddSecondaryContactView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val rfmAccessEnabled = appConfig.rfmAccessEnabled
    if (rfmAccessEnabled) {
      (for {
        _           <- request.userAnswers.get(subPrimaryEmailPage)
        contactName <- request.userAnswers.get(subPrimaryContactNamePage)
      } yield {
        val preparedForm = request.userAnswers.get(subAddSecondaryContactPage) match {
          case Some(value) => form.fill(value)
          case None        => form
        }
        Ok(view(preparedForm, contactName, mode))
      })
    } else {
      Redirect(controllers.routes.UnderConstructionController.onPageLoad)
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(subPrimaryContactNamePage)
      .map { contactName =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, contactName, mode))),
            value =>
              value match {
                case true =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(subAddSecondaryContactPage, value))
                    _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                  } yield Redirect(controllers.subscription.routes.SecondaryContactNameController.onPageLoad(mode))

                case false =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(subAddSecondaryContactPage, value))
                    _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                  } yield Redirect(controllers.subscription.routes.CaptureSubscriptionAddressController.onPageLoad(mode))
              }
          )
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }

}
