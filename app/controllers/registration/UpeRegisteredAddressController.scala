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

package controllers.registration

import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.UpeRegisteredAddressFormProvider
import models.{Mode, UKAddress}
import navigation.UltimateParentNavigator
import pages._
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
  val countryOptions:        CountryOptions,
  navigator:                 UltimateParentNavigator,
  val controllerComponents:  MessagesControllerComponents,
  view:                      UpeRegisteredAddressView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {
  val form: Form[UKAddress] = formProvider()
  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers
      .get(UpeNameRegistrationPage)
      .map { name =>
        val preparedForm = request.userAnswers.get(UpeRegisteredAddressPage) match {
          case Some(value) => form.fill(value)
          case None        => form
        }

        Ok(
          view(
            preparedForm,
            mode,
            name,
            countryOptions.conditionalUkInclusion(request.userAnswers.get(UpeRegisteredInUKPage), request.userAnswers.get(UpeEntityTypePage))
          )
        )
      }
      .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(UpeNameRegistrationPage)
      .map { name =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(
                  view(
                    formWithErrors,
                    mode,
                    name,
                    countryOptions.conditionalUkInclusion(request.userAnswers.get(UpeRegisteredInUKPage), request.userAnswers.get(UpeEntityTypePage))
                  )
                )
              ),
            value =>
              for {
                updatedAnswers <-
                  Future.fromTry(
                    request.userAnswers
                      .set(UpeRegisteredAddressPage, value)
                  )
                _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(navigator.nextPage(UpeRegisteredAddressPage, mode, updatedAnswers))
          )
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }
}
