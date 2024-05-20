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
import forms.SecondaryTelephoneFormProvider
import models.Mode
import navigation.AmendSubscriptionNavigator
import pages.{SubSecondaryCapturePhonePage, SubSecondaryContactNamePage, SubSecondaryPhonePreferencePage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.manageAccount.SecondaryTelephoneView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SecondaryTelephoneController @Inject() (
  val subscriptionConnector: SubscriptionConnector,
  identify:                  IdentifierAction,
  agentIdentifierAction:     AgentIdentifierAction,
  getData:                   SubscriptionDataRetrievalAction,
  requireData:               SubscriptionDataRequiredAction,
  navigator:                 AmendSubscriptionNavigator,
  formProvider:              SecondaryTelephoneFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      SecondaryTelephoneView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(clientPillar2Id: Option[String] = None): Action[AnyContent] =
    (identifierAction(clientPillar2Id, agentIdentifierAction, identify) andThen getData andThen requireData) { implicit request =>
      (for {
        contactName <- request.subscriptionLocalData.get(SubSecondaryContactNamePage)
        _           <- request.subscriptionLocalData.get(SubSecondaryPhonePreferencePage)
      } yield {
        val form = formProvider(contactName)
        val preparedForm = request.subscriptionLocalData.get(SubSecondaryCapturePhonePage) match {
          case Some(v) => form.fill(v)
          case None    => form
        }
        Ok(view(preparedForm, clientPillar2Id, contactName))

      })
        .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

    }

  def onSubmit(clientPillar2Id: Option[String] = None): Action[AnyContent] =
    (identifierAction(clientPillar2Id, agentIdentifierAction, identify) andThen getData andThen requireData).async { implicit request =>
      request.subscriptionLocalData
        .get(SubSecondaryContactNamePage)
        .map { contactName =>
          val form = formProvider(contactName)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, clientPillar2Id, contactName))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.subscriptionLocalData.set(SubSecondaryCapturePhonePage, value))
                  _              <- subscriptionConnector.save(request.userId, Json.toJson(updatedAnswers))
                } yield Redirect(navigator.nextPage(SubSecondaryCapturePhonePage, clientPillar2Id, updatedAnswers))
            )
        }
        .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
    }

}
