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
import forms.SecondaryContactEmailFormProvider
import models.Mode
import models.requests.DataRequest
import models.subscription.Subscription
import pages.SubscriptionPage
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.errors.ErrorTemplate
import views.html.subscriptionview.SecondaryContactEmailView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SecondaryContactEmailController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              SecondaryContactEmailFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      SecondaryContactEmailView,
  page_not_available:        ErrorTemplate
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val notAvailable         = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")
    val secondaryContactName = getSecondaryContactName(request)
    val form                 = formProvider(secondaryContactName)
    isPreviousPageDefined(request) match {
      case true =>
        request.userAnswers
          .get(SubscriptionPage)
          .fold(NotFound(notAvailable))(subs =>
            subs.secondaryContactEmail
              .fold(Ok(view(form, mode, secondaryContactName)))(data => Ok(view(form.fill(data), mode, secondaryContactName)))
          )
      case false => NotFound(notAvailable)
    }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val secondaryContactName = getSecondaryContactName(request)
    val form                 = formProvider(secondaryContactName)
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, secondaryContactName))),
        value =>
          request.userAnswers
            .get(SubscriptionPage)
            .map { subs =>
              val subscriptionData = request.userAnswers
                .get(SubscriptionPage)
                .getOrElse(throw new Exception("no subscription data found for primary contact"))
              for {
                updatedAnswers <-
                  Future.fromTry(request.userAnswers.set(SubscriptionPage, subscriptionData.copy(secondaryContactEmail = Some(value))))
                _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(controllers.subscription.routes.SecondaryTelephonePreferenceController.onPageLoad(mode))
            }
            .getOrElse(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))
      )
  }

  private def getSecondaryContactName(request: DataRequest[AnyContent]): String =
    request.userAnswers
      .get(SubscriptionPage)
      .flatMap { sub =>
        sub.secondaryContactName
      }
      .getOrElse("")

  private def isPreviousPageDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(SubscriptionPage)
      .map { sub =>
        sub.secondaryContactName
      }
      .nonEmpty
}
