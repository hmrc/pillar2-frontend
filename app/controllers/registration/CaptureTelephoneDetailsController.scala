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
import models.{ContactUPEByTelephone, Mode}
import models.requests.DataRequest
import navigation.Navigator
import pages.RegistrationPage
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import views.html.errors.ErrorTemplate
import views.html.registrationview.CaptureTelephoneDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CaptureTelephoneDetailsController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  navigator:                 Navigator,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              CaptureTelephoneDetailsFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  page_not_available:        ErrorTemplate,
  view:                      CaptureTelephoneDetailsView
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
          .get(RegistrationPage)
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
          val regData = request.userAnswers.get(RegistrationPage).getOrElse(throw new Exception("Is UPE registered in UK not been selected"))
          val regDataWithoutId =
            regData.withoutIdRegData.getOrElse(throw new Exception("upeNameRegistration, address & email should be available before email"))
          for {
            updatedAnswers <-
              Future.fromTry(
                request.userAnswers.set(
                  RegistrationPage,
                  regData
                    .copy(isRegistrationStatus = RowStatus.Completed, withoutIdRegData = Some(regDataWithoutId.copy(telephoneNumber = Some(value))))
                )
              )
            _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
          } yield Redirect(controllers.registration.routes.UpeCheckYourAnswersController.onPageLoad)
        }
      )
  }

  private def getUserName(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(RegistrationPage)
    registration.fold("")(regData => regData.withoutIdRegData.fold("")(withoutId => withoutId.upeContactName.fold("")(name => name)))
  }

  private def isPreviousPageDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(RegistrationPage)
      .fold(false)(data =>
        data.withoutIdRegData.fold(false)(withoutId =>
          withoutId.contactUpeByTelephone.fold(false)(contactTel => contactTel == ContactUPEByTelephone.Yes)
        )
      )

}
