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
import models.requests.DataRequest
import models.{Mode, NormalMode}
import pages.{NominatedFilingMemberPage, RegistrationPage, SubscriptionPage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import views.html.errors.ErrorTemplate
import views.html.subscriptionview.UseContactPrimaryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UseContactPrimaryController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  formProvider:              UseContactPrimaryFormProvider,
  page_not_available:        ErrorTemplate,
  val controllerComponents:  MessagesControllerComponents,
  view:                      UseContactPrimaryView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {
  val form = formProvider()
  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val notAvailable = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")
    isPreviousPageDefined(request) match {
      case true =>
        isNfmRegisteredUK(request) match {
          case true =>
            request.userAnswers
              .get(SubscriptionPage)
              .fold(NotFound(notAvailable)) { reg =>
                reg.useContactPrimary.fold(Ok(view(form, mode, getName(request), getEmail(request), getPhoneNumber(request))))(data =>
                  Ok(view(form.fill(data), mode, getName(request), getEmail(request), getPhoneNumber(request)))
                )
              }
          case false =>
            isUpeRegisteredUK(request) match {
              case true =>
                request.userAnswers
                  .get(SubscriptionPage)
                  .fold(NotFound(notAvailable)) { reg =>
                    reg.useContactPrimary.fold(Ok(view(form, mode, getUpeName(request), getUpeEmail(request), getUpePhoneNumber(request))))(data =>
                      Ok(view(form.fill(data), mode, getUpeName(request), getUpeEmail(request), getUpePhoneNumber(request)))
                    )
                  }
              case false => Redirect(controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode)) // ask for primary conact
            }
        }
      case false => NotFound(notAvailable)
    }
  }
  // noinspection ScalaStyle
  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val subData = request.userAnswers.get(SubscriptionPage).getOrElse(throw new Exception("subscription data is available"))
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          isNfmRegisteredUK(request) match {
            case true =>
              Future.successful(BadRequest(view(formWithErrors, mode, getName(request), getEmail(request), getPhoneNumber(request))))
            case false =>
              Future.successful(BadRequest(view(formWithErrors, mode, getName(request), getEmail(request), getPhoneNumber(request))))
          },
        value =>
          value match {
            case true =>
              (isNfmRegisteredUK(request), getPhoneNumber(request).isEmpty, getUpePhoneNumber(request).isEmpty) match {
                case (true, true, _) =>
                  for {
                    updatedAnswers <-
                      Future
                        .fromTry(
                          request.userAnswers.set(
                            SubscriptionPage,
                            subData.copy(
                              domesticOrMne = subData.domesticOrMne,
                              useContactPrimary = Some(value),
                              contactByTelephone = Some(ContactByNfmPhoneNumber(request)),
                              primaryContactName = Some(getName(request)),
                              primaryContactEmail = Some(getEmail(request)),
                              groupDetailStatus = subData.groupDetailStatus,
                              contactDetailsStatus = RowStatus.InProgress
                            )
                          )
                        )
                    _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                  } yield Redirect(controllers.subscription.routes.AddSecondaryContactController.onPageLoad(mode))
                case (true, false, _) =>
                  for {
                    updatedAnswers <-
                      Future
                        .fromTry(
                          request.userAnswers.set(
                            SubscriptionPage,
                            subData.copy(
                              domesticOrMne = subData.domesticOrMne,
                              useContactPrimary = Some(value),
                              contactByTelephone = Some(ContactByNfmPhoneNumber(request)),
                              primaryContactName = Some(getName(request)),
                              primaryContactEmail = Some(getEmail(request)),
                              primaryContactTelephone = Some(getPhoneNumber(request)),
                              groupDetailStatus = subData.groupDetailStatus,
                              contactDetailsStatus = RowStatus.InProgress
                            )
                          )
                        )
                    _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                  } yield Redirect(controllers.subscription.routes.AddSecondaryContactController.onPageLoad(mode))
                case (false, _, true) =>
                  for {
                    updatedAnswers <-
                      Future
                        .fromTry(
                          request.userAnswers.set(
                            SubscriptionPage,
                            subData.copy(
                              domesticOrMne = subData.domesticOrMne,
                              useContactPrimary = Some(value),
                              contactByTelephone = Some(ContactByUpePhoneNumber(request)),
                              primaryContactTelephone = None,
                              primaryContactName = Some(getUpeName(request)),
                              primaryContactEmail = Some(getUpeEmail(request)),
                              groupDetailStatus = subData.groupDetailStatus,
                              contactDetailsStatus = RowStatus.InProgress
                            )
                          )
                        )
                    _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                  } yield Redirect(controllers.subscription.routes.AddSecondaryContactController.onPageLoad(mode))
                case (false, _, false) =>
                  for {
                    updatedAnswers <-
                      Future
                        .fromTry(
                          request.userAnswers.set(
                            SubscriptionPage,
                            subData.copy(
                              domesticOrMne = subData.domesticOrMne,
                              useContactPrimary = Some(value),
                              contactByTelephone = Some(ContactByUpePhoneNumber(request)),
                              primaryContactName = Some(getUpeName(request)),
                              primaryContactEmail = Some(getUpeEmail(request)),
                              primaryContactTelephone = Some(getUpePhoneNumber(request)),
                              groupDetailStatus = subData.groupDetailStatus,
                              contactDetailsStatus = RowStatus.InProgress
                            )
                          )
                        )
                    _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                  } yield Redirect(controllers.subscription.routes.AddSecondaryContactController.onPageLoad(mode))
              }
            case false =>
              if (subData.useContactPrimary.fold(false)(usePrimary => usePrimary)) {
                for {
                  updatedAnswers <-
                    Future
                      .fromTry(
                        request.userAnswers.set(
                          SubscriptionPage,
                          subData.copy(
                            domesticOrMne = subData.domesticOrMne,
                            useContactPrimary = Some(value),
                            groupDetailStatus = subData.groupDetailStatus,
                            contactDetailsStatus = RowStatus.InProgress,
                            primaryContactName = None,
                            primaryContactEmail = None,
                            contactByTelephone = None,
                            primaryContactTelephone = None
                          )
                        )
                      )
                  _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                } yield Redirect(controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode))
              } else {
                for {
                  updatedAnswers <-
                    Future
                      .fromTry(
                        request.userAnswers.set(
                          SubscriptionPage,
                          subData.copy(
                            domesticOrMne = subData.domesticOrMne,
                            accountingPeriod = subData.accountingPeriod,
                            primaryContactEmail = subData.primaryContactEmail,
                            contactByTelephone = subData.contactByTelephone,
                            primaryContactTelephone = subData.primaryContactTelephone,
                            primaryContactName = subData.primaryContactName,
                            groupDetailStatus = subData.groupDetailStatus,
                            contactDetailsStatus = RowStatus.InProgress,
                            useContactPrimary = Some(value)
                          )
                        )
                      )
                  _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                } yield Redirect(controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode))
              }
          }
      )
  }

  private def isPreviousPageDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(SubscriptionPage)
      .fold(false) { data =>
        data.groupDetailStatus.toString == "Completed"
      }

  private def isNfmRegisteredUK(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(NominatedFilingMemberPage)
      .flatMap { nfm =>
        nfm.isNfmRegisteredInUK
      } match {
      case Some(false) => true
      case _           => false
    }

  private def isUpeRegisteredUK(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(RegistrationPage)
      .map { upe =>
        upe.isUPERegisteredInUK
      } match {
      case Some(false) => true
      case _           => false
    }

  private def getName(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(NominatedFilingMemberPage)
    registration.fold("")(regData => regData.withoutIdRegData.fold("")(withoutId => withoutId.fmContactName.fold("")(name => name)))
  }

  private def getEmail(request: DataRequest[AnyContent]): String = {
    val registration = request.userAnswers.get(NominatedFilingMemberPage)
    registration.fold("")(regData => regData.withoutIdRegData.fold("")(withoutId => withoutId.fmEmailAddress.fold("")(email => email)))
  }

  private def ContactByNfmPhoneNumber(request: DataRequest[AnyContent]): Boolean = {
    val registration = request.userAnswers.get(NominatedFilingMemberPage)
    registration.fold(false)(regData => regData.withoutIdRegData.fold(false)(withoutId => withoutId.telephoneNumber.fold(false)(tel => tel.nonEmpty)))
  }

  private def ContactByUpePhoneNumber(request: DataRequest[AnyContent]): Boolean = {
    val registration = request.userAnswers.get(RegistrationPage)
    registration.fold(false)(regData => regData.withoutIdRegData.fold(false)(withoutId => withoutId.contactUpeByTelephone.nonEmpty))
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
