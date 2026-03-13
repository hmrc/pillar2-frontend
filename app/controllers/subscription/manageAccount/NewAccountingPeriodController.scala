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
import controllers.deriveNewAccountingPeriodDateBoundaries
import forms.NewAccountingPeriodFormProvider
import models.subscription.{AccountingPeriod, AccountingPeriodV2, ChosenAccountingPeriod}
import models.{Mode, UserAnswers}
import pages.{NewAccountingPeriodPage, SubAccountingPeriodPage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.*
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.manageAccount.NewAccountingPeriodView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class NewAccountingPeriodController @Inject() (
  val subscriptionConnector:              SubscriptionConnector,
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  sessionRepository:                      SessionRepository,
  getData:                                SubscriptionDataRetrievalAction,
  requireData:                            SubscriptionDataRequiredAction,
  checkAmendMultipleAPScreens:            AmendMultipleAccountingPeriodScreensAction,
  formProvider:                           NewAccountingPeriodFormProvider,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   NewAccountingPeriodView
)(using ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def form(chosenAccountingPeriod: ChosenAccountingPeriod): Form[AccountingPeriod] =
    formProvider(chosenAccountingPeriod)

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen checkAmendMultipleAPScreens andThen getData andThen requireData).async { request =>
      given Request[AnyContent] = request

      val accountingPeriods:        Option[Seq[AccountingPeriodV2]] = request.subscriptionLocalData.accountingPeriods
      val selectedAccountingPeriod: Option[AccountingPeriod]        = request.subscriptionLocalData.get(SubAccountingPeriodPage)

      (accountingPeriods, selectedAccountingPeriod) match {
        case (Some(periods), Some(selectedPeriod)) =>
          val chosenAccountingPeriod: ChosenAccountingPeriod = deriveNewAccountingPeriodDateBoundaries(periods, selectedPeriod)

          sessionRepository.get(request.userId).map { maybeUserAnswers =>
            val preparedForm =
              maybeUserAnswers
                .flatMap(_.get(NewAccountingPeriodPage))
                .map(form(chosenAccountingPeriod).fill)
                .getOrElse(form(chosenAccountingPeriod))

            Ok(
              view(
                preparedForm,
                chosenAccountingPeriod,
                request.isAgent,
                request.subscriptionLocalData.organisationName,
                request.subscriptionLocalData.plrReference,
                mode
              )
            )
          }
        case _ => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen checkAmendMultipleAPScreens andThen getData andThen requireData).async { request =>
      given Request[AnyContent] = request

      val accountingPeriods:        Option[Seq[AccountingPeriodV2]] = request.subscriptionLocalData.accountingPeriods
      val selectedAccountingPeriod: Option[AccountingPeriod]        = request.subscriptionLocalData.get(SubAccountingPeriodPage)

      (accountingPeriods, selectedAccountingPeriod) match {
        case (Some(periods), Some(selectedPeriod)) =>
          val chosenAccountingPeriod: ChosenAccountingPeriod = deriveNewAccountingPeriodDateBoundaries(periods, selectedPeriod)

          remapFormErrors(
            form(chosenAccountingPeriod)
              .bindFromRequest()
          )
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(
                      formWithErrors,
                      chosenAccountingPeriod,
                      request.isAgent,
                      request.subscriptionLocalData.organisationName,
                      request.subscriptionLocalData.plrReference,
                      mode
                    )
                  )
                ),
              value =>
                for {
                  maybeUserAnswers <- sessionRepository.get(request.userId)
                  updatedAnswers   <- Future.fromTry(
                                      maybeUserAnswers.getOrElse(UserAnswers(request.userId)).set(NewAccountingPeriodPage, value)
                                    )
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(Call("GET", "#")) // TODO: Update to check your answers page
            )

        case _ => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  private def remapFormErrors[A](form: Form[A]): Form[A] =
    form.copy(errors = form.errors.map {
      case e if e.key == "" => e.copy(key = "endDate")
      case e                => e
    })

}
