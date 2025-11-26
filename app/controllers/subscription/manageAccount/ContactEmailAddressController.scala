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
import controllers.actions.{IdentifierAction, SubscriptionDataRequiredAction, SubscriptionDataRetrievalAction}
import forms.ContactEmailAddressFormProvider
import navigation.AmendSubscriptionNavigator
import pages.{SubPrimaryContactNamePage, SubPrimaryEmailPage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.manageAccount.ContactEmailAddressView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class ContactEmailAddressController @Inject() (
  val subscriptionConnector:              SubscriptionConnector,
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getData:                                SubscriptionDataRetrievalAction,
  requireData:                            SubscriptionDataRequiredAction,
  navigator:                              AmendSubscriptionNavigator,
  formProvider:                           ContactEmailAddressFormProvider,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   ContactEmailAddressView
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData) { implicit request =>
      request.maybeSubscriptionLocalData
        .flatMap { subscriptionLocalData =>
          subscriptionLocalData
            .get(SubPrimaryContactNamePage)
            .map { contactName =>
              val form = formProvider(contactName)
              Ok(
                view(
                  form.fill(subscriptionLocalData.subPrimaryEmail),
                  contactName,
                  request.isAgent,
                  request.maybeSubscriptionLocalData.flatMap(_.organisationName)
                )
              )
            }
        }
        .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.subscriptionLocalData
        .get(SubPrimaryContactNamePage)
        .map { contactName =>
          val form = formProvider(contactName)
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, contactName, request.isAgent, request.subscriptionLocalData.organisationName))),
              value =>
                for {
                  updatedAnswers <-
                    Future.fromTry(request.subscriptionLocalData.set(SubPrimaryEmailPage, value))
                  _ <- subscriptionConnector.save(request.userId, Json.toJson(updatedAnswers))
                } yield Redirect(navigator.nextPage(SubPrimaryEmailPage, updatedAnswers))
            )
        }
        .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
    }

}
