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

package controllers.registration

import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.CaptureTelephoneDetailsFormProvider
import models.Mode
import pages.RegistrationPage
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import views.html.registrationview.CaptureTelephoneDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CaptureTelephoneDetailsController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              CaptureTelephoneDetailsFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      CaptureTelephoneDetailsView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    (for {
      reg                <- request.userAnswers.get(RegistrationPage)
      noIDData           <- reg.withoutIdRegData
      userName           <- request.userAnswers.upeContactName
      bookmarkPrevention <- request.userAnswers.upeNoIDBookmarkLogic
    } yield {
      val form         = formProvider(userName)
      val preparedForm = noIDData.telephoneNumber.map(form.fill).getOrElse(form)
      Ok(view(preparedForm, mode, userName))
    }).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    (for {
      name      <- request.userAnswers.upeContactName
      reg       <- request.userAnswers.get(RegistrationPage)
      withoutId <- reg.withoutIdRegData
    } yield formProvider(name)
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, name))),
        value =>
          for {
            updatedAnswers <-
              Future.fromTry(
                request.userAnswers.set(
                  RegistrationPage,
                  reg.copy(isRegistrationStatus = RowStatus.Completed, withoutIdRegData = Some(withoutId.copy(telephoneNumber = Some(value))))
                )
              )
            _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
          } yield Redirect(controllers.registration.routes.UpeCheckYourAnswersController.onPageLoad)
      )).getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))

  }

}
