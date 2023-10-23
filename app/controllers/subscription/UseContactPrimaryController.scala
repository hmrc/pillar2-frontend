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
import models.subscription.SubscriptionContactDetails
import models.{Mode, NormalMode}
import pages._
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.subscriptionview.UseContactPrimaryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

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
    contactDetail(request) match {
      case Right(contactDetail) =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future
                .successful(BadRequest(view(formWithErrors, mode, contactDetail.contactName, contactDetail.ContactEmail, contactDetail.ContactTel))),
            value =>
              value match {
                case true =>
                  for {
                    updatedAnswers  <- Future.fromTry(request.userAnswers.set(subUsePrimaryContactPage, value))
                    updatedAnswers1 <- Future.fromTry(updatedAnswers.set(subPrimaryContactNamePage, contactDetail.contactName))
                    updatedAnswers2 <- Future.fromTry(updatedAnswers1.set(subPrimaryEmailPage, contactDetail.ContactEmail))
                    updatedAnswers3 <-
                      Future
                        .fromTry(contactDetail.ContactTel.map(updatedAnswers2.set(subPrimaryCapturePhonePage, _)).getOrElse(Success(updatedAnswers2)))
                    _ <- userAnswersConnectors.save(updatedAnswers3.id, Json.toJson(updatedAnswers3.data))
                  } yield Redirect(controllers.subscription.routes.AddSecondaryContactController.onPageLoad(mode))
                case false =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(subUsePrimaryContactPage, value))
                    _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                  } yield Redirect(controllers.subscription.routes.ContactNameComplianceController.onPageLoad(mode))
              }
          )
      case Left(result) => Future.successful(result)
    }
  }

  private def contactDetail(request: DataRequest[AnyContent]): Either[Result, SubscriptionContactDetails] =
    request.userAnswers
      .get(NominateFilingMemberPage)
      .flatMap { registered =>
        if (registered) {
          request.userAnswers.get(fmRegisteredInUKPage).map { ukBased =>
            if (!ukBased) {
              (for {
                contactName  <- request.userAnswers.get(fmContactNamePage)
                contactEmail <- request.userAnswers.get(fmContactEmailPage)
              } yield Right(SubscriptionContactDetails(contactName, contactEmail, request.userAnswers.get(fmCapturePhonePage))))
                .getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
            } else {
              (for {
                contactName  <- request.userAnswers.get(upeContactNamePage)
                contactEmail <- request.userAnswers.get(upeContactEmailPage)
              } yield Right(SubscriptionContactDetails(contactName, contactEmail, request.userAnswers.get(upeCapturePhonePage))))
                .getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
            }
          }
        } else {
          for {
            contactName  <- request.userAnswers.get(upeContactNamePage)
            contactEmail <- request.userAnswers.get(upeContactEmailPage)
          } yield Right(SubscriptionContactDetails(contactName, contactEmail, request.userAnswers.get(upeCapturePhonePage)))
        }
      }
      .getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))

  private def fmNoID(mode: Mode)(implicit request: DataRequest[AnyContent]) =
    (for {
      contactName  <- request.userAnswers.get(fmContactNamePage)
      contactEmail <- request.userAnswers.get(fmContactEmailPage)
      telPref      <- request.userAnswers.get(fmPhonePreferencePage)
    } yield {
      val contactTel = request.userAnswers.get(fmCapturePhonePage)
      request.userAnswers.get(subUsePrimaryContactPage) match {
        case Some(value) if telPref  => Ok(view(form.fill(value), mode, contactName, contactEmail, contactTel))
        case Some(value) if !telPref => Ok(view(form.fill(value), mode, contactName, contactEmail, None))
        case None if telPref         => Ok(view(form, mode, contactName, contactEmail, contactTel))
        case None if !telPref        => Ok(view(form, mode, contactName, contactEmail, None))
      }
    }).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

  private def upeNoID(mode: Mode)(implicit request: DataRequest[AnyContent]) =
    (for {
      contactName  <- request.userAnswers.get(upeContactNamePage)
      contactEmail <- request.userAnswers.get(upeContactEmailPage)
      telPref      <- request.userAnswers.get(upePhonePreferencePage)
    } yield {
      val contactTel = request.userAnswers.get(upeCapturePhonePage)
      request.userAnswers.get(subUsePrimaryContactPage) match {
        case Some(value) if telPref  => Ok(view(form.fill(value), mode, contactName, contactEmail, contactTel))
        case Some(value) if !telPref => Ok(view(form.fill(value), mode, contactName, contactEmail, None))
        case None if telPref         => Ok(view(form, mode, contactName, contactEmail, contactTel))
        case None if !telPref        => Ok(view(form, mode, contactName, contactEmail, None))
      }
    }).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

}
