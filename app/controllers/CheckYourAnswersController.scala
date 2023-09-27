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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.MneOrDomestic
import models.requests.DataRequest
import pages.{NominatedFilingMemberPage, RegistrationPage, SubscriptionPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers.{UpeTelephonePreferenceSummary, _}
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView
import views.html.errors.ErrorTemplate

class CheckYourAnswersController @Inject() (
  identify:                 IdentifierAction,
  getData:                  DataRetrievalAction,
  requireData:              DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view:                     CheckYourAnswersView,
  page_not_available:       ErrorTemplate,
  countryOptions:           CountryOptions
)(implicit appConfig:       FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val notAvailable = page_not_available("page_not_available.title", "page_not_available.heading", "page_not_available.message")

    println("************************************" + isUpeWithIdDefined(request) + isIncorporatedEntityDefined(request))
    val listUpe = (isUpeWithIdDefined(request), isIncorporatedEntityDefined(request), isUpeCanContactByPhone(request)) match {
      case (true, true, _) =>
        SummaryListViewModel(
          rows = Seq(
            EntityTypeIncorporatedCompanyNameUprSummary.row(request.userAnswers),
            EntityTypeIncorporatedCompanyRegUprSummary.row(request.userAnswers),
            EntityTypeIncorporatedCompanyUtrUprSummary.row(request.userAnswers)
          ).flatten
        )
      case (true, false, _) =>
        SummaryListViewModel(
          rows = Seq(
            EntityTypePartnershipCompanyNameUprSummary.row(request.userAnswers),
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
      case (false, false, _) =>
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

    val listNfm = (isNfmWithIdDefined(request), isIncorporatedEntityNfmDefined(request), doNotWantToRegisterNfm(request)) match {
      case (true, true, _) =>
        SummaryListViewModel(
          rows = Seq(
            EntityTypeIncorporatedCompanyNameNfmSummary.row(request.userAnswers),
            EntityTypeIncorporatedCompanyRegNfmSummary.row(request.userAnswers),
            EntityTypeIncorporatedCompanyUtrNfmSummary.row(request.userAnswers)
          ).flatten
        )
      case (true, false, _) =>
        SummaryListViewModel(
          rows = Seq(
            EntityTypePartnershipCompanyNameNfmSummary.row(request.userAnswers),
            EntityTypePartnershipCompanyRegNfmSummary.row(request.userAnswers),
            EntityTypePartnershipCompanyUtrNfmSummary.row(request.userAnswers)
          ).flatten
        )
      case (false, false, true) =>
        SummaryListViewModel(
          rows = Seq(
            NominateFilingMemberYesNoSummary.row(request.userAnswers)
          ).flatten
        )
      case (_, _, _) =>
        SummaryListViewModel(rows =
          Seq(
            NfmNameRegistrationSummary.row(request.userAnswers),
            NfmRegisteredAddressSummary.row(request.userAnswers, countryOptions),
            NfmContactNameSummary.row(request.userAnswers),
            NfmEmailAddressSummary.row(request.userAnswers),
            NfmTelephonePreferenceSummary.row(request.userAnswers)
          ).flatten
        )
    }

    val listPrimary = SummaryListViewModel(
      rows = Seq(
        ContactNameComplianceSummary.row(request.userAnswers),
        ContactEmailAddressSummary.row(request.userAnswers),
        ContactByTelephoneSummary.row(request.userAnswers),
        ContactCaptureTelephoneDetailsSummary.row(request.userAnswers)
      ).flatten
    )

    val listSecondary = isSecondContactDefined(request) match {
      case true =>
        SummaryListViewModel(
          rows = Seq(
            AddSecondaryContactSummary.row(request.userAnswers),
            SecondaryContactNameSummary.row(request.userAnswers),
            SecondaryContactEmailSummary.row(request.userAnswers),
            SecondaryTelephonePreferenceSummary.row(request.userAnswers),
            SecondaryTelephoneSummary.row(request.userAnswers)
          ).flatten
        )
      case false =>
        SummaryListViewModel(rows = Seq())
    }

    val furtherRegistrationDetailsList = SummaryListViewModel(
      rows = Seq(
        MneOrDomesticSummary.row(request.userAnswers),
        GroupAccountingPeriodSummary.row(request.userAnswers),
        GroupAccountingPeriodStartDateSummary.row(request.userAnswers),
        GroupAccountingPeriodEndDateSummary.row(request.userAnswers)
      ).flatten
    )

    val address = SummaryListViewModel(
      rows = Seq(
        ContactCorrespondenceAddressSummary.row(request.userAnswers, countryOptions)
      ).flatten
    )

    if (isPreviousPagesDefined(request))
      Ok(view(listUpe, listNfm, furtherRegistrationDetailsList, listPrimary, listSecondary, address))
    else
      NotFound(notAvailable)
  }

  private def isPreviousPagesDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(SubscriptionPage)
      .fold(false)(data => data.contactDetailsStatus.toString == "Completed")

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
        data.withoutIdRegData.isDefined && data.withoutIdRegData.fold(false)(data => data.contactUpeByTelephone.fold(false)(phone => phone))
      }

  private def isIncorporatedEntityDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(RegistrationPage)
      .fold(false) { data =>
        data.withIdRegData.fold(false)(data => data.incorporatedEntityRegistrationData.isDefined)
      }
  private def isSecondContactDefined(request: DataRequest[AnyContent]): Boolean =
    request.userAnswers
      .get(SubscriptionPage)
      .fold(false)((data => data.addSecondaryContact.fold(false)(contact => contact)))

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

}
