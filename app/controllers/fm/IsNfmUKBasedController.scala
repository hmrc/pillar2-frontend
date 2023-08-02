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
import controllers.actions._
import controllers.routes
import forms.IsNFMUKBasedFormProvider
import models.{Mode, NfmRegisteredInUkConfirmation, NfmRegistrationConfirmation}
import models.fm.FilingMember
import pages.NominatedFilingMemberPage
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import views.html.fmview.IsNFMUKBasedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsNfmUKBasedController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              IsNFMUKBasedFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      IsNFMUKBasedView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(NominatedFilingMemberPage) match {
      case None        => form
      case Some(value) => value.isNfmRegisteredInUK.fold(form)(regInUk => form.fill(regInUk))
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val fmData = request.userAnswers.get(NominatedFilingMemberPage)
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          value match {
            case NfmRegisteredInUkConfirmation.No =>
              for {
                updatedAnswers <-
                  Future
                    .fromTry(
                      request.userAnswers
                        .set(
                          NominatedFilingMemberPage,
                          fmData.fold(FilingMember(nfmConfirmation = NfmRegistrationConfirmation.Yes, isNFMnStatus = RowStatus.InProgress))(data =>
                            data.copy(NfmRegistrationConfirmation.Yes, Some(value), isNFMnStatus = RowStatus.InProgress)
                          )
                        )
                    )
                _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(controllers.fm.routes.NfmNameRegistrationController.onPageLoad(mode))
            case NfmRegisteredInUkConfirmation.Yes =>
              for {
                updatedAnswers <-
                  Future
                    .fromTry(
                      request.userAnswers
                        .set(
                          NominatedFilingMemberPage,
                          FilingMember(NfmRegistrationConfirmation.No, Some(value), isNFMnStatus = RowStatus.InProgress)
                        )
                    )
                _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(routes.UnderConstructionController.onPageLoad)

          }
      )
  }
}
