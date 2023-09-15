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
import models.registration.WithoutIdRegData
import models.requests.DataRequest
import models.{Mode, UpeRegisteredAddress}
import navigation.Navigator
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
  page_not_available:        ErrorTemplate,
  view:                      UpeRegisteredAddressView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {
  val countryList = CountryOptions.options.sortWith((s, t) => s.label(0).toLower < t.label(0).toLower)
  val form: Form[UpeRegisteredAddress] = formProvider()
  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val userName     = getUserName(request)
    val notAvailable = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")
    isPreviousPageDefined(request) match {
      case true =>
        request.userAnswers
          .get(RegistrationPage)
          .fold(NotFound(notAvailable)) { reg =>
            reg.withoutIdRegData.fold(NotFound(notAvailable))(data =>
              data.upeRegisteredAddress.fold(Ok(view(form, mode, userName, countryList)))(address =>
                Ok(view(form.fill(address), mode, userName, countryList))
              )
            )
          }

      case false => NotFound(notAvailable)
    }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val userName = getUserName(request)
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, userName, countryList))),
        value => {
          val regData          = request.userAnswers.get(RegistrationPage).getOrElse(throw new Exception("Is UPE registered in UK not been selected"))
          val regDataWithoutId = regData.withoutIdRegData.getOrElse(throw new Exception("upeNameRegistration should be available before address"))
          for {
            updatedAnswers <-
              Future.fromTry(
                request.userAnswers
                  .set(RegistrationPage, regData.copy(withoutIdRegData = Some(regDataWithoutId.copy(upeRegisteredAddress = Some(value)))))
              )
            _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
          } yield Redirect(controllers.registration.routes.UpeContactNameController.onPageLoad(mode))
        }
      )
  }

  private def getUserName(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(RegistrationPage)
    registration.fold("")(regData => regData.withoutIdRegData.fold("")(withoutId => withoutId.upeNameRegistration))
  }

  private def isPreviousPageDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(RegistrationPage)
      .fold(false)(data => data.isUPERegisteredInUK.toString.nonEmpty)
}
