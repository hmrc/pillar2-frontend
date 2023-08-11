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
import forms.NfmContactNameFormProvider
import models.fm.{FilingMember, WithoutIdNfmData}
import models.requests.DataRequest
import models.{Mode, NfmRegisteredInUkConfirmation, NfmRegistrationConfirmation}
import pages.NominatedFilingMemberPage
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import views.html.errors.ErrorTemplate
import views.html.fmview.NfmContactNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NfmContactNameController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              NfmContactNameFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  page_not_available:        ErrorTemplate,
  view:                      NfmContactNameView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val notAvailable = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")

    isPreviousPageDefined(request) match {
      case true =>
        request.userAnswers
          .get(NominatedFilingMemberPage)
          .fold(NotFound(notAvailable)) { reg =>
            reg.withoutIdRegData.fold(NotFound(notAvailable))(data =>
              data.fmContactName.fold(Ok(view(form, mode)))(contactName => Ok(view(form.fill(contactName), mode)))
            )
          }
      case false => NotFound(notAvailable)
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value => {
          val regData = request.userAnswers.get(NominatedFilingMemberPage).getOrElse(throw new Exception("Is NFM registered in UK not been selected"))
          val regDataWithoutId = regData.withoutIdRegData.getOrElse(throw new Exception("nfmNameRegistration should be available before address"))

          for {
            updatedAnswers <-
              Future.fromTry(
                request.userAnswers
                  .set(NominatedFilingMemberPage, regData.copy(withoutIdRegData = Some(regDataWithoutId.copy(fmContactName = Some(value)))))
              )
            _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
          } yield Redirect(controllers.fm.routes.NfmEmailAddressController.onPageLoad(mode))
        }
      )
  }

  private def isPreviousPageDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(NominatedFilingMemberPage)
      .fold(false)(data => data.withoutIdRegData.fold(false)(withoutId => withoutId.registeredFmName.nonEmpty))
}
