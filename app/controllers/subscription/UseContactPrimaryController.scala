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
import forms.UseContactPrimaryFormProvider
import helpers.SubscriptionHelpers
import models.requests.DataRequest
import models.{Mode, NormalMode}
import pages._
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UseContactPrimaryController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              UseContactPrimaryFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      UseContactPrimaryView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with SubscriptionHelpers {
  val form = formProvider()
  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    (for {
      nfmNominated     <- request.userAnswers.get(NominateFilingMemberPage)
      upeMneOrDomestic <- request.userAnswers.get(upeRegisteredInUKPage)
    } yield {
      val nfmMneOrDom = request.userAnswers.get(fmRegisteredInUKPage)
      (nfmNominated, upeMneOrDomestic, nfmMneOrDom) match {
        case (true, _, Some(false))                        => fmNoID(mode)
        case (true, false, Some(true)) | (false, false, _) => upeNoID(mode)
        case _ => Redirect(controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode))
      }
    }).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold( //ask for the empty string
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, "", "", ""))),
        value =>
          value match {
            case true =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(subUsePrimaryContactPage, value))
                _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(controllers.subscription.routes.AddSecondaryContactController.onPageLoad(mode))
            case false =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(subUsePrimaryContactPage, value))
                _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(controllers.subscription.routes.ContactNameComplianceController.onPageLoad(mode))
          }
      )
  }



  private def fmNoID(mode: Mode)(implicit request: DataRequest[AnyContent]): Action[AnyContent] =
    (for {
      contactName  <- request.userAnswers.get(fmContactNamePage)
      contactEmail <- request.userAnswers.get(fmContactEmailPage)
      telPref      <- request.userAnswers.get(fmPhonePreferencePage)
    } yield
      if (telPref) {
        val contactTel = request.userAnswers.get(fmCapturePhonePage)
        Ok(view(form, mode, contactName, contactEmail, contactTel))
      } else {
        Ok(view(form, mode, contactName, contactEmail, None))
      }).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

  private def upeNoID(mode: Mode)(implicit request: DataRequest[AnyContent]): Action[AnyContent] =
    (for {
      contactName  <- request.userAnswers.get(upeContactNamePage)
      contactEmail <- request.userAnswers.get(upeContactEmailPage)
      telPref      <- request.userAnswers.get(upePhonePreferencePage)
    } yield
      if (telPref) {
        request.userAnswers
          .get(upeCapturePhonePage)
          .map { phone =>
            Ok(view(form, mode, contactName, contactEmail, phone))
          }
          .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      } else {
        Ok(view(form, mode, contactName, contactEmail))
      }).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

}
