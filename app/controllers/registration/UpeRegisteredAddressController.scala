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

package controllers.registration

import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.UpeRegisteredAddressFormProvider
import models.{Mode, RegisteredAddress}
import pages.{upeNameRegistrationPage, upeRegisteredAddressPage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import views.html.registrationview.UpeRegisteredAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpeRegisteredAddressController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              UpeRegisteredAddressFormProvider,
  CountryOptions:            CountryOptions,
  val controllerComponents:  MessagesControllerComponents,
  view:                      UpeRegisteredAddressView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {
  val countryList = CountryOptions.options.sortWith((s, t) => s.label(0).toLower < t.label(0).toLower)
  val form: Form[RegisteredAddress] = formProvider()
  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers
      .get(upeNameRegistrationPage)
      .map { name =>
        val preparedForm = request.userAnswers.get(upeRegisteredAddressPage) match {
          case Some(value) => form.fill(value)
          case None        => form
        }
        Ok(view(preparedForm, mode, name, countryList))
      }
      .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(upeNameRegistrationPage)
      .map { name =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, name, countryList))),
            value =>
              for {
                updatedAnswers <-
                  Future.fromTry(
                    request.userAnswers
                      .set(upeRegisteredAddressPage, value)
                  )
                _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(controllers.registration.routes.UpeContactNameController.onPageLoad(mode))
          )
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }
}
