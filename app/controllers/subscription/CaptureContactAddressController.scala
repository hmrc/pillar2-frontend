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
import models.errors._
import models.fm.FilingMember
import models.requests.DataRequest
import models.subscription.SubscriptionAddress
import models.{Mode, NormalMode}
import pages.{NominatedFilingMemberPage, RegistrationPage, SubscriptionPage}
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.SubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import utils.countryOptions.CountryOptions
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
  subscriptionService:       SubscriptionService,
  countryOptions:            CountryOptions
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {
  val logger = Logger(getClass)

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val notAvailable = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")

    val nfmConfirmationValue: Boolean = request.userAnswers.get(NominatedFilingMemberPage).map(_.nfmConfirmation).getOrElse(false)
    isPreviousPageDefined(request) match {
      case true =>
        nfmConfirmationValue match {
          case true =>
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
                          countryOptions.getCountryNameFromCode(getNfmCountryCode(request))
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
                          countryOptions.getCountryNameFromCode(getNfmCountryCode(request))
                        )
                      )
                    )
                  }
              case false =>
                val filingMember: Option[FilingMember] = request.userAnswers.get(NominatedFilingMemberPage)
                filingMember match {
                  case Some(filingMember) =>
                    val nfmAddressResult = subscriptionService.getNfmAddressDetails(filingMember)
                    nfmAddressResult match {
                      case Right(nfmAddress) =>
                        val countryName = countryOptions.getCountryNameFromCode(nfmAddress.countryCode)
                        logger.info(s"Passing country to view: $countryName")
                        Ok(
                          view(
                            form,
                            mode,
                            nfmAddress.addressLine1,
                            nfmAddress.addressLine2.getOrElse(""),
                            nfmAddress.addressLine3,
                            nfmAddress.addressLine4.getOrElse(""),
                            nfmAddress.postalCode.getOrElse(""),
                            countryName
                          )
                        )
                      case Left(error) =>
                        // Log the error
                        logger.error(s"Error retrieving Nfm address details: $error")
                        BadRequest(
                          view(
                            form.withError("address", "There was an error retrieving the address details."),
                            mode,
                            // Provide defaults
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                          )
                        )
                    }
                  case None =>
                    logger.warn("Tried to fetch FilingMember details but none found.")
                    BadRequest("Please provide FilingMember details first.")
                }
            }
          case _ =>
            isUpeNotRegisteredUK(request) match {
              case true =>
                request.userAnswers
                  .get(SubscriptionPage)
                  .fold(NotFound(notAvailable)) { reg =>
                    reg.subscriptionAddress.fold(
                      Ok(
                        view(
                          form,
                          mode,
                          getUpeAddressLine1(request),
                          getUpeAddressLine2(request),
                          getUpeAddressLine3(request),
                          getUpeAddressLine4(request),
                          getUpePostalCode(request),
                          countryOptions.getCountryNameFromCode(getUpeCountryCode(request))
                        )
                      )
                    )(data =>
                      Ok(
                        view(
                          form.fill(true),
                          mode,
                          getUpeAddressLine1(request),
                          getUpeAddressLine2(request),
                          getUpeAddressLine3(request),
                          getUpeAddressLine4(request),
                          getUpePostalCode(request),
                          countryOptions.getCountryNameFromCode(getUpeCountryCode(request))
                        )
                      )
                    )
                  }
              case false =>
                // Call the method getUpeAddressDetails in the SubscriptionService
                // to get the UpeRegisteredAddress details.
                request.userAnswers.get(RegistrationPage) match {
                  case Some(registration) =>
                    logger.info(s"Found registration: $registration")
                    val upeAddressResult = subscriptionService.getUpeAddressDetails(registration)
                    upeAddressResult match {
                      case Right(upeAddress) =>
                        val countryName = countryOptions.getCountryNameFromCode(upeAddress.countryCode)
                        logger.info(s"Passing country to view: $countryName")
                        Ok(
                          view(
                            form,
                            mode,
                            upeAddress.addressLine1,
                            upeAddress.addressLine2.getOrElse(""),
                            upeAddress.addressLine3,
                            upeAddress.addressLine4.getOrElse(""),
                            upeAddress.postalCode.getOrElse(""),
                            countryName
                          )
                        )
                      case Left(error) =>
                        logger.error(s"Error retrieving Upe address details: $error")
                        BadRequest(
                          view(
                            form.withError("address", "There was an error retrieving the UPE address details."),
                            mode,
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                          )
                        )
                    }
                  case None =>
                    logger.warn("Registration not found!")
                    // Handle the case where the registration details aren't available
                    BadRequest("Registration details not available")
                }
            }
        }
      case false => NotFound(notAvailable)
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    // Extract the address fields from the request
    val addressLine1 = request.body.asFormUrlEncoded.get("addressLine1").headOption.getOrElse("")
    val addressLine2 = request.body.asFormUrlEncoded.get("addressLine2").headOption.getOrElse("")
    val town_city    = request.body.asFormUrlEncoded.get("town_city").headOption.getOrElse("")
    val region       = request.body.asFormUrlEncoded.get("region").headOption.getOrElse("")
    val postcode     = request.body.asFormUrlEncoded.get("postcode").headOption.getOrElse("")
    val country      = request.body.asFormUrlEncoded.get("country").headOption.getOrElse("")

    request.userAnswers
      .get(SubscriptionPage)
      .fold(
        // Case when SubscriptionPage data is not available
        Future.successful(
          BadRequest(
            view(
              form.withError("subscription", "Subscription data is not available."),
              mode,
              addressLine1,
              addressLine2,
              town_city,
              region,
              postcode,
              country
            )
          )
        )
      ) { subData => // Case when SubscriptionPage data is available

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
                        addressLine1,
                        addressLine2,
                        town_city,
                        region,
                        postcode,
                        country
                      )
                    )
                  )
                case false =>
                  Future.successful(
                    BadRequest(
                      view(
                        formWithErrors,
                        mode,
                        addressLine1,
                        addressLine2,
                        town_city,
                        region,
                        postcode,
                        country
                      )
                    )
                  )
              },
            value =>
              if (value) {
                val subscriptionAddress = SubscriptionAddress(
                  addressLine1,
                  if (addressLine2.nonEmpty) Some(addressLine2) else None,
                  town_city,
                  if (region.nonEmpty) Some(region) else None,
                  if (postcode.nonEmpty) Some(postcode) else None,
                  country
                )

                isNfmNotRegisteredUK(request) match {
                  case true =>
                    for {
                      updatedAnswers <-
                        Future.fromTry(
                          request.userAnswers.set(
                            SubscriptionPage,
                            subData.copy(
                              subscriptionAddress = Some(subscriptionAddress),
                              contactDetailsStatus = RowStatus.Completed
                            )
                          )
                        )
                      _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                    } yield Redirect(controllers.routes.UnderConstructionController.onPageLoad)
                  case false =>
                    for {
                      updatedAnswers <-
                        Future.fromTry(
                          request.userAnswers.set(
                            SubscriptionPage,
                            subData.copy(
                              subscriptionAddress = Some(subscriptionAddress),
                              contactDetailsStatus = RowStatus.Completed
                            )
                          )
                        )
                      _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                    } yield Redirect(controllers.routes.UnderConstructionController.onPageLoad)
                }
              } else {
                for {
                  updatedAnswers <-
                    Future.fromTry(
                      request.userAnswers.set(
                        SubscriptionPage,
                        subData.copy()
                      )
                    )
                  _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                } yield Redirect(controllers.subscription.routes.SubscriptionAddressController.onPageLoad(NormalMode))
              }
          )
      }
  }

  private def isPreviousPageDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(SubscriptionPage)
      .fold(false) { data =>
        data.addSecondaryContact.fold(false)(data => data.booleanValue() == false)
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

}
