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

package controllers.fm

import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.NfmRegisteredAddressFormProvider
import models.{Mode, RegisteredAddress}
import pages.{fmNameRegistrationPage, fmRegisteredAddressPage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import views.html.fmview.NfmRegisteredAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NfmRegisteredAddressController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              NfmRegisteredAddressFormProvider,
  countryOptions:            CountryOptions,
  val controllerComponents:  MessagesControllerComponents,
  view:                      NfmRegisteredAddressView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {
  val form: Form[RegisteredAddress] = formProvider()
  val countryList = countryOptions.options.sortWith((s, t) => s.label(0).toLower < t.label(0).toLower)
  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers.get(fmNameRegistrationPage).map { name =>
      val preparedForm = request.userAnswers.get(fmRegisteredAddressPage) match {
        case Some(value) => form.fill(value)
        case None => form
      }
      Ok(view(preparedForm, mode, name,countryList))
    }.getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers.get(fmNameRegistrationPage).map { name =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, name, countryList))),
        value => {
          for {
            updatedAnswers <-
              Future.fromTry(request.userAnswers.set(fmRegisteredAddressPage, value))
            _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
          } yield Redirect(controllers.fm.routes.NfmContactNameController.onPageLoad(mode))
        }
      )}.getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }

}
