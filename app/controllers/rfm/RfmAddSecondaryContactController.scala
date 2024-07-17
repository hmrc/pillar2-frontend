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

package controllers.rfm

import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions._
import forms.RfmAddSecondaryContactFormProvider
import models.Mode
import navigation.ReplaceFilingMemberNavigator
import pages.{RfmAddSecondaryContactPage, RfmPrimaryContactNamePage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.rfm.RfmAddSecondaryContactView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class RfmAddSecondaryContactController @Inject() (
  val userAnswersConnectors:        UserAnswersConnectors,
  @Named("RfmIdentifier") identify: IdentifierAction,
  getData:                          DataRetrievalAction,
  requireData:                      DataRequiredAction,
  navigator:                        ReplaceFilingMemberNavigator,
  formProvider:                     RfmAddSecondaryContactFormProvider,
  val controllerComponents:         MessagesControllerComponents,
  view:                             RfmAddSecondaryContactView
)(implicit ec:                      ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val rfmAccessEnabled = appConfig.rfmAccessEnabled
    if (rfmAccessEnabled) {
      request.userAnswers
        .get(RfmPrimaryContactNamePage)
        .map { contactName =>
          val preparedForm = request.userAnswers.get(RfmAddSecondaryContactPage).map(form.fill).getOrElse(form)
          Ok(view(preparedForm, contactName, mode))
        }
        .getOrElse(Redirect(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad))
    } else {
      Redirect(controllers.routes.UnderConstructionController.onPageLoad)
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(RfmPrimaryContactNamePage)
      .map { contactName =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, contactName, mode))),
            wantsToNominateRfmSecondaryContact =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(RfmAddSecondaryContactPage, wantsToNominateRfmSecondaryContact))
                _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(navigator.nextPage(RfmAddSecondaryContactPage, mode, updatedAnswers))
          )
      }
      .getOrElse(Future.successful(Redirect(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad)))
  }
}
