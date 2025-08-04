/*
 * Copyright 2024 HM Revenue & Customs
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

import cats.syntax.option._
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions._
import controllers.subscription.UseContactPrimaryController.contactSummaryList
import forms.UseContactPrimaryFormProvider
import models.requests.DataRequest
import models.subscription.SubscriptionContactDetails
import models.{Mode, NormalMode}
import navigation.SubscriptionNavigator
import pages._
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
import views.html.subscriptionview.UseContactPrimaryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class UseContactPrimaryController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  navigator:                 SubscriptionNavigator,
  formProvider:              UseContactPrimaryFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  view:                      UseContactPrimaryView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {
  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    (for {
      nfmNominated     <- request.userAnswers.get(NominateFilingMemberPage)
      upeMneOrDomestic <- request.userAnswers.get(UpeRegisteredInUKPage)
      _                <- request.userAnswers.get(SubAccountingPeriodPage)
    } yield {
      val nfmMneOrDom = request.userAnswers.get(FmRegisteredInUKPage)
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
                .successful(
                  BadRequest(
                    view(
                      formWithErrors,
                      mode,
                      contactSummaryList(contactDetail.contactName, contactDetail.ContactEmail, contactDetail.ContactTel)
                    )
                  )
                ),
            value =>
              value match {
                case true =>
                  for {
                    updatedAnswers  <- Future.fromTry(request.userAnswers.set(SubUsePrimaryContactPage, value))
                    updatedAnswers1 <- Future.fromTry(updatedAnswers.set(SubPrimaryContactNamePage, contactDetail.contactName))
                    updatedAnswers2 <- Future.fromTry(updatedAnswers1.set(SubPrimaryEmailPage, contactDetail.ContactEmail))
                    updatedAnswers3 <- Future.fromTry(updatedAnswers2.set(SubPrimaryPhonePreferencePage, contactDetail.phonePref))
                    updatedAnswers4 <-
                      Future
                        .fromTry(contactDetail.ContactTel.map(updatedAnswers3.set(SubPrimaryCapturePhonePage, _)).getOrElse(Success(updatedAnswers3)))
                    _ <- userAnswersConnectors.save(updatedAnswers4.id, Json.toJson(updatedAnswers4.data))
                  } yield Redirect(navigator.nextPage(SubUsePrimaryContactPage, mode, updatedAnswers4))
                case false =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(SubUsePrimaryContactPage, value))
                    _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                  } yield Redirect(navigator.nextPage(SubUsePrimaryContactPage, mode, updatedAnswers))
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
          request.userAnswers.get(FmRegisteredInUKPage).map { ukBased =>
            if (!ukBased) {
              (for {
                contactName  <- request.userAnswers.get(FmContactNamePage)
                contactEmail <- request.userAnswers.get(FmContactEmailPage)
                phonePref    <- request.userAnswers.get(FmPhonePreferencePage)
              } yield Right(SubscriptionContactDetails(contactName, contactEmail, phonePref, request.userAnswers.get(FmCapturePhonePage))))
                .getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
            } else {
              (for {
                contactName  <- request.userAnswers.get(UpeContactNamePage)
                contactEmail <- request.userAnswers.get(UpeContactEmailPage)
                phonePref    <- request.userAnswers.get(UpePhonePreferencePage)
              } yield Right(SubscriptionContactDetails(contactName, contactEmail, phonePref, request.userAnswers.get(UpeCapturePhonePage))))
                .getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
            }
          }
        } else {
          for {
            contactName  <- request.userAnswers.get(UpeContactNamePage)
            contactEmail <- request.userAnswers.get(UpeContactEmailPage)
            phonePref    <- request.userAnswers.get(UpePhonePreferencePage)
          } yield Right(SubscriptionContactDetails(contactName, contactEmail, phonePref, request.userAnswers.get(UpeCapturePhonePage)))
        }
      }
      .getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))

  private def fmNoID(mode: Mode)(implicit request: DataRequest[AnyContent]) =
    (for {
      contactName  <- request.userAnswers.get(FmContactNamePage)
      contactEmail <- request.userAnswers.get(FmContactEmailPage)
      telPref      <- request.userAnswers.get(FmPhonePreferencePage)
    } yield {
      val contactTel = request.userAnswers.get(FmCapturePhonePage)
      request.userAnswers.get(SubUsePrimaryContactPage) match {
        case Some(value) if telPref =>
          Ok(view(form.fill(value), mode, contactSummaryList(contactName, contactEmail, contactTel)))
        case Some(value) if !telPref =>
          Ok(view(form.fill(value), mode, contactSummaryList(contactName, contactEmail, None)))
        case None if telPref  => Ok(view(form, mode, contactSummaryList(contactName, contactEmail, contactTel)))
        case None if !telPref => Ok(view(form, mode, contactSummaryList(contactName, contactEmail, None)))
      }
    }).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

  private def upeNoID(mode: Mode)(implicit request: DataRequest[AnyContent]) =
    (for {
      contactName  <- request.userAnswers.get(UpeContactNamePage)
      contactEmail <- request.userAnswers.get(UpeContactEmailPage)
      telPref      <- request.userAnswers.get(UpePhonePreferencePage)
    } yield {
      val contactTel = request.userAnswers.get(UpeCapturePhonePage)
      request.userAnswers.get(SubUsePrimaryContactPage) match {
        case Some(value) if telPref  => Ok(view(form.fill(value), mode, contactSummaryList(contactName, contactEmail, contactTel)))
        case Some(value) if !telPref => Ok(view(form.fill(value), mode, contactSummaryList(contactName, contactEmail, None)))
        case None if telPref         => Ok(view(form, mode, contactSummaryList(contactName, contactEmail, contactTel)))
        case None if !telPref        => Ok(view(form, mode, contactSummaryList(contactName, contactEmail, None)))
      }
    }).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
}

object UseContactPrimaryController {
  private[controllers] def contactSummaryList(contactName: String, contactEmail: String, contactTel: Option[String])(implicit
    messages:                                              Messages
  ): SummaryList =
    SummaryListViewModel(
      rows = Seq(
        SummaryListRowViewModel(key = "useContactPrimary.name", value = ValueViewModel(HtmlContent(HtmlFormat.escape(contactName)))).some,
        SummaryListRowViewModel(key = "useContactPrimary.email", value = ValueViewModel(HtmlContent(HtmlFormat.escape(contactEmail).toString))).some,
        contactTel.map(tel => SummaryListRowViewModel(key = "useContactPrimary.telephone", value = ValueViewModel(HtmlFormat.escape(tel).toString)))
      ).flatten
    )
}
