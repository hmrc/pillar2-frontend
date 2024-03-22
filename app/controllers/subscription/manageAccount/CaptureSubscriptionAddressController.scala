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

import cats.data.OptionT
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.CaptureSubscriptionAddressFormProvider
import models.Mode
import models.hods.UpeCorrespAddressDetails.makeSubscriptionAddress
import navigation.AmendSubscriptionNavigator
import pages.SubRegisteredAddressPage
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ReadSubscriptionService, ReferenceNumberService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import views.html.subscriptionview.manageAccount.CaptureSubscriptionAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CaptureSubscriptionAddressController @Inject() (
  val userAnswersConnectors:   UserAnswersConnectors,
  identify:                    IdentifierAction,
  getData:                     DataRetrievalAction,
  requireData:                 DataRequiredAction,
  navigator:                   AmendSubscriptionNavigator,
  val readSubscriptionService: ReadSubscriptionService,
  referenceNumberService:      ReferenceNumberService,
  formProvider:                CaptureSubscriptionAddressFormProvider,
  val countryOptions:          CountryOptions,
  val controllerComponents:    MessagesControllerComponents,
  view:                        CaptureSubscriptionAddressView
)(implicit ec:                 ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async { implicit request =>
    (for {
      plrReference <- OptionT.fromOption[Future](referenceNumberService.get(None, request.enrolments))
      subData      <- OptionT.liftF(readSubscriptionService.readSubscription(plrReference))
    } yield {
      val address = request.userAnswers.get(SubRegisteredAddressPage).getOrElse(makeSubscriptionAddress(subData.upeCorrespAddressDetails))
      Ok(view(form.fill(address), mode, countryOptions.options()))

    }).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, countryOptions.options()))),
        value =>
          for {
            updatedAnswers <-
              Future.fromTry(request.userAnswers.set(SubRegisteredAddressPage, value))
            _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
          } yield Redirect(navigator.nextPage(SubRegisteredAddressPage, mode, updatedAnswers))
      )
  }

}
