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

package controllers.fm

import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.NfmRegisteredAddressFormProvider
import models.{Mode, NonUKAddress}
import navigation.NominatedFilingMemberNavigator
import pages.{FmEntityTypePage, FmNameRegistrationPage, FmRegisteredAddressPage, FmRegisteredInUKPage}
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
  val countryOptions:        CountryOptions,
  val controllerComponents:  MessagesControllerComponents,
  navigator:                 NominatedFilingMemberNavigator,
  view:                      NfmRegisteredAddressView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {
  val form: Form[NonUKAddress] = formProvider()
  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers
      .get(FmNameRegistrationPage)
      .map { name =>
        val preparedForm = request.userAnswers.get(FmRegisteredAddressPage).map(address => form.fill(address)).getOrElse(form)

        Ok(
          view(
            preparedForm,
            mode,
            name,
            countryOptions.conditionalUkInclusion(request.userAnswers.get(FmRegisteredInUKPage), request.userAnswers.get(FmEntityTypePage))
          )
        )
      }
      .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(FmNameRegistrationPage)
      .map { name =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, name, countryOptions.options()))),
            value =>
              for {
                updatedAnswers <-
                  Future.fromTry(request.userAnswers.set(FmRegisteredAddressPage, value))
                _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(navigator.nextPage(FmRegisteredAddressPage, mode, updatedAnswers))
          )
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }

}
