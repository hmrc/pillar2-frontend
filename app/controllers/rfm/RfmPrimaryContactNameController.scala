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
import forms.RfmPrimaryContactNameFormProvider
import models.{Mode, NormalMode}
import navigation.ReplaceFilingMemberNavigator
import pages.RfmPrimaryContactNamePage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.rfm.RfmPrimaryContactNameView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class RfmPrimaryContactNameController @Inject() (
  val userAnswersConnectors:        UserAnswersConnectors,
  @Named("RfmIdentifier") identify: IdentifierAction,
  getData:                          DataRetrievalAction,
  requireData:                      DataRequiredAction,
  journeyGuard:                     RfmDataJourneyGuardAction,
  formProvider:                     RfmPrimaryContactNameFormProvider,
  val controllerComponents:         MessagesControllerComponents,
  view:                             RfmPrimaryContactNameView,
  navigator:                        ReplaceFilingMemberNavigator
)(using ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = (identify andThen getData andThen requireData andThen journeyGuard) { request =>
    given Request[AnyContent] = request
    val preparedForm          = request.userAnswers.get(RfmPrimaryContactNamePage) match {
      case Some(v) => form.fill(v)
      case None    => form
    }
    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { request =>
    given Request[AnyContent] = request
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <-
              Future
                .fromTry(request.userAnswers.set(RfmPrimaryContactNamePage, value))
            _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
          } yield Redirect(navigator.nextPage(RfmPrimaryContactNamePage, mode, updatedAnswers))
      )
  }

}
