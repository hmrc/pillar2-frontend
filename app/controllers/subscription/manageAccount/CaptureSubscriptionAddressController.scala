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
import connectors.SubscriptionConnector
import controllers.actions.{IdentifierAction, SubscriptionDataRequiredAction, SubscriptionDataRetrievalAction}
import forms.CaptureSubscriptionAddressFormProvider
import models.NonUKAddress
import pages.SubRegisteredAddressPage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import views.html.subscriptionview.manageAccount.CaptureSubscriptionAddressView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class CaptureSubscriptionAddressController @Inject() (
  val subscriptionConnector:              SubscriptionConnector,
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getData:                                SubscriptionDataRetrievalAction,
  requireData:                            SubscriptionDataRequiredAction,
  formProvider:                           CaptureSubscriptionAddressFormProvider,
  val countryOptions:                     CountryOptions,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   CaptureSubscriptionAddressView
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[NonUKAddress] = formProvider()

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData) { implicit request =>
      val preparedForm =
        request.maybeSubscriptionLocalData.flatMap(_.get(SubRegisteredAddressPage).map(address => form.fill(address))).getOrElse(form)
      Ok(view(preparedForm, countryOptions.options(), request.isAgent, request.maybeSubscriptionLocalData.flatMap(_.organisationName)))
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(view(formWithErrors, countryOptions.options(), request.isAgent, request.subscriptionLocalData.organisationName))
            ),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(request.subscriptionLocalData.set(SubRegisteredAddressPage, value))
              _ <- subscriptionConnector.save(request.userId, Json.toJson(updatedAnswers))

            } yield Redirect(controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad())
        )

    }

}
