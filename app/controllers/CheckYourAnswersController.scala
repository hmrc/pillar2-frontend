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

package controllers

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.requests.DataRequest
import models.subscription.Subscription
import pages.{NominatedFilingMemberPage, RegistrationPage, SubscriptionPage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView
import views.html.errors.ErrorTemplate
import views.html.subscriptionview.ContactCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  val userAnswersConnectors: UserAnswersConnectors,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  val controllerComponents:  MessagesControllerComponents,
  page_not_available:        ErrorTemplate,
  view:                      CheckYourAnswersView,
  countryOptions:            CountryOptions
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val notAvailable = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")
    (for {
      sub <- request.userAnswers.get(SubscriptionPage)
      listUpe = (isUpeWithIdDefined(request), isIncorporatedEntityDefined(request), isUpeCanContactByPhone(request)) match {
                  case (true, true, _) =>
                    SummaryListViewModel(
                      rows = Seq(
                        EntityTypeIncorporatedCompanyNameUpeSummary.row(request.userAnswers),
                        EntityTypeIncorporatedCompanyRegUpeSummary.row(request.userAnswers),
                        EntityTypeIncorporatedCompanyUtrUpeSummary.row(request.userAnswers)
                      ).flatten
                    )
                  case (true, false, _) =>
                    SummaryListViewModel(
                      rows = Seq(
                        EntityTypePartnershipCompanyNameUpeSummary.row(request.userAnswers),
                        EntityTypePartnershipCompanyRegUprSummary.row(request.userAnswers),
                        EntityTypePartnershipCompanyUtrUprSummary.row(request.userAnswers)
                      ).flatten
                    )
                  case (false, false, true) =>
                    SummaryListViewModel(rows =
                      Seq(
                        UpeNameRegistrationSummary.row(request.userAnswers),
                        UpeRegisteredAddressSummary.row(request.userAnswers, countryOptions),
                        UpeContactNameSummary.row(request.userAnswers),
                        UpeContactEmailSummary.row(request.userAnswers),
                        UpeTelephonePreferenceSummary.row(request.userAnswers),
                        UPEContactTelephoneSummary.row(request.userAnswers)
                      ).flatten
                    )
                  case (false, false, false) =>
                    SummaryListViewModel(rows =
                      Seq(
                        UpeNameRegistrationSummary.row(request.userAnswers),
                        UpeRegisteredAddressSummary.row(request.userAnswers, countryOptions),
                        UpeContactNameSummary.row(request.userAnswers),
                        UpeContactEmailSummary.row(request.userAnswers),
                        UpeTelephonePreferenceSummary.row(request.userAnswers)
                      ).flatten
                    )
                }

      listNfm =
        (
          isNfmWithIdDefined(request),
          isIncorporatedEntityNfmDefined(request),
          doNotWantToRegisterNfm(request),
          isNfmCanContactByPhone(request)
        ) match {
          case (true, true, _, _) =>
            SummaryListViewModel(
              rows = Seq(
                NominateFilingMemberYesNoSummary.row(request.userAnswers),
                NominateFilingMemberYesNoSummary.row(request.userAnswers),
                EntityTypeIncorporatedCompanyNameNfmSummary.row(request.userAnswers),
                EntityTypeIncorporatedCompanyRegNfmSummary.row(request.userAnswers),
                EntityTypeIncorporatedCompanyUtrNfmSummary.row(request.userAnswers)
              ).flatten
            )
          case (true, false, _, _) =>
            SummaryListViewModel(
              rows = Seq(
                NominateFilingMemberYesNoSummary.row(request.userAnswers),
                NominateFilingMemberYesNoSummary.row(request.userAnswers),
                EntityTypePartnershipCompanyNameNfmSummary.row(request.userAnswers),
                EntityTypePartnershipCompanyRegNfmSummary.row(request.userAnswers),
                EntityTypePartnershipCompanyUtrNfmSummary.row(request.userAnswers)
              ).flatten
            )
          case (false, false, true, _) =>
            SummaryListViewModel(rows = Seq.empty)
          case (_, _, _, true) =>
            SummaryListViewModel(rows =
              Seq(
                NominateFilingMemberYesNoSummary.row(request.userAnswers),
                NfmNameRegistrationSummary.row(request.userAnswers),
                NfmRegisteredAddressSummary.row(request.userAnswers, countryOptions),
                NfmContactNameSummary.row(request.userAnswers),
                NfmEmailAddressSummary.row(request.userAnswers),
                NfmTelephonePreferenceSummary.row(request.userAnswers),
                NfmCaptureTelephoneDetailsSummary.row(request.userAnswers)
              ).flatten
            )
          case (_, _, _, _) =>
            SummaryListViewModel(rows =
              Seq(
                NominateFilingMemberYesNoSummary.row(request.userAnswers),
                NfmNameRegistrationSummary.row(request.userAnswers),
                NfmRegisteredAddressSummary.row(request.userAnswers, countryOptions),
                NfmContactNameSummary.row(request.userAnswers),
                NfmEmailAddressSummary.row(request.userAnswers),
                NfmTelephonePreferenceSummary.row(request.userAnswers)
              ).flatten
            )
        }

      furtherRegistrationDetailsList = SummaryListViewModel(
                                         rows = Seq(
                                           MneOrDomesticSummary.row(request.userAnswers),
                                           GroupAccountingPeriodSummary.row(request.userAnswers),
                                           GroupAccountingPeriodStartDateSummary.row(request.userAnswers),
                                           GroupAccountingPeriodEndDateSummary.row(request.userAnswers)
                                         ).flatten
                                       )
      listPrimary = isPrimaryPhoneDefined(sub) match {
                      case true =>
                        SummaryListViewModel(
                          rows = Seq(
                            ContactNameComplianceSummary.row(request.userAnswers),
                            ContactEmailAddressSummary.row(request.userAnswers),
                            ContactByTelephoneSummary.row(request.userAnswers),
                            ContactCaptureTelephoneDetailsSummary.row(request.userAnswers)
                          ).flatten
                        )
                      case false =>
                        val subRegData = request.userAnswers.get(SubscriptionPage).getOrElse(throw new Exception("Subscription data not available"))
                        for {
                          updatedAnswers <-
                            Future
                              .fromTry(
                                request.userAnswers.set(
                                  SubscriptionPage,
                                  subRegData.copy(
                                    contactByTelephone = Some(false)
                                  )
                                )
                              )
                          _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                        } yield ()

                        SummaryListViewModel(
                          rows = Seq(
                            ContactNameComplianceSummary.row(request.userAnswers),
                            ContactEmailAddressSummary.row(request.userAnswers),
                            ContactByTelephoneSummary.row(request.userAnswers)
                          ).flatten
                        )
                    }

      addSecondaryContactList =
        SummaryListViewModel(
          rows = Seq(
            AddSecondaryContactSummary.row(request.userAnswers)
          ).flatten
        )

      listSecondary = (isSecondContactDefined(request), isSecondaryPhoneDefined(request)) match {
                        case (true, true) =>
                          SummaryListViewModel(
                            rows = Seq(
                              SecondaryContactNameSummary.row(request.userAnswers),
                              SecondaryContactEmailSummary.row(request.userAnswers),
                              SecondaryTelephonePreferenceSummary.row(request.userAnswers),
                              SecondaryTelephoneSummary.row(request.userAnswers)
                            ).flatten
                          )
                        case (true, false) =>
                          val subRegData = request.userAnswers.get(SubscriptionPage).getOrElse(throw new Exception("Subscription data not available"))
                          for {
                            updatedAnswers <-
                              Future
                                .fromTry(
                                  request.userAnswers.set(
                                    SubscriptionPage,
                                    subRegData.copy(
                                      secondaryTelephonePreference = Some(false),
                                      secondaryContactTelephone = None
                                    )
                                  )
                                )
                            _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                          } yield ()
                          SummaryListViewModel(
                            rows = Seq(
                              SecondaryContactNameSummary.row(request.userAnswers),
                              SecondaryContactEmailSummary.row(request.userAnswers),
                              SecondaryTelephonePreferenceSummary.row(request.userAnswers)
                            ).flatten
                          )
                        case _ =>
                          val subRegData = request.userAnswers.get(SubscriptionPage).getOrElse(throw new Exception("Subscription data not available"))
                          for {
                            updatedAnswers <-
                              Future
                                .fromTry(
                                  request.userAnswers.set(
                                    SubscriptionPage,
                                    subRegData.copy(
                                      addSecondaryContact = Some(false),
                                      secondaryContactName = None,
                                      secondaryContactEmail = None,
                                      secondaryContactTelephone = None
                                    )
                                  )
                                )
                            _ <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
                          } yield ()
                          SummaryListViewModel(rows = Seq())
                      }
      address = SummaryListViewModel(
                  rows = Seq(
                    ContactCorrespondenceAddressSummary.row(request.userAnswers, countryOptions)
                  ).flatten
                )

    } yield
      if (isPreviousPagesDefined(request))
        Ok(view(listUpe, listNfm, furtherRegistrationDetailsList, listPrimary, addSecondaryContactList, listSecondary, address))
      else
        NotFound(notAvailable)).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))

  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) async { implicit request =>
    val regdata = request.userAnswers.get(RegistrationPage).getOrElse(throw new Exception("Registration is not available"))
    val fmData  = request.userAnswers.get(NominatedFilingMemberPage).getOrElse(throw new Exception("Filing is not available"))
    Future.successful(Redirect(controllers.routes.TaskListController.onPageLoad))
  // createRegistrationAndSubscription(regdata, fmData)
  }
  private def isPreviousPagesDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(SubscriptionPage)
      .fold(false) { data =>
        data.groupDetailStatus.toString == "Completed"
      }

  private def isUpeWithIdDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(RegistrationPage)
      .fold(false) { data =>
        data.withIdRegData.isDefined && data.isUPERegisteredInUK
      }

  private def isUpeCanContactByPhone(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(RegistrationPage)
      .fold(false) { data =>
        data.withoutIdRegData.isDefined && data.withoutIdRegData.fold(false)(data =>
          data.contactUpeByTelephone.fold(false)(phone => phone) && data.telephoneNumber.isDefined
        )
      }

  private def isNfmCanContactByPhone(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(NominatedFilingMemberPage)
      .fold(false) { data =>
        data.withoutIdRegData.isDefined && data.withoutIdRegData.fold(false)(data => data.contactNfmByTelephone.fold(false)(phone => phone))
      }

  private def isIncorporatedEntityDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(RegistrationPage)
      .fold(false) { data =>
        data.withIdRegData.fold(false)(data => data.incorporatedEntityRegistrationData.isDefined)
      }

  private def isNfmWithIdDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(NominatedFilingMemberPage)
      .fold(false) { data =>
        data.withIdRegData.isDefined && data.isNfmRegisteredInUK.fold(false)(isReg => isReg)
      }

  private def doNotWantToRegisterNfm(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(NominatedFilingMemberPage)
      .map { nfm =>
        nfm.nfmConfirmation
      } match {
      case Some(false) => true
      case _           => false
    }

  private def isIncorporatedEntityNfmDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(NominatedFilingMemberPage)
      .fold(false) { data =>
        data.withIdRegData.fold(false)(data => data.incorporatedEntityRegistrationData.isDefined)
      }

  private def isSecondContactDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(SubscriptionPage)
      .fold(false)(data =>
        data.addSecondaryContact.fold(false)(contact =>
          contact
            && data.secondaryContactName.isDefined
            && data.secondaryContactEmail.isDefined
            && data.secondaryTelephonePreference.isDefined
        )
      )

  private def isPrimaryPhoneDefined(data: Subscription): Boolean =
    data.contactByTelephone.fold(false)(contact => contact) && data.primaryContactTelephone.isDefined

  private def isSecondaryPhoneDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(SubscriptionPage)
      .fold(false)(data =>
        data.secondaryTelephonePreference.fold(false) { phone =>
          phone && data.secondaryContactTelephone.isDefined
        }
      )
}
