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
import forms.AddSecondaryContactFormProvider
import navigation.AmendSubscriptionNavigator
import pages._
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.manageAccount.AddSecondaryContactView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddSecondaryContactController @Inject() (
  val subscriptionConnector: SubscriptionConnector,
  identify:                  IdentifierAction,
  agentIdentifierAction:     AgentIdentifierAction,
  getData:                   SubscriptionDataRetrievalAction,
  requireData:               SubscriptionDataRequiredAction,
  formProvider:              AddSecondaryContactFormProvider,
  navigator:                 AmendSubscriptionNavigator,
  val controllerComponents:  MessagesControllerComponents,
  view:                      AddSecondaryContactView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(clientPillar2Id: Option[String] = None): Action[AnyContent] =
    (identifierAction(clientPillar2Id, agentIdentifierAction, identify) andThen getData) { implicit request =>
      (for {
        subscriptionLocalData <- request.maybeSubscriptionLocalData
        contactName           <- subscriptionLocalData.get(SubPrimaryContactNamePage)
      } yield Ok(view(form.fill(subscriptionLocalData.subAddSecondaryContact), contactName, clientPillar2Id)))
        .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }

  def onSubmit(clientPillar2Id: Option[String] = None): Action[AnyContent] =
    (identifierAction(clientPillar2Id, agentIdentifierAction, identify) andThen getData andThen requireData).async { implicit request =>
      request.subscriptionLocalData
        .get(SubPrimaryContactNamePage)
        .map { contactName =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, contactName, clientPillar2Id))),
              {
                case wantsToNominateSecondaryContact @ true =>
                  for {
                    updatedAnswers <- Future.fromTry(request.subscriptionLocalData.set(SubAddSecondaryContactPage, wantsToNominateSecondaryContact))
                    _              <- subscriptionConnector.save(request.userId, Json.toJson(updatedAnswers))
                  } yield Redirect(navigator.nextPage(SubAddSecondaryContactPage, clientPillar2Id, updatedAnswers))
                case wantsToNominateSecondaryContact @ false =>
                  for {
                    updatedAnswers  <- Future.fromTry(request.subscriptionLocalData.set(SubAddSecondaryContactPage, wantsToNominateSecondaryContact))
                    updatedAnswers1 <- Future.fromTry(updatedAnswers.remove(SubSecondaryContactNamePage))
                    updatedAnswers2 <- Future.fromTry(updatedAnswers1.remove(SubSecondaryEmailPage))
                    updatedAnswers3 <- Future.fromTry(updatedAnswers2.remove(SubSecondaryPhonePreferencePage))
                    updatedAnswers4 <- Future.fromTry(updatedAnswers3.remove(SubSecondaryCapturePhonePage))
                    _               <- subscriptionConnector.save(request.userId, Json.toJson(updatedAnswers4))
                  } yield Redirect(navigator.nextPage(SubAddSecondaryContactPage, clientPillar2Id, updatedAnswers4))
              }
            )
        }
        .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
    }

}
