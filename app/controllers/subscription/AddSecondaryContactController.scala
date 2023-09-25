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
import forms.AddSecondaryContactFormProvider
import models.Mode
import models.requests.DataRequest
import models.subscription.Subscription
import pages.{NominatedFilingMemberPage, SubscriptionPage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import views.html.errors.ErrorTemplate
import views.html.subscriptionview.AddSecondaryContactView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddSecondaryContactController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              AddSecondaryContactFormProvider,
  page_not_available:        ErrorTemplate,
  val controllerComponents:  MessagesControllerComponents,
  view:                      AddSecondaryContactView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val notAvailable = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")
    val nfmData      = request.userAnswers.get(SubscriptionPage).fold(false)(data => data.groupDetailStatus == RowStatus.Completed)
    val contactName  = getContactName(request)
    nfmData match {

      case true =>
        request.userAnswers
          .get(SubscriptionPage)
          .fold(NotFound(notAvailable))(subs =>
            subs.addSecondaryContact
              .fold(Ok(view(form, contactName, mode)))(data => Ok(view(form.fill(data), contactName, mode)))
          )
      case false => NotFound(notAvailable)
    }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val contactName = getContactName(request)
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, contactName, mode))),
        value =>
          value match {
            case true =>
              request.userAnswers
                .get(SubscriptionPage)
                .map { sub =>
                  val subsData =
                    request.userAnswers.get(SubscriptionPage).getOrElse(throw new Exception("Primary details not available"))
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(SubscriptionPage, subsData.copy(addSecondaryContact = Some(value))))
                    _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                  } yield Redirect(controllers.subscription.routes.SecondaryContactNameController.onPageLoad(mode))
                }
                .getOrElse(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))

            case false =>
              request.userAnswers
                .get(SubscriptionPage)
                .map { sub =>
                  val subsData =
                    request.userAnswers.get(SubscriptionPage).getOrElse(throw new Exception("Primary details not available"))
                  for {
                    updatedAnswers <-
                      Future.fromTry(request.userAnswers.set(SubscriptionPage, subsData.copy(addSecondaryContact = Some(value))))
                    _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                  } yield Redirect(routes.UnderConstructionController.onPageLoad)
                }
                .getOrElse(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))
          }
      )
  }

  private def getContactName(request: DataRequest[AnyContent]): String =
    request.userAnswers
      .get(SubscriptionPage)
      .flatMap { subs =>
        subs.primaryContactName
      }
      .getOrElse("")

}
