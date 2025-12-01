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
import forms.AddSecondaryContactFormProvider
import navigation.AmendSubscriptionNavigator
import pages.*
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.manageAccount.AddSecondaryContactView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class AddSecondaryContactController @Inject() (
  val subscriptionConnector:              SubscriptionConnector,
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getData:                                SubscriptionDataRetrievalAction,
  requireData:                            SubscriptionDataRequiredAction,
  formProvider:                           AddSecondaryContactFormProvider,
  navigator:                              AmendSubscriptionNavigator,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   AddSecondaryContactView
)(using ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData) { request =>
      given Request[AnyContent] = request
      (for {
        subscriptionLocalData <- request.maybeSubscriptionLocalData
        contactName           <- subscriptionLocalData.get(SubPrimaryContactNamePage)
      } yield {
        val form: Form[Boolean] = formProvider(contactName)
        Ok(
          view(
            form.fill(subscriptionLocalData.subAddSecondaryContact),
            contactName,
            request.isAgent,
            request.maybeSubscriptionLocalData.flatMap(_.organisationName)
          )
        )
      })
        .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { request =>
      given Request[AnyContent] = request
      request.subscriptionLocalData
        .get(SubPrimaryContactNamePage)
        .map { contactName =>
          val form: Form[Boolean] = formProvider(contactName)
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, contactName, request.isAgent, request.subscriptionLocalData.organisationName))),
              {
                case wantsToNominateSecondaryContact @ true =>
                  for {
                    updatedAnswers <- Future.fromTry(request.subscriptionLocalData.set(SubAddSecondaryContactPage, wantsToNominateSecondaryContact))
                    _              <- subscriptionConnector.save(request.userId, Json.toJson(updatedAnswers))
                  } yield Redirect(navigator.nextPage(SubAddSecondaryContactPage, updatedAnswers))
                case wantsToNominateSecondaryContact @ false =>
                  for {
                    updatedAnswers  <- Future.fromTry(request.subscriptionLocalData.set(SubAddSecondaryContactPage, wantsToNominateSecondaryContact))
                    updatedAnswers1 <- Future.fromTry(updatedAnswers.removeIfExists(SubSecondaryContactNamePage))
                    updatedAnswers2 <- Future.fromTry(updatedAnswers1.removeIfExists(SubSecondaryEmailPage))
                    updatedAnswers3 <- Future.fromTry(updatedAnswers2.removeIfExists(SubSecondaryPhonePreferencePage))
                    updatedAnswers4 <- Future.fromTry(updatedAnswers3.removeIfExists(SubSecondaryCapturePhonePage))
                    _               <- subscriptionConnector.save(request.userId, Json.toJson(updatedAnswers4))
                  } yield Redirect(navigator.nextPage(SubAddSecondaryContactPage, updatedAnswers4))
              }
            )
        }
        .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
    }

}
