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
import forms.MneOrDomesticFormProvider
import models.EntityLocationChangeResult.{EntityLocationChangeAllowed, EntityLocationChangeBlocked}
import models.MneOrDomestic
import navigation.AmendSubscriptionNavigator
import pages.SubMneOrDomesticPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.manageAccount.MneOrDomesticView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class MneOrDomesticController @Inject() (
  val subscriptionConnector:                      SubscriptionConnector,
  @Named("EnrolmentIdentifier") identifierAction: IdentifierAction,
  subscriptionDataRetrievalAction:                SubscriptionDataRetrievalAction,
  subscriptionDataRequiredAction:                 SubscriptionDataRequiredAction,
  navigator:                                      AmendSubscriptionNavigator,
  mneOrDomesticFormProvider:                      MneOrDomesticFormProvider,
  val controllerComponents:                       MessagesControllerComponents,
  mneOrDomesticView:                              MneOrDomesticView
)(implicit ec:                                    ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val mneOrDomesticForm: Form[MneOrDomestic] = mneOrDomesticFormProvider()

  def onPageLoad(): Action[AnyContent] =
    (identifierAction andThen subscriptionDataRetrievalAction) { implicit request =>
      val preparedForm = request.maybeSubscriptionLocalData.flatMap(_.get(SubMneOrDomesticPage)) match {
        case Some(mneOrDomestic) => mneOrDomesticForm.fill(mneOrDomestic)
        case None                => mneOrDomesticForm
      }
      Ok(mneOrDomesticView(preparedForm, request.isAgent, request.maybeSubscriptionLocalData.flatMap(_.organisationName)))
    }

  def onSubmit(): Action[AnyContent] =
    (identifierAction andThen subscriptionDataRetrievalAction andThen subscriptionDataRequiredAction).async { implicit request =>
      mneOrDomesticForm
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(mneOrDomesticView(formWithErrors, request.isAgent, request.subscriptionLocalData.organisationName))),
          newMneOrDomesticValue =>
            MneOrDomestic.handleEntityLocationChange(from = request.subscriptionLocalData.subMneOrDomestic, to = newMneOrDomesticValue) match {
              case EntityLocationChangeAllowed =>
                logger.info(s"Allowed entity location change from ${request.subscriptionLocalData.subMneOrDomestic} to $newMneOrDomesticValue")
                for {
                  updatedAnswers <- Future.fromTry(request.subscriptionLocalData.set(SubMneOrDomesticPage, newMneOrDomesticValue))
                  _              <- subscriptionConnector.save(request.userId, Json.toJson(updatedAnswers))
                } yield Redirect(navigator.nextPage(SubMneOrDomesticPage, updatedAnswers))
              case EntityLocationChangeBlocked =>
                logger.info(s"Blocked entity location change from ${request.subscriptionLocalData.subMneOrDomestic} to $newMneOrDomesticValue")
                Future.successful(Redirect(controllers.subscription.manageAccount.routes.MneToDomesticController.onPageLoad))
            }
        )
    }

}
