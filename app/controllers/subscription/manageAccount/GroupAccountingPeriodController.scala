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
import connectors.UserAnswersConnectors
import controllers.actions._
import forms.GroupAccountingPeriodFormProvider
import models.Mode
import models.subscription.AccountingPeriod
import pages.{SubAccountingPeriodPage, SubMneOrDomesticPage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.GroupAccountingPeriodView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GroupAccountingPeriodController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              GroupAccountingPeriodFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      GroupAccountingPeriodView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def form: Form[AccountingPeriod] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    if (request.userAnswers.isPageDefined(SubMneOrDomesticPage)) {
      val preparedForm = request.userAnswers.get(SubAccountingPeriodPage) match {
        case Some(v) => form.fill(v)
        case None    => form
      }
      Ok(view(preparedForm, mode))
    } else {
      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    remapFormErrors(
      form
        .bindFromRequest()
    )
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(SubAccountingPeriodPage, value))
            _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
          } yield Redirect(controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad)
      )
  }

  private def remapFormErrors[A](form: Form[A]): Form[A] =
    form.copy(errors = form.errors.map {
      case e if e.key == "" => e.copy(key = "endDate")
      case e                => e
    })

}
