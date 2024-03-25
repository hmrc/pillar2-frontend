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
import forms.SecondaryTelephoneFormProvider
import models.Mode
import navigation.AmendSubscriptionNavigator
import pages.{SubSecondaryCapturePhonePage, SubSecondaryContactNamePage, SubSecondaryPhonePreferencePage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{ReadSubscriptionService, ReferenceNumberService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.manageAccount.SecondaryTelephoneView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SecondaryTelephoneController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  navigator:                 AmendSubscriptionNavigator,
  val readSubscriptionService: ReadSubscriptionService,
  referenceNumberService:      ReferenceNumberService,
  formProvider:              SecondaryTelephoneFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      SecondaryTelephoneView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async { implicit request =>
    (for {
      plrReference <- OptionT.fromOption[Future](referenceNumberService.get(None, request.enrolments))
      subData      <- OptionT.liftF(readSubscriptionService.readSubscription(plrReference))
      secondaryContactName <- OptionT.fromOption[Future](request.userAnswers.get(SubSecondaryContactNamePage)) orElse
        OptionT.fromOption[Future](subData.secondaryContactDetails.map(_.name))
    } yield {
      val form           = formProvider(secondaryContactName)
      val existingAnswer = request.userAnswers.get(SubSecondaryCapturePhonePage) orElse subData.secondaryContactDetails.flatMap(_.telephone)
      val preparedForm   = existingAnswer.map(form.fill).getOrElse(form)
      Ok(view(preparedForm, mode, secondaryContactName))
    }).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(SubSecondaryContactNamePage)
      .map { contactName =>
        val form = formProvider(contactName)
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, contactName))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(SubSecondaryCapturePhonePage, value))
                _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(navigator.nextPage(SubSecondaryCapturePhonePage, mode, updatedAnswers))
          )
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }

}
