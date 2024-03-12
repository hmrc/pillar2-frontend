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
import forms.SecondaryTelephoneFormProvider
import models.Mode
import pages.{SubSecondaryCapturePhonePage, SubSecondaryContactNamePage, subSecondaryPhonePreferencePage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.SecondaryTelephoneView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SecondaryTelephoneController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              SecondaryTelephoneFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      SecondaryTelephoneView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    (for {
      contactName <- request.userAnswers.get(SubSecondaryContactNamePage)
      _           <- request.userAnswers.get(subSecondaryPhonePreferencePage)
    } yield {
      val form = formProvider(contactName)
      val preparedForm = request.userAnswers.get(SubSecondaryCapturePhonePage) match {
        case Some(v) => form.fill(v)
        case None    => form
      }
      Ok(view(preparedForm, mode, contactName))

    })
      .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(SubSecondaryContactNamePage)
      .map { contactName =>
        val form = formProvider(contactName)
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, contactName))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(SubSecondaryCapturePhonePage, value))
                _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(controllers.subscription.routes.CaptureSubscriptionAddressController.onPageLoad(mode))
          )
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }

}
