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
import connectors.UserAnswersConnectors
import controllers.actions._
import forms.ContactByTelephoneFormProvider
import models.Mode
import navigation.AmendSubscriptionNavigator
import pages.{SubPrimaryContactNamePage, SubPrimaryPhonePreferencePage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.ContactByTelephoneView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ContactByTelephoneController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  navigator:                 AmendSubscriptionNavigator,
  formProvider:              ContactByTelephoneFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      ContactByTelephoneView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers
      .get(SubPrimaryContactNamePage)
      .map { contactName =>
        val form = formProvider(contactName)
        val preparedForm = request.userAnswers.get(SubPrimaryPhonePreferencePage) match {
          case Some(v) => form.fill(v)
          case None    => form
        }
        Ok(view(preparedForm, mode, contactName))

      }
      .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(SubPrimaryContactNamePage)
      .map { contactName =>
        val form = formProvider(contactName)
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, contactName))),
            nominatePrimaryContactNumber =>
              for {
                updatedAnswers <-
                  Future.fromTry(request.userAnswers.set(SubPrimaryPhonePreferencePage, nominatePrimaryContactNumber))
                _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(navigator.nextPage(SubPrimaryPhonePreferencePage, mode, updatedAnswers))
          )
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }

}
