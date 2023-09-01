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
import forms.NominateFilingMemberYesNoFormProvider
import models.{Mode, NfmRegistrationConfirmation}
import models.fm.FilingMember
import pages.{NominatedFilingMemberPage, RegistrationPage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import views.html.errors.ErrorTemplate
import views.html.fmview.NominateFilingMemberYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NominateFilingMemberYesNoController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              NominateFilingMemberYesNoFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  page_not_available:        ErrorTemplate,
  view:                      NominateFilingMemberYesNoView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val notAvailable = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")
    val regData      = request.userAnswers.get(RegistrationPage)
    val toDisplay    = regData.fold(false)(data => data.isRegistrationStatus == RowStatus.Completed)
    toDisplay match {
      case true =>
        val preparedForm = request.userAnswers.get(NominatedFilingMemberPage) match {
          case None        => form
          case Some(value) => form.fill(value.nfmConfirmation)
        }

        Ok(view(preparedForm, mode))
      case false => NotFound(notAvailable)
    }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val fmData = request.userAnswers.get(NominatedFilingMemberPage)

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          value match {
            case true =>
              for {
                updatedAnswers <-
                  Future.fromTry(
                    request.userAnswers.set(
                      NominatedFilingMemberPage,
                      fmData.fold(FilingMember(nfmConfirmation = value, isNFMnStatus = RowStatus.InProgress))(data =>
                        data copy (nfmConfirmation = value, isNFMnStatus = RowStatus.InProgress)
                      )
                    )
                  )
                _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(controllers.fm.routes.IsNfmUKBasedController.onPageLoad(mode))
            case false =>
              for {
                updatedAnswers <-
                  Future.fromTry(
                    request.userAnswers.set(NominatedFilingMemberPage, FilingMember(nfmConfirmation = value, isNFMnStatus = RowStatus.Completed))
                  )
                _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(controllers.routes.TaskListController.onPageLoad)
          }
      )
  }
}
