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
import forms.ContactNameComplianceFormProvider
import models.requests.DataRequest
import models.subscription.Subscription
import models.{Mode, NormalMode}
import pages.SubscriptionPage
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import views.html.errors.ErrorTemplate
import views.html.subscriptionview.ContactNameComplianceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ContactNameComplianceController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              ContactNameComplianceFormProvider,
  page_not_available:        ErrorTemplate,
  val controllerComponents:  MessagesControllerComponents,
  view:                      ContactNameComplianceView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {
  val form = formProvider()

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val notAvailable = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")
    isPreviousPageDefined(request) match {
      case true =>
        request.userAnswers
          .get(SubscriptionPage)
          .fold(NotFound(notAvailable)) { reg =>
            reg.primaryContactName.fold(Ok(view(form, mode)))(data => Ok(view(form.fill(data), mode)))
          }

      case false => NotFound(notAvailable)
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val regData = request.userAnswers.get(SubscriptionPage).getOrElse(throw new Exception("Subscription data not available"))
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <-
              Future
                .fromTry(
                  request.userAnswers.set(
                    SubscriptionPage,
                    Subscription(
                      domesticOrMne = regData.domesticOrMne,
                      useContactPrimary = regData.useContactPrimary,
                      primaryContactEmail = regData.primaryContactEmail,
                      contactByTelephone = regData.contactByTelephone,
                      primaryContactTelephone = regData.primaryContactTelephone,
                      primaryContactName = Some(value),
                      groupDetailStatus = regData.groupDetailStatus,
                      contactDetailsStatus = RowStatus.InProgress
                    )
                  )
                )
            _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
          } yield Redirect(controllers.subscription.routes.ContactEmailAddressController.onPageLoad(NormalMode))
      )
  }

  private def isPreviousPageDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(SubscriptionPage)
      .fold(false)(data => data.useContactPrimary.toString.nonEmpty)

}
