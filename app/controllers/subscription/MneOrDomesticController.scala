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

package controllers.subscription

import cache.SessionData
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions._
import forms.MneOrDomesticFormProvider
import models.Mode
import pages.subMneOrDomesticPage
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import views.html.subscriptionview.MneOrDomesticView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MneOrDomesticController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  sessionRepository:         SessionRepository,
  formProvider:              MneOrDomesticFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      MneOrDomesticView,
  sessionData:               SessionData
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    if (request.userAnswers.fmStatus == RowStatus.Completed) {
      val preparedForm = request.userAnswers.get(subMneOrDomesticPage) match {
        case Some(value) => form.fill(value)
        case None        => form
      }
      Ok(view(preparedForm, mode))
    } else {
      Redirect(controllers.routes.BookmarkPreventionController.onPageLoad)
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <-
              Future
                .fromTry(request.userAnswers.set(subMneOrDomesticPage, value))
            _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(controllers.subscription.routes.GroupAccountingPeriodController.onPageLoad(mode))
            .withSession((sessionData.updateMneOrDomestic(value.toString)))
      )
  }

}
