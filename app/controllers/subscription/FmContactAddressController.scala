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

package controllers.subscription

import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions._
import controllers.routes
import forms.FmContactAddressFormProvider
import models.Mode
import models.subscription.FmContactAddress
import pages.SubscriptionPage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import views.html.errors.ErrorTemplate
import views.html.subscriptionview.FmContactAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FmContactAddressController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              FmContactAddressFormProvider,
  CountryOptions:            CountryOptions,
  val controllerComponents:  MessagesControllerComponents,
  view:                      FmContactAddressView,
  page_not_available:        ErrorTemplate
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val countryList = CountryOptions.options.sortWith((s, t) => s.label(0).toLower < t.label(0).toLower)
  val form: Form[FmContactAddress] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val notAvailable = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")
    request.userAnswers
      .get(SubscriptionPage)
      .fold(NotFound(notAvailable)) { reg =>
        reg.fmContactAddress.fold(NotFound(notAvailable))((address => Ok(view(form.fill(address), mode, countryList))))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, countryList))),
        value => {
          val regData = request.userAnswers.get(SubscriptionPage).getOrElse(throw new Exception("Is UPE registered in UK not been selected"))
          for {
            updatedAnswers <-
              Future.fromTry(
                request.userAnswers
                  .set(SubscriptionPage, regData.copy(fmContactAddress = Some(value)))
              )
            _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
          } yield Redirect(routes.UnderConstructionController.onPageLoad)
        }
      )
  }
}
