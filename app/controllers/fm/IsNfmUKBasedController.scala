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
import models.requests.DataRequest
import pages.{NominatedFilingMemberPage, RegistrationPage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import views.html.fmview.IsNFMUKBasedView
import views.html.errors.ErrorTemplate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsNfmUKBasedController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              IsNFMUKBasedFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  page_not_available:        ErrorTemplate,
  view:                      IsNFMUKBasedView
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
            reg.isNfmRegisteredInUK.fold(Ok(view(form, mode)))(data => Ok(view(form.fill(data), mode)))
          }
      case false =>
        NotFound(notAvailable)
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          value match {
            case NfmRegisteredInUkConfirmation.Yes =>
              val regData =
                request.userAnswers
                  .get(NominatedFilingMemberPage)
                  .getOrElse(FilingMember(NfmRegistrationConfirmation.Yes, isNfmRegisteredInUK = Some(value), isNFMnStatus = RowStatus.InProgress))

              for {
                updatedAnswers <-
                  Future
                    .fromTry(
                      request.userAnswers
                        .set(
                          NominatedFilingMemberPage,
                          regData.copy(NfmRegistrationConfirmation.Yes, isNfmRegisteredInUK = Some(value))
                        )
                    )
                _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))

              } yield Redirect(controllers.fm.routes.NfmEntityTypeController.onPageLoad(mode))

            case NfmRegisteredInUkConfirmation.No =>
              val regData =
                request.userAnswers
                  .get(NominatedFilingMemberPage)
                  .getOrElse(FilingMember(NfmRegistrationConfirmation.Yes, isNfmRegisteredInUK = Some(value), isNFMnStatus = RowStatus.InProgress))

              val checkedRegData =
                regData.withIdRegData.fold(regData)(_ =>
                  FilingMember(NfmRegistrationConfirmation.Yes, isNfmRegisteredInUK = Some(value), isNFMnStatus = RowStatus.InProgress)
                )
              for {
                updatedAnswers <-
                  Future.fromTry(
                    request.userAnswers.set(
                      NominatedFilingMemberPage,
                      checkedRegData.copy(NfmRegistrationConfirmation.Yes, isNfmRegisteredInUK = Some(value), orgType = None, withIdRegData = None)
                    )
                  )
                _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(routes.UnderConstructionController.onPageLoad)

          }
      )
  }

  private def isPreviousPageDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(NominatedFilingMemberPage)
      .fold(false)(data => data.nfmConfirmation == NfmRegistrationConfirmation.Yes)

}
