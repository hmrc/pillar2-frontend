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
import controllers.actions._
import controllers.routes
import forms.SecondaryTelephoneFormProvider
import models.Mode
import models.requests.DataRequest
import models.subscription.Subscription
import pages.SubscriptionPage
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import views.html.errors.ErrorTemplate
import views.html.subscriptionview.SecondaryTelephoneView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SecondaryTelephoneController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              SecondaryTelephoneFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      SecondaryTelephoneView,
  page_not_available:        ErrorTemplate
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val notAvailable         = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")
    val secondaryContactName = getSecondaryContactName(request)
    val form                 = formProvider(secondaryContactName)
    getSecondaryContactName(request) match {
      case "" => NotFound(notAvailable)
      case _ =>
        request.userAnswers
          .get(SubscriptionPage)
          .fold(NotFound(notAvailable))(subs =>
            subs.secondaryContactTelephone.fold(Ok(view(form, mode, secondaryContactName)))(data =>
              Ok(view(form.fill(data), mode, secondaryContactName))
            )
          )
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val secondaryContactName = getSecondaryContactName(request)
    val form                 = formProvider(secondaryContactName)
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          request.userAnswers
            .get(SubscriptionPage)
            .map { sub =>
              val subscriptionData = request.userAnswers
                .get(SubscriptionPage)
                .getOrElse(
                  Subscription(
                    domesticOrMne = sub.domesticOrMne,
                    groupDetailStatus = sub.groupDetailStatus,
                    contactDetailsStatus = sub.contactDetailsStatus,
                    accountingPeriod = sub.accountingPeriod,
                    primaryContactName = sub.primaryContactName,
                    primaryContactEmail = sub.primaryContactEmail,
                    primaryContactTelephone = sub.primaryContactTelephone,
                    useContactPrimary = sub.useContactPrimary,
                    secondaryContactName = Some(secondaryContactName),
                    secondaryContactEmail = sub.secondaryContactEmail,
                    secondaryTelephonePreference = Some(true),
                    secondaryContactTelephone = Some(value)
                  )
                )

              for {
                updatedAnswers <-
                  Future.fromTry(
                    request.userAnswers.set(
                      SubscriptionPage,
                      subscriptionData.copy(secondaryContactTelephone = Some(value), contactDetailsStatus = RowStatus.InProgress)
                    )
                  )
                _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(controllers.subscription.routes.CaptureSubscriptionAddressController.onPageLoad(mode))
            }
            .getOrElse(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))
      )
  }

  def getSecondaryContactName(request: DataRequest[AnyContent]): String =
    request.userAnswers
      .get(SubscriptionPage)
      .fold("")(regData => regData.secondaryContactName.fold("")(name => name))

}
