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
import controllers.actions.*
import forms.GroupAccountingPeriodFormProvider
import models.subscription.AccountingPeriod
import navigation.AmendSubscriptionNavigator
import pages.SubAccountingPeriodPage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.manageAccount.GroupAccountingPeriodView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class GroupAccountingPeriodController @Inject() (
  val subscriptionConnector:              SubscriptionConnector,
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getData:                                SubscriptionDataRetrievalAction,
  requireData:                            SubscriptionDataRequiredAction,
  navigator:                              AmendSubscriptionNavigator,
  formProvider:                           GroupAccountingPeriodFormProvider,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   GroupAccountingPeriodView
)(using ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def form: Form[AccountingPeriod] = formProvider(true)

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData) { request =>
      given Request[AnyContent] = request
      val preparedForm          = request.maybeSubscriptionLocalData.flatMap(_.get(SubAccountingPeriodPage)) match {
        case Some(v) => form.fill(v)
        case None    => form
      }
      Ok(view(preparedForm, request.isAgent, request.maybeSubscriptionLocalData.flatMap(_.organisationName)))
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { request =>
      given Request[AnyContent] = request
      remapFormErrors(
        form
          .bindFromRequest()
      )
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.isAgent, request.subscriptionLocalData.organisationName))),
          value =>
            val oldPeriod = request.subscriptionLocalData.get(SubAccountingPeriodPage)
            for {
              updatedAnswers <- Future.fromTry(request.subscriptionLocalData.set(SubAccountingPeriodPage, value))
              withUpdatedPeriods = oldPeriod.fold(updatedAnswers) { old =>
                                     val updatedPeriods = updatedAnswers.accountingPeriods.map { periods =>
                                       periods.map { p =>
                                         if p.startDate == old.startDate && p.endDate == old.endDate then
                                           p.copy(startDate = value.startDate, endDate = value.endDate)
                                         else p
                                       }
                                     }
                                     updatedAnswers.copy(accountingPeriods = updatedPeriods)
                                   }
              _ <- subscriptionConnector.save(request.userId, Json.toJson(withUpdatedPeriods))
            } yield Redirect(navigator.nextPage(SubAccountingPeriodPage, withUpdatedPeriods))
        )
    }

  private def remapFormErrors[A](form: Form[A]): Form[A] =
    form.copy(errors = form.errors.map {
      case e if e.key == "" => e.copy(key = "endDate")
      case e                => e
    })

}
