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
import models.registration.{Registration, WithoutIdRegData}
import models.requests.DataRequest
import models.subscription.common.UpeCorrespAddressDetails
import models.{Mode, NfmRegisteredInUkConfirmation, NormalMode, UPERegisteredInUKConfirmation, UseContactPrimary}
import pages.{CaptureContactAddressPage, NominatedFilingMemberPage, RegistrationPage, SubscriptionPage}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{Json, Reads}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.HtmlFormat
import queries.Gettable
import service.SubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.errors.ErrorTemplate
import views.html.subscriptionview.CaptureContactAddressView
import play.api.libs.json._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CaptureContactAddressController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              CaptureContactAddressFormProvider,
  page_not_available:        ErrorTemplate,
  val controllerComponents:  MessagesControllerComponents,
  view:                      CaptureContactAddressView,
  subscriptionService:       SubscriptionService
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val logger: Logger = Logger(this.getClass)

  val form = formProvider()

//  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
//    val notAvailable = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")
//    val emptyString  = ""
//
//    //  isPreviousPageDefined(request) match {
//    //    case true =>
//    if (isNfmNotRegisteredUK(request) || !isUpeRegisteredUK(request)) {
//
//      val upeAddressDetails: Either[String, UpeCorrespAddressDetails] = request.userAnswers.get(RegistrationPage) match {
//        case Some(registration) =>
//          subscriptionService.getUpeAddressDetails(registration).map(Right(_)).getOrElse(Left("Error obtaining Upe Address Details"))
//        case None =>
//          throw new NoSuchElementException("No registration data found")
//      }
//
//      upeAddressDetails match {
//        case Right(addressDetails) =>
//          Ok(populateViewWithDetails(form.fill(true), mode, Some(addressDetails), emptyString, emptyString, emptyString))
//        case Left(_) =>
//          val filledForm = getFilledFormFromSubscriptionPage(request)
//          Ok(populateViewWithDetails(filledForm, mode, None, getName(request), getEmail(request), getPhoneNumber(request)))
//      }
//    } else if (isUpeRegisteredUK(request)) {
//      val filledForm = getFilledFormFromSubscriptionPage(request)
//      Ok(populateViewWithDetails(filledForm, mode, None, getUpeName(request), getUpeEmail(request), getUpePhoneNumber(request)))
//    } else {
//      Redirect(controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode))
//    }
//  //    case false => NotFound(notAvailable)
//  //  }
//  }


  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val notAvailable = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")
    val emptyString = ""

    // Get Upe Address Details or a specific error message.
    val upeAddressDetails: Either[String, UpeCorrespAddressDetails] = request.userAnswers.get(RegistrationPage) match {
      case Some(registration) =>
        subscriptionService.getUpeAddressDetails(registration).map(Right(_)).getOrElse(Left("Error obtaining Upe Address Details"))
      case None =>
        Left("No registration data found")
    }

    if (isNfmNotRegisteredUK(request) || !isUpeRegisteredUK(request)) {
      upeAddressDetails match {
        case Right(addressDetails) =>
          Ok(populateViewWithDetails(form.fill(true), mode, Some(addressDetails), emptyString, emptyString, emptyString))
        case Left(error) =>
          // Logging the error
          logger.error("Error : " + error)
          NotFound(s"Error: $error")
      }
    } else if (isUpeRegisteredUK(request)) {
      val filledForm = getFilledFormFromSubscriptionPage(request)
      Ok(populateViewWithDetails(filledForm, mode, None, getUpeName(request), getUpeEmail(request), getUpePhoneNumber(request)))
    } else {
      Redirect(controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode))
    }
  }

  private def getFilledFormFromSubscriptionPage(request: DataRequest[AnyContent]): Form[Boolean] =
    request.userAnswers.get(SubscriptionPage).fold(form) { reg =>
      reg.useContactPrimary match {
        case Some(UseContactPrimary.Yes) => form.fill(true)
        case Some(UseContactPrimary.No)  => form.fill(false)
        case None                        => form
      }
    }

  private def populateViewWithDetails(
    form:              Form[Boolean],
    mode:              Mode,
    addressDetailsOpt: Option[UpeCorrespAddressDetails],
    name:              String,
    email:             String,
    phoneNumber:       String
  )(implicit request:  Request[_]): HtmlFormat.Appendable = {

    val (addressLine1, addressLine2, addressLine3, addressLine4, postCode, countryCode) = addressDetailsOpt match {
      case Some(addressDetails) =>
        (
          addressDetails.addressLine1,
          addressDetails.addressLine2.getOrElse(""),
          addressDetails.addressLine3.getOrElse(""),
          addressDetails.addressLine4.getOrElse(""),
          addressDetails.postCode.getOrElse(""),
          addressDetails.countryCode
        )
      case None => ("", "", "", "", "", "")
    }

    view(
      form,
      mode,
      name,
      email,
      phoneNumber,
      "",
      "",
      "",
      addressLine1,
      addressLine2,
      addressLine3,
      addressLine4,
      postCode,
      countryCode
    )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => {
          val (
            addressLine1,
            addressLine2,
            addressLine3,
            addressLine4,
            postCode,
            countryCode
          ) = extractAddressDetails(formWithErrors)

          Future.successful(
            BadRequest(
              view(
                formWithErrors,
                mode,
                getName(request),
                getEmail(request),
                getPhoneNumber(request),
                getUpeName(request),
                getUpeEmail(request),
                getUpePhoneNumber(request),
                addressLine1,
                addressLine2,
                addressLine3,
                addressLine4,
                postCode,
                countryCode
              )
            )
          )
        },
        value =>
          {
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(CaptureContactAddressPage, value))
              _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
            } yield Redirect(routes.UnderConstructionController.onPageLoad)
          }.recover { case exception: Exception =>
            // Logging the error
            logger.error("Error encountered while processing onSubmit: ", exception)

            // Responding to the client with an error
            InternalServerError("There was an error saving the data.")
          }
      )
  }

  private def extractDataFromUserAnswers[T](
    request:            DataRequest[AnyContent],
    page:               Gettable[Registration],
    extractionFunction: WithoutIdRegData => String
  )(implicit reads:     Reads[Registration]): String = {
    val registrationOption = request.userAnswers.get(page)

    registrationOption.fold("") { registration =>
      registration.withoutIdRegData.fold("") { withoutId =>
        extractionFunction(withoutId)
      }
    }
  }

  private def extractAddressDetails(form: Form[Boolean]): (String, String, String, String, String, String) = {
    val addressLine1 = form.data.getOrElse("addressLine1", "")
    val addressLine2 = form.data.getOrElse("addressLine2", "")
    val addressLine3 = form.data.getOrElse("addressLine3", "")
    val addressLine4 = form.data.getOrElse("addressLine4", "")
    val postCode     = form.data.getOrElse("postCode", "")
    val countryCode  = form.data.getOrElse("countryCode", "")

    (addressLine1, addressLine2, addressLine3, addressLine4, postCode, countryCode)
  }

  private def isPreviousPageDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(SubscriptionPage)
      .fold(false) { data =>
        data.groupDetailStatus.toString == "Completed"
      }

  private def isNfmNotRegisteredUK(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(NominatedFilingMemberPage)
      .fold(false) { data =>
        data.isNfmRegisteredInUK.fold(false)(regInUk => regInUk == NfmRegisteredInUkConfirmation.No)
      }

  private def isUpeRegisteredUK(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(RegistrationPage)
      .fold(false) { data =>
        data.isUPERegisteredInUK == UPERegisteredInUKConfirmation.No
      }

  private def getName(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(NominatedFilingMemberPage)
    registration.fold("")(regData => regData.withoutIdRegData.fold("")(withoutId => withoutId.fmContactName.fold("")(name => name)))
  }

  private def getEmail(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(NominatedFilingMemberPage)
    registration.fold("")(regData => regData.withoutIdRegData.fold("")(withoutId => withoutId.fmEmailAddress.fold("")(email => email)))
  }

  private def getPhoneNumber(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(NominatedFilingMemberPage)
    registration.fold("")(regData => regData.withoutIdRegData.fold("")(withoutId => withoutId.telephoneNumber.fold("")(tel => tel)))
  }

  private def getUpeName(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(RegistrationPage)
    registration.fold("")(regData => regData.withoutIdRegData.fold("")(withoutId => withoutId.upeContactName.fold("")(name => name)))
  }

  private def getUpeEmail(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(RegistrationPage)
    registration.fold("")(regData => regData.withoutIdRegData.fold("")(withoutId => withoutId.emailAddress.fold("")(email => email)))
  }

  private def getUpePhoneNumber(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(RegistrationPage)
    registration.fold("")(regData => regData.withoutIdRegData.fold("")(withoutId => withoutId.telephoneNumber.fold("")(tel => tel)))
  }
}
