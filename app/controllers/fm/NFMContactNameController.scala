/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.fm

import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.NFMContactNameFormProvider
import models.Mode
import pages.NominatedFilingMemberPage
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.NFMContactNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NFMContactNameController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              NFMContactNameFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      NFMContactNameView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(NominatedFilingMemberPage) match {
      case None        => form
      case Some(value) => value.withoutIdRegData.fold(form)(data => data.fmContactName.fold(form)(contactName => form.fill(contactName)))
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value => {
          val regData          = request.userAnswers.get(NominatedFilingMemberPage).getOrElse(throw new Exception("Contact for NFM not selected"))
          val regDataWithoutId = regData.withoutIdRegData.getOrElse(throw new Exception("nFMNameRegistration should be available before address"))
          for {
            updatedAnswers <-
              Future.fromTry(
                request.userAnswers
                  .set(NominatedFilingMemberPage, regData.copy(withoutIdRegData = Some(regDataWithoutId.copy(fmContactName = Some(value)))))
              )
            _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
          } yield Redirect(controllers.fm.routes.NFMEmailAddressController.onPageLoad(mode))
        }
      )
  }
}
