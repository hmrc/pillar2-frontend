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

package controllers.subscription

import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions._
import controllers.routes
import forms.CaptureContactAddressFormProvider
import models.{Mode, NfmRegisteredInUkConfirmation, UPERegisteredInUKConfirmation}
import models.requests.DataRequest
import pages.{CaptureContactAddressPage, NominatedFilingMemberPage, RegistrationPage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.CaptureContactAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CaptureContactAddressController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              CaptureContactAddressFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      CaptureContactAddressView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(CaptureContactAddressPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(CaptureContactAddressPage, value))
            _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
          } yield Redirect(routes.UnderConstructionController.onPageLoad)
      )
  }

  private def isNfmNotRegisteredUK(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(NominatedFilingMemberPage)
      .fold(false) { data =>
        data.isNfmRegisteredInUK.fold(false)(regInUk => regInUk == NfmRegisteredInUkConfirmation.No)
      }

  private def isUpeNotRegisteredUK(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(RegistrationPage)
      .fold(false) { data =>
        data.isUPERegisteredInUK == UPERegisteredInUKConfirmation.No
      }
}
