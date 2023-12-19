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

package controllers.subscription.manageAccount

import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions._
import forms.ContactCaptureTelephoneDetailsFormProvider
import models.Mode
import pages.{subPrimaryCapturePhonePage, subPrimaryContactNamePage, subPrimaryPhonePreferencePage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.manageAccount.ContactCaptureTelephoneDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ContactCaptureTelephoneDetailsController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              ContactCaptureTelephoneDetailsFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      ContactCaptureTelephoneDetailsView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    (for {
      _           <- request.userAnswers.get(subPrimaryPhonePreferencePage)
      contactName <- request.userAnswers.get(subPrimaryContactNamePage)
    } yield {
      val form = formProvider(contactName)
      val preparedForm = request.userAnswers.get(subPrimaryCapturePhonePage) match {
        case Some(v) => form.fill(v)
        case None    => form
      }
      Ok(view(preparedForm, mode, contactName))

    })
      .getOrElse(Redirect(controllers.routes.BookmarkPreventionController.onPageLoad))

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(subPrimaryContactNamePage)
      .map { contactName =>
        val form = formProvider(contactName)
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, contactName))),
            value =>
              for {
                updatedAnswers <-
                  Future.fromTry(request.userAnswers set (subPrimaryCapturePhonePage, value))
                _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(controllers.subscription.manageAccount.routes.AddSecondaryContactController.onPageLoad)
          )
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }

}