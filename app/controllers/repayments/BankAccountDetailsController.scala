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

package controllers.repayments

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions._
import forms.BankAccountDetailsFormProvider
import models.Mode
import models.repayments.BankAccountDetails
import navigation.RepaymentNavigator
import pages.BankAccountDetailsPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.repayments.BankAccountDetailsView

import scala.concurrent.{ExecutionContext, Future}

class BankAccountDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  identify:                 AmendIdentifierAction,
  getSessionData:           SessionDataRetrievalAction,
  requireSessionData:       SessionDataRequiredAction,
  sessionRepository:        SessionRepository,
  formProvider:             BankAccountDetailsFormProvider,
  featureAction:            FeatureFlagActionFactory,
  navigator:                RepaymentNavigator,
  val controllerComponents: MessagesControllerComponents,
  view:                     BankAccountDetailsView
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form: Form[BankAccountDetails] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (featureAction.repaymentsAccessAction andThen identify andThen getSessionData andThen requireSessionData) { implicit request =>
      val preparedForm = request.userAnswers.get(BankAccountDetailsPage) match {
        case None              => form
        case Some(userAnswers) => form.fill(userAnswers)
      }
      Ok(view(preparedForm, mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getSessionData andThen requireSessionData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(BankAccountDetailsPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(BankAccountDetailsPage, mode, updatedAnswers))
        )
    }
}
