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
import controllers.actions._
import forms.GroupAccountingPeriodFormProvider
import models.subscription.AccountingPeriod
import navigation.AmendSubscriptionNavigator
import pages.SubAccountingPeriodPage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.manageAccount.GroupAccountingPeriodView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GroupAccountingPeriodController @Inject() (
  val subscriptionConnector: SubscriptionConnector,
  identify:                  EnrolmentIdentifierAction,
  getData:                   SubscriptionDataRetrievalAction,
  requireData:               SubscriptionDataRequiredAction,
  navigator:                 AmendSubscriptionNavigator,
  formProvider:              GroupAccountingPeriodFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      GroupAccountingPeriodView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def form: Form[AccountingPeriod] = formProvider()

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData) { implicit request =>
      val preparedForm = request.maybeSubscriptionLocalData.flatMap(_.get(SubAccountingPeriodPage)) match {
        case Some(v) => form.fill(v)
        case None    => form
      }
      Ok(view(preparedForm))
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      remapFormErrors(
        form
          .bindFromRequest()
      )
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.subscriptionLocalData.set(SubAccountingPeriodPage, value))
              _              <- subscriptionConnector.save(request.userId, Json.toJson(updatedAnswers))
            } yield Redirect(navigator.nextPage(SubAccountingPeriodPage, updatedAnswers))
        )
    }

  private def remapFormErrors[A](form: Form[A]): Form[A] =
    form.copy(errors = form.errors.map {
      case e if e.key == "" => e.copy(key = "endDate")
      case e                => e
    })

}
