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
import models.subscription.{ContactDetail, ContactDetailsModel, NoContactDetailsFound}
import models.{Mode, NormalMode}
import pages._
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.UseContactPrimaryView

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
    with SubscriptionHelpers{
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


  val fmContactName = request.userAnswers.get(fmContactNamePage)
  val fmContactEmail = request.userAnswers.get(fmContactEmailPage)
  val fmTel = request.userAnswers.get(fmCapturePhonePage)
  val upeTel = request.userAnswers.get(upeCapturePhonePage)
  val upeContactName = request.userAnswers.get(upeContactNamePage)
  val upeContactEmail = request.userAnswers.get(upeContactEmailPage)
  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>


    form
      .bindFromRequest()
      .fold(
        formWithErrors => fmContactEmail.isDefined match{
          case true => Future.successful(BadRequest(view(formWithErrors, mode, fmContactName.getOrElse(""), fmContactEmail.getOrElse(""), fmTel)))
          case _ => Future.successful(BadRequest(view(formWithErrors, mode, upeContactName.getOrElse(""), upeContactEmail.getOrElse(""), upeTel)))
        },
        value =>
          value match {
            case true =>
              if(fmContactEmail.isDefined){
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(subUsePrimaryContactPage, value))
//                  updatedAnswers <- Future.fromTry(request.userAnswers.set(subPrimaryContactNamePage, fmContactName.get))
//                  updatedAnswers <- Future.fromTry(request.userAnswers.set(subPrimaryEmailPage, fmContactEmail.get))
//                  updatedAnswers <- Future.fromTry(request.userAnswers.set(subPrimaryCapturePhonePage, fmTel.get))
                  _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                } yield Redirect(controllers.subscription.routes.AddSecondaryContactController.onPageLoad(mode))
              } else {
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(subUsePrimaryContactPage, value))
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(subPrimaryContactNamePage, upeContactName.get))
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(subPrimaryEmailPage, upeContactEmail.get))
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(subPrimaryCapturePhonePage, upeTel.get))
                  _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                } yield Redirect(controllers.subscription.routes.AddSecondaryContactController.onPageLoad(mode))
              }

            case false =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(subUsePrimaryContactPage, value))
                _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(controllers.subscription.routes.ContactNameComplianceController.onPageLoad(mode))
          }
      )
  }


  private def fmNoID(mode: Mode)(implicit request: DataRequest[AnyContent]) =
    (for {
      contactName  <- request.userAnswers.get(fmContactNamePage)
      contactEmail <- request.userAnswers.get(fmContactEmailPage)
      telPref      <- request.userAnswers.get(fmPhonePreferencePage)
    } yield {
      if (telPref) {
        val contactTel = request.userAnswers.get(fmCapturePhonePage)
        Ok(view(form, mode, contactName, contactEmail, contactTel))
      } else {
        Ok(view(form, mode, contactName, contactEmail, None))
      }
    }).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

  private def upeNoID(mode: Mode)(implicit request: DataRequest[AnyContent]) =
    (for {
      contactName  <- request.userAnswers.get(upeContactNamePage)
      contactEmail <- request.userAnswers.get(upeContactEmailPage)
      telPref      <- request.userAnswers.get(upePhonePreferencePage)
    } yield {
      if (telPref) {
        val phone =request.userAnswers.get(upeCapturePhonePage)
            Ok(view(form, mode, contactName, contactEmail, phone))

      } else {
        Ok(view(form, mode, contactName, contactEmail, None))
      }
    }).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

}
