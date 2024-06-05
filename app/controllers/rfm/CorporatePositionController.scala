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
import pages.{RfmCorporatePositionPage, RfmPillar2ReferencePage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.rfm.CorporatePositionView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CorporatePositionController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  rfmIdentify:               RfmIdentifierAction,
  sessionRepository:         SessionRepository,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              RfmCorporatePositionFormProvider,
  navigator:                 ReplaceFilingMemberNavigator,
  val controllerComponents:  MessagesControllerComponents,
  view:                      CorporatePositionView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[CorporatePosition] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (rfmIdentify andThen getData andThen requireData) { implicit request =>
    val rfmAccessEnabled = appConfig.rfmAccessEnabled
    if (rfmAccessEnabled) {
      val preparedForm = request.userAnswers.get(RfmCorporatePositionPage) match {
        case Some(value) => form.fill(value)
        case None        => form
      }
      Ok(view(preparedForm, mode))
    } else {
      Redirect(controllers.routes.UnderConstructionController.onPageLoad)
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (rfmIdentify andThen getData andThen requireData).async { implicit request =>
    sessionRepository.get(request.userId).flatMap { maybeSessionUserAnswers =>
      request.userAnswers
        .get(RfmPillar2ReferencePage)
        .orElse(maybeSessionUserAnswers.flatMap(sessionUserAnswers => sessionUserAnswers.get(RfmPillar2ReferencePage)))
        .map { pillar2Id =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
              corporatePosition =>
                for {
                  updatedAnswers  <- Future.fromTry(request.userAnswers.set(RfmCorporatePositionPage, corporatePosition))
                  updatedAnswers1 <- Future.fromTry(updatedAnswers.set(RfmPillar2ReferencePage, pillar2Id))
                  _               <- userAnswersConnectors.save(updatedAnswers1.id, Json.toJson(updatedAnswers1.data))
                } yield Redirect(navigator.nextPage(RfmCorporatePositionPage, mode, updatedAnswers1))
            )
        }
        .getOrElse(Future.successful(Redirect(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad)))
    }

  }

}
