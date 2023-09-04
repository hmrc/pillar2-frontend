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
import forms.NfmCaptureTelephoneDetailsFormProvider
import models.Mode
import models.fm.ContactNFMByTelephone
import pages.{NfmCaptureTelephoneDetailsPage, NominatedFilingMemberPage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.fmview.NfmCaptureTelephoneDetailsView
import models.requests.DataRequest
import utils.RowStatus
import views.html.errors.ErrorTemplate

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NfmCaptureTelephoneDetailsController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              NfmCaptureTelephoneDetailsFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  page_not_available:        ErrorTemplate,
  view:                      NfmCaptureTelephoneDetailsView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val userName     = getUserName(request)
    val form         = formProvider(userName)
    val notAvailable = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")
    isPreviousPageDefined(request) match {
      case true =>
        request.userAnswers
          .get(NominatedFilingMemberPage)
          .fold(NotFound(notAvailable)) { reg =>
            reg.withoutIdRegData.fold(NotFound(notAvailable))(data =>
              data.telephoneNumber.fold(Ok(view(form, mode, userName)))(tel => Ok(view(form.fill(tel), mode, userName)))
            )
          }

      case false => NotFound(notAvailable)
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val userName = getUserName(request)
    val form     = formProvider(userName)
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, userName))),
        value => {
          val fmRegData =
            request.userAnswers.get(NominatedFilingMemberPage).getOrElse(throw new Exception("Is NFM registered in UK not been selected"))
          val regDataWithoutId =
            fmRegData.withoutIdRegData.getOrElse(throw new Exception("fmName, address & email should be available before email"))
          for {
            updatedAnswers <-
              Future.fromTry(
                request.userAnswers.set(
                  NominatedFilingMemberPage,
                  fmRegData
                    .copy(isNFMnStatus = RowStatus.Completed, withoutIdRegData = Some(regDataWithoutId.copy(telephoneNumber = Some(value))))
                )
              )
            _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
          } yield Redirect(controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad)
        }
      )
  }

  private def getUserName(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(NominatedFilingMemberPage)
    registration.fold("")(regData => regData.withoutIdRegData.fold("")(withoutId => withoutId.fmContactName.fold("")(name => name)))
  }

  private def isPreviousPageDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(NominatedFilingMemberPage)
      .fold(false)(data => data.withoutIdRegData.fold(false)(withoutId => withoutId.contactNfmByTelephone.fold(false)(contactTel => contactTel)))
}
