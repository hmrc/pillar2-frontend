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
import models.requests.DataRequest
import models.{Mode, UpeRegisteredAddress}
import pages.RegistrationPage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import views.html.errors.ErrorTemplate
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
  val form: Form[UpeRegisteredAddress] = formProvider()
  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    (for {
      reg      <- request.userAnswers.get(RegistrationPage)
      noIDData <- reg.withoutIdRegData
      userName <- noIDData.upeContactName
    } yield {
      val preparedForm = noIDData.upeRegisteredAddress.fold(form)(data => form fill data)
      Ok(view(preparedForm, mode, userName, countryList))
    }).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val userName = request.userAnswers.upeUserName
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, userName, countryList))),
        value =>
          request.userAnswers
            .get(RegistrationPage)
            .flatMap { reg =>
              reg.withoutIdRegData.map { withoutId =>
                for {
                  updatedAnswers <-
                    Future.fromTry(
                      request.userAnswers
                        .set(RegistrationPage, reg.copy(withoutIdRegData = Some(withoutId.copy(upeRegisteredAddress = Some(value)))))
                    )
                  _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                } yield Redirect(controllers.registration.routes.UpeContactNameController.onPageLoad(mode))
              }
            }
            .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
      )
  }
}
