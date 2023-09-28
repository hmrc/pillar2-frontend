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
import forms.CaptureContactAddressFormProvider
import models.fm.{FilingMember, NfmRegisteredAddress}
import models.requests.DataRequest
import models.subscription.SubscriptionAddress
import models.subscription.common.UpeCorrespAddressDetails
import models.{Mode, NormalMode}
import pages.{NominatedFilingMemberPage, RegistrationPage, SubscriptionPage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.Gettable
import service.SubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import views.html.errors.ErrorTemplate
import views.html.subscriptionview.CaptureContactAddressView

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

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val notAvailable = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")

    // Get Upe Address Details or a specific error message.
    val upeAddressDetails: Either[String, UpeCorrespAddressDetails] = request.userAnswers.get(RegistrationPage) match {
      case Some(registration) =>
        subscriptionService.getUpeAddressDetails(registration).map(Right(_)).getOrElse(Left("Error obtaining Upe Address Details"))
      case None =>
        Left("No registration data found")
    }

    val nfmAddressDetails: Either[String, NfmRegisteredAddress] = request.userAnswers.get(NominatedFilingMemberPage) match {
      case Some(filing) =>
        subscriptionService.getNfmRegisteredAddress(filing).map(Right(_)).getOrElse(Left("Error obtaining Nfm Address Details"))
      case None =>
        Left("No registration data found")
    }

//    val preparedForm = request.userAnswers.get(CaptureContactAddressPage) match {
//      case None        => form
//      case Some(value) => form.fill(value)
//    }

    val nfmConfirmationValue: Option[Boolean] =
      request.userAnswers.get(FilingMemberPath).map(_.nfmConfirmation)

    nfmConfirmationValue match {
      case Some(true) =>
        isNfmNotRegisteredUK(request) match {
          case true =>
            request.userAnswers
              .get(SubscriptionPage)
              .fold(NotFound(notAvailable)) { reg =>
                reg.subscriptionAddress.fold(
                  Ok(
                    view(
                      form, // Form is not filled with SubscriptionAddress. It expects a Boolean.
                      mode,
                      getNfmAddressLine1(request),
                      getNfmAddressLine2(request),
                      getNfmAddressLine3(request),
                      getNfmAddressLine4(request),
                      getNfmPostalCode(request),
                      getNfmCountryCode(request)
                    )
                  )
                )(data =>
                  Ok(
                    view(
                      form.fill(true), // Fill the form with a Boolean value.
                      mode,
                      getNfmAddressLine1(request),
                      getNfmAddressLine2(request),
                      getNfmAddressLine3(request),
                      getNfmAddressLine4(request),
                      getNfmPostalCode(request),
                      getNfmCountryCode(request)
                    )
                  )
                )
              }

          case false =>
            isUpeNotRegisteredUK(request) match {
              case true =>
                request.userAnswers
                  .get(SubscriptionPage)
                  .fold(NotFound(notAvailable)) { reg =>
                    reg.subscriptionAddress.fold(
                      Ok(
                        view(
                          form, // Form is not filled with SubscriptionAddress. It expects a Boolean.
                          mode,
                          getUpeAddressLine1(request),
                          getUpeAddressLine2(request),
                          getUpeAddressLine3(request),
                          getUpeAddressLine4(request),
                          getUpePostalCode(request),
                          getUpeCountryCode(request)
                        )
                      )
                    )(data =>
                      Ok(
                        view(
                          form.fill(true), // Fill the form with a Boolean value.
                          mode,
                          getUpeAddressLine1(request),
                          getUpeAddressLine2(request),
                          getUpeAddressLine3(request),
                          getUpeAddressLine4(request),
                          getUpePostalCode(request),
                          getUpeCountryCode(request)
                        )
                      )
                    )
                  }
            }
        }

//    isNfmNotRegisteredUK(request) match {
//      case true =>
//        request.userAnswers
//          .get(SubscriptionPage)
//          .fold(NotFound(notAvailable)) { reg =>
//            reg.subscriptionAddress.fold(
//              Ok(
//                view(
//                  form,
//                  mode,
//                  getNfmAddressLine1(request),
//                  getNfmAddressLine2(request),
//                  getNfmAddressLine3(request),
//                  getNfmAddressLine4(request),
//                  getNfmPostalCode(request),
//                  getNfmCountryCode(request)
//                )
//              )
//            )(data =>
//              Ok(
//                view(
//                  form.fill(data),
//                  mode,
//                  getNfmAddressLine1(request),
//                  getNfmAddressLine2(request),
//                  getNfmAddressLine3(request),
//                  getNfmAddressLine4(request),
//                  getNfmPostalCode(request),
//                  getNfmCountryCode(request)
//                )
//              )
//            )
//          }
//      case false =>
//        isUpeNotRegisteredUK(request) match {
//          case true =>
//            request.userAnswers
//              .get(SubscriptionPage)
//              .fold(NotFound(notAvailable)) { reg =>
//                reg.subscriptionAddress.fold(
//                  Ok(
//                    view(
//                      form,
//                      mode,
//                      getUpeAddressLine1(request),
//                      getUpeAddressLine2(request),
//                      getUpeAddressLine3(request),
//                      getUpeAddressLine4(request),
//                      getUpePostalCode(request),
//                      getUpeCountryCode(request)
//                    )
//                  )
//                )(data =>
//                  Ok(
//                    view(
//                      form.fill(data),
//                      mode,
//                      getUpeAddressLine1(request),
//                      getUpeAddressLine2(request),
//                      getUpeAddressLine3(request),
//                      getUpeAddressLine4(request),
//                      getUpePostalCode(request),
//                      getUpeCountryCode(request)
//                    )
//                  )
//                )
//              }
//          case false => Redirect(controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode)) // ask for primary conact
//        }
    }

//    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val subData = request.userAnswers.get(SubscriptionPage).getOrElse(throw new Exception("subscription data is not available"))
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          isNfmNotRegisteredUK(request) match {
            case true =>
              Future.successful(
                BadRequest(
                  view(
                    formWithErrors,
                    mode,
                    getNfmAddressLine1(request),
                    getNfmAddressLine2(request),
                    getNfmAddressLine3(request),
                    getNfmAddressLine4(request),
                    getNfmPostalCode(request),
                    getNfmCountryCode(request)
                  )
                )
              )
            case false =>
              Future.successful(
                BadRequest(
                  view(
                    formWithErrors,
                    mode,
                    getUpeAddressLine1(request),
                    getUpeAddressLine2(request),
                    getUpeAddressLine3(request),
                    getUpeAddressLine4(request),
                    getUpePostalCode(request),
                    getUpeCountryCode(request)
                  )
                )
              )
          },
        value =>
          value match {
            case true =>
              isNfmNotRegisteredUK(request) match {
                case true =>
                  for {
                    updatedAnswers <-
                      Future
                        .fromTry(
                          request.userAnswers.set(
                            SubscriptionPage,
                            subData.copy(
                              subscriptionAddress = Some(
                                SubscriptionAddress(
                                  getNfmAddressLine1(request),
                                  Some(getNfmAddressLine2(request)),
                                  getNfmAddressLine3(request),
                                  Some(getNfmAddressLine4(request)),
                                  Some(getNfmPostalCode(request)),
                                  getNfmCountryCode(request)
                                )
                              ),
                              contactDetailsStatus = RowStatus.Completed
                            )
                          )
                        )
                    _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                  } yield Redirect(controllers.routes.UnderConstructionController.onPageLoad)
                case false =>
                  for {
                    updatedAnswers <-
                      Future
                        .fromTry(
                          request.userAnswers.set(
                            SubscriptionPage,
                            subData.copy(
                              subscriptionAddress = Some(
                                SubscriptionAddress(
                                  getUpeAddressLine1(request),
                                  Some(getUpeAddressLine2(request)),
                                  getUpeAddressLine3(request),
                                  Some(getUpeAddressLine4(request)),
                                  Some(getUpePostalCode(request)),
                                  getUpeCountryCode(request)
                                )
                              ),
                              contactDetailsStatus = RowStatus.Completed
                            )
                          )
                        )
                    _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                  } yield Redirect(controllers.routes.UnderConstructionController.onPageLoad)
              }
            case false =>
              for {
                updatedAnswers <-
                  Future
                    .fromTry(
                      request.userAnswers.set(
                        SubscriptionPage,
                        subData.copy()
                      )
                    )
                _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
              } yield Redirect(controllers.subscription.routes.FmContactAddressController.onPageLoad(NormalMode))
          }
      )
  }

  private def isPreviousPageDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(SubscriptionPage)
      .fold(false) { data =>
        data.addSecondaryContact == false
      }

  private def isNfmNotRegisteredUK(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(NominatedFilingMemberPage)
      .flatMap { nfm =>
        nfm.isNfmRegisteredInUK
      } match {
      case Some(false) => true
      case _           => false
    }

  private def isUpeNotRegisteredUK(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(RegistrationPage)
      .map { upe =>
        upe.isUPERegisteredInUK
      } match {
      case Some(false) => true
      case _           => false
    }

  private def getNfmAddressLine1(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(NominatedFilingMemberPage)
    registration.fold("")(regData =>
      regData.withoutIdRegData.fold("")(withoutId =>
        withoutId.registeredFmAddress.fold("")(nfmRegisteredAddress => nfmRegisteredAddress.addressLine1)
      )
    )
  }

  private def getNfmAddressLine2(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(NominatedFilingMemberPage)
    registration.fold("")(regData =>
      regData.withoutIdRegData.fold("")(withoutId =>
        withoutId.registeredFmAddress.fold("")(nfmRegisteredAddress => nfmRegisteredAddress.addressLine2.getOrElse(""))
      )
    )
  }

  private def getNfmAddressLine3(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(NominatedFilingMemberPage)
    registration.fold("")(regData =>
      regData.withoutIdRegData.fold("")(withoutId =>
        withoutId.registeredFmAddress.fold("")(nfmRegisteredAddress => nfmRegisteredAddress.addressLine3)
      )
    )
  }

  private def getNfmAddressLine4(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(NominatedFilingMemberPage)
    registration.fold("")(regData =>
      regData.withoutIdRegData.fold("")(withoutId =>
        withoutId.registeredFmAddress.fold("")(nfmRegisteredAddress => nfmRegisteredAddress.addressLine4.getOrElse(""))
      )
    )
  }

  private def getNfmPostalCode(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(NominatedFilingMemberPage)
    registration.fold("")(regData =>
      regData.withoutIdRegData.fold("")(withoutId =>
        withoutId.registeredFmAddress.fold("")(nfmRegisteredAddress => nfmRegisteredAddress.postalCode.getOrElse(""))
      )
    )
  }

  private def getNfmCountryCode(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(NominatedFilingMemberPage)
    registration.fold("")(regData =>
      regData.withoutIdRegData.fold("")(withoutId => withoutId.registeredFmAddress.fold("")(nfmRegisteredAddress => nfmRegisteredAddress.countryCode))
    )
  }

  private def getUpeAddressLine1(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(RegistrationPage)
    registration.fold("")(regData =>
      regData.withoutIdRegData.fold("")(withoutId =>
        withoutId.upeRegisteredAddress.fold("")(upeRegisteredAddress => upeRegisteredAddress.addressLine1)
      )
    )
  }

  private def getUpeAddressLine2(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(RegistrationPage)
    registration.fold("")(regData =>
      regData.withoutIdRegData.fold("")(withoutId =>
        withoutId.upeRegisteredAddress.fold("")(upeRegisteredAddress => upeRegisteredAddress.addressLine2.getOrElse(""))
      )
    )
  }

  private def getUpeAddressLine3(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(RegistrationPage)
    registration.fold("")(regData =>
      regData.withoutIdRegData.fold("")(withoutId =>
        withoutId.upeRegisteredAddress.fold("")(upeRegisteredAddress => upeRegisteredAddress.addressLine3)
      )
    )
  }

  private def getUpeAddressLine4(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(RegistrationPage)
    registration.fold("")(regData =>
      regData.withoutIdRegData.fold("")(withoutId =>
        withoutId.upeRegisteredAddress.fold("")(upeRegisteredAddress => upeRegisteredAddress.addressLine4.getOrElse(""))
      )
    )
  }

  private def getUpePostalCode(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(RegistrationPage)
    registration.fold("")(regData =>
      regData.withoutIdRegData.fold("")(withoutId =>
        withoutId.upeRegisteredAddress.fold("")(upeRegisteredAddress => upeRegisteredAddress.postalCode.getOrElse(""))
      )
    )
  }

  private def getUpeCountryCode(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(RegistrationPage)
    registration.fold("")(regData =>
      regData.withoutIdRegData.fold("")(withoutId =>
        withoutId.upeRegisteredAddress.fold("")(upeRegisteredAddress => upeRegisteredAddress.countryCode)
      )
    )
  }

  object FilingMemberPath extends Gettable[FilingMember] {
    override def path: JsPath = JsPath \ "data" \ "FilingMember"
  }
}
