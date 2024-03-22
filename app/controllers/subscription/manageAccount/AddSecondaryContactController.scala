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

import cats.data.OptionT
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions._
import forms.AddSecondaryContactFormProvider
import models.Mode
import navigation.AmendSubscriptionNavigator
import pages.{SubAddSecondaryContactPage, SubPrimaryContactNamePage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ReadSubscriptionService, ReferenceNumberService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.manageAccount.AddSecondaryContactView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddSecondaryContactController @Inject() (
  val userAnswersConnectors:   UserAnswersConnectors,
  identify:                    IdentifierAction,
  getData:                     DataRetrievalAction,
  requireData:                 DataRequiredAction,
  val readSubscriptionService: ReadSubscriptionService,
  referenceNumberService:      ReferenceNumberService,
  formProvider:                AddSecondaryContactFormProvider,
  navigator:                   AmendSubscriptionNavigator,
  val controllerComponents:    MessagesControllerComponents,
  view:                        AddSecondaryContactView
)(implicit ec:                 ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async { implicit request =>
    (for {
      plrReference <- OptionT.fromOption[Future](referenceNumberService.get(None, request.enrolments))
      subData      <- OptionT.liftF(readSubscriptionService.readSubscription(plrReference))
    } yield {
      val preparedForm = request.userAnswers
        .get(SubAddSecondaryContactPage)
        .map(addSecondaryContact => form.fill(addSecondaryContact))
        .getOrElse(if (subData.secondaryContactDetails.isDefined) form.fill(true) else form.fill(false))
      val primaryContactName = request.userAnswers.get(SubPrimaryContactNamePage).getOrElse(subData.primaryContactDetails.name)
      Ok(view(preparedForm, primaryContactName, mode))
    })
      .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    referenceNumberService
      .get(None, request.enrolments)
      .map { plrReference =>
        readSubscriptionService.readSubscription(plrReference).flatMap { subData =>
          val primaryContactName = request.userAnswers.get(SubPrimaryContactNamePage).getOrElse(subData.primaryContactDetails.name)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, primaryContactName, mode))),
              wantsToNominateSecondaryContact =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(SubAddSecondaryContactPage, wantsToNominateSecondaryContact))
                  _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                } yield Redirect(navigator.nextPage(SubAddSecondaryContactPage, mode, updatedAnswers))
            )
        }
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))

  }
}
