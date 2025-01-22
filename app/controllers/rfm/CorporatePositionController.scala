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
import forms.RfmCorporatePositionFormProvider
import models.Mode
import models.rfm.CorporatePosition
import navigation.ReplaceFilingMemberNavigator
import pages.{RfmCorporatePositionPage, RfmPillar2ReferencePage, RfmRegistrationDatePage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.rfm.CorporatePositionView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class CorporatePositionController @Inject() (
  val userAnswersConnectors:        UserAnswersConnectors,
  @Named("RfmIdentifier") identify: IdentifierAction,
  sessionRepository:                SessionRepository,
  getData:                          DataRetrievalAction,
  requireData:                      DataRequiredAction,
  formProvider:                     RfmCorporatePositionFormProvider,
  navigator:                        ReplaceFilingMemberNavigator,
  val controllerComponents:         MessagesControllerComponents,
  view:                             CorporatePositionView
)(implicit ec:                      ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[CorporatePosition] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(RfmCorporatePositionPage) match {
      case Some(value) => form.fill(value)
      case None        => form
    }
    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    sessionRepository.get(request.userId).flatMap { maybeSessionUserAnswers =>
      (for {
        pillar2Id        <- maybeSessionUserAnswers.map(_.get(RfmPillar2ReferencePage)).getOrElse(request.userAnswers.get(RfmPillar2ReferencePage))
        registrationDate <- maybeSessionUserAnswers.map(_.get(RfmRegistrationDatePage)).getOrElse(request.userAnswers.get(RfmRegistrationDatePage))
      } yield form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          corporatePosition =>
            for {
              updatedAnswers  <- Future.fromTry(request.userAnswers.set(RfmCorporatePositionPage, corporatePosition))
              updatedAnswers1 <- Future.fromTry(updatedAnswers.set(RfmPillar2ReferencePage, pillar2Id))
              updatedAnswers2 <- Future.fromTry(updatedAnswers1.set(RfmRegistrationDatePage, registrationDate))
              _               <- userAnswersConnectors.save(updatedAnswers2.id, Json.toJson(updatedAnswers2.data))
            } yield Redirect(navigator.nextPage(RfmCorporatePositionPage, mode, updatedAnswers2))
        ))
        .getOrElse(Future.successful(Redirect(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad)))
    }

  }

}
