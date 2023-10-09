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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.CaptureSubscriptionAddressFormProvider
import models.Mode
import models.subscription.SubscriptionAddress
import pages.SubscriptionPage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import utils.countryOptions.CountryOptions
import views.html.subscriptionview.CaptureSubscriptionAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CaptureSubscriptionAddressController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              CaptureSubscriptionAddressFormProvider,
  CountryOptions:            CountryOptions,
  val controllerComponents:  MessagesControllerComponents,
  view:                      CaptureSubscriptionAddressView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {
  val countryList = CountryOptions.options.sortWith((s, t) => s.label(0).toLower < t.label(0).toLower)
  val form: Form[SubscriptionAddress] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers
      .get(SubscriptionPage)
      .map { sub =>
        val preparedForm = sub.correspondenceAddress.fold(form)(data => form fill data)
        Ok(view(preparedForm, mode, countryList))
      }
      .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(SubscriptionPage)
      .map { sub =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, countryList))),
            value =>
              for {
                updatedAnswers <-
                  Future.fromTry(
                    request.userAnswers
                      .set(SubscriptionPage, sub.copy(correspondenceAddress = Some(value), contactDetailsStatus = RowStatus.Completed))
                  )
                _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(controllers.subscription.routes.ContactCheckYourAnswersController.onPageLoad)
          )
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }
}
