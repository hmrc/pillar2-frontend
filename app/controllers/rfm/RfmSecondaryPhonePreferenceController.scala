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
import controllers.actions.*
import forms.RfmSecondaryPhonePreferenceFormProvider
import models.Mode
import navigation.ReplaceFilingMemberNavigator
import pages.{RfmSecondaryContactNamePage, RfmSecondaryPhonePreferencePage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.rfm.RfmSecondaryPhonePreferenceView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class RfmSecondaryPhonePreferenceController @Inject() (
  val userAnswersConnectors:        UserAnswersConnectors,
  @Named("RfmIdentifier") identify: IdentifierAction,
  getData:                          DataRetrievalAction,
  requireData:                      DataRequiredAction,
  journeyGuard:                     RfmDataJourneyGuardAction,
  navigator:                        ReplaceFilingMemberNavigator,
  formProvider:                     RfmSecondaryPhonePreferenceFormProvider,
  val controllerComponents:         MessagesControllerComponents,
  view:                             RfmSecondaryPhonePreferenceView
)(using ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen journeyGuard) { request =>
    given Request[AnyContent] = request
    request.userAnswers
      .get(RfmSecondaryContactNamePage)
      .map { contactName =>
        val form         = formProvider(contactName)
        val preparedForm = request.userAnswers.get(RfmSecondaryPhonePreferencePage).map(form.fill).getOrElse(form)
        Ok(view(preparedForm, mode, contactName))
      }
      .getOrElse(Redirect(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { request =>
    given Request[AnyContent] = request
    request.userAnswers
      .get(RfmSecondaryContactNamePage)
      .map { contactName =>
        val form = formProvider(contactName)
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, contactName))),
            nominatedSecondaryContactNumber =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(RfmSecondaryPhonePreferencePage, nominatedSecondaryContactNumber))
                _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(navigator.nextPage(RfmSecondaryPhonePreferencePage, mode, updatedAnswers))
          )
      }
      .getOrElse(Future.successful(Redirect(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad)))
  }
}
