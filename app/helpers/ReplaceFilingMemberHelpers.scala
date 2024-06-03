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

package helpers

import models.{GovUKMarginBottom9, UserAnswers}
import models.rfm.CorporatePosition
import models.subscription.{ContactDetailsType, NewFilingMemberDetail}
import pages._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utils.RowStatus
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers.{EntityTypeIncorporatedCompanyNameRfmSummary, EntityTypeIncorporatedCompanyRegRfmSummary, EntityTypeIncorporatedCompanyUtrRfmSummary, EntityTypePartnershipCompanyNameRfmSummary, EntityTypePartnershipCompanyRegRfmSummary, EntityTypePartnershipCompanyUtrRfmSummary, RfmAddSecondaryContactSummary, RfmCapturePrimaryTelephoneSummary, RfmContactByTelephoneSummary, RfmCorporatePositionSummary, RfmNameRegistrationSummary, RfmPrimaryContactEmailSummary, RfmPrimaryContactNameSummary, RfmRegisteredAddressSummary, RfmSecondaryContactEmailSummary, RfmSecondaryContactNameSummary, RfmSecondaryTelephonePreferenceSummary, RfmSecondaryTelephoneSummary}
import viewmodels.govuk.all.{FluentSummaryList, SummaryListViewModel}

trait ReplaceFilingMemberHelpers {

  self: UserAnswers =>

  def securityQuestionStatus: RowStatus = {
    val first  = get(RfmPillar2ReferencePage).isDefined
    val second = get(RfmRegistrationDatePage).isDefined
    (first, second) match {
      case (true, true)  => RowStatus.Completed
      case (true, false) => RowStatus.InProgress
      case _             => RowStatus.NotStarted
    }
  }

  def rfmPrimaryContactList(implicit messages: Messages): SummaryList = SummaryListViewModel(
    rows = Seq(
      RfmPrimaryContactNameSummary.row(self),
      RfmPrimaryContactEmailSummary.row(self),
      RfmContactByTelephoneSummary.row(self),
      RfmCapturePrimaryTelephoneSummary.row(self)
    ).flatten
  ).withCssClass(GovUKMarginBottom9.toString)

  def rfmCorporatePositionSummaryList(countryOptions: CountryOptions)(implicit messages: Messages): SummaryList =
    SummaryListViewModel(
      rows = Seq(
        RfmCorporatePositionSummary.row(self),
        RfmNameRegistrationSummary.row(self),
        RfmRegisteredAddressSummary.row(self, countryOptions),
        EntityTypeIncorporatedCompanyNameRfmSummary.row(self),
        EntityTypeIncorporatedCompanyRegRfmSummary.row(self),
        EntityTypeIncorporatedCompanyUtrRfmSummary.row(self),
        EntityTypePartnershipCompanyNameRfmSummary.row(self),
        EntityTypePartnershipCompanyRegRfmSummary.row(self),
        EntityTypePartnershipCompanyUtrRfmSummary.row(self)
      ).flatten
    ).withCssClass(GovUKMarginBottom9.toString)

  def rfmSecondaryContactList(implicit messages: Messages): SummaryList = SummaryListViewModel(
    rows = Seq(
      RfmAddSecondaryContactSummary.row(self),
      RfmSecondaryContactNameSummary.row(self),
      RfmSecondaryContactEmailSummary.row(self),
      RfmSecondaryTelephonePreferenceSummary.row(self),
      RfmSecondaryTelephoneSummary.row(self)
    ).flatten
  ).withCssClass(GovUKMarginBottom9.toString)

  def rfmNoIdQuestionStatus: RowStatus = {
    val first  = get(RfmNameRegistrationPage).isDefined
    val second = get(RfmRegisteredAddressPage).isDefined
    (first, second) match {
      case (true, true)  => RowStatus.Completed
      case (true, false) => RowStatus.InProgress
      case _             => RowStatus.NotStarted
    }
  }

  def getSecondaryContact: Option[ContactDetailsType] =
    get(RfmAddSecondaryContactPage).flatMap { nominated =>
      if (nominated) {
        for {
          secondaryName  <- get(RfmSecondaryContactNamePage)
          secondaryEmail <- get(RfmSecondaryEmailPage)
        } yield ContactDetailsType(name = secondaryName, telephone = getSecondaryTelephone, emailAddress = secondaryEmail)
      } else {
        None
      }
    }

  private def getSecondaryTelephone: Option[String] =
    get(RfmSecondaryPhonePreferencePage).flatMap { nominated =>
      if (nominated) get(RfmSecondaryCapturePhonePage) else None
    }

  def getNewFilingMemberDetail: Option[NewFilingMemberDetail] =
    for {
      referenceNumber   <- get(RfmPillar2ReferencePage)
      corporatePosition <- get(RfmCorporatePositionPage)
      primaryName       <- get(RfmPrimaryContactNamePage)
      email             <- get(RfmPrimaryContactEmailPage)
      address           <- get(RfmContactAddressPage)
    } yield NewFilingMemberDetail(
      plrReference = referenceNumber,
      corporatePosition = corporatePosition,
      contactName = primaryName,
      contactEmail = email,
      phoneNumber = getPrimaryTelephone,
      address = address,
      secondaryContactInformation = getSecondaryContact
    )

  private def getPrimaryTelephone: Option[String] =
    get(RfmContactByTelephonePage).flatMap { nominated =>
      if (nominated) get(RfmCapturePrimaryTelephonePage) else None
    }

  private def isAllSecondaryInfoProvided: Boolean =
    get(RfmAddSecondaryContactPage)
      .map { nominated =>
        if (nominated) {
          (for {
            _         <- get(RfmSecondaryContactNamePage)
            _         <- get(RfmSecondaryEmailPage)
            nominated <- get(RfmSecondaryPhonePreferencePage)
          } yield
            if (nominated) {
              get(RfmSecondaryCapturePhonePage).map(_ => true).getOrElse(false)
            } else {
              true
            }).getOrElse(false)
        } else {
          true
        }
      }
      .getOrElse(false)

  private def isAllPrimaryContactInfoProvided: Boolean =
    (for {
      _                     <- get(RfmPrimaryContactNamePage)
      _                     <- get(RfmPrimaryContactEmailPage)
      _                     <- get(RfmContactAddressPage)
      primaryPhoneNominated <- get(RfmContactByTelephonePage)
    } yield
      if (primaryPhoneNominated) {
        get(RfmCapturePrimaryTelephonePage).map(_ => true).getOrElse(false)
      } else {
        true
      }).getOrElse(false)

  def isRfmJourneyCompleted: Boolean =
    get(RfmCorporatePositionPage)
      .map(corporatePosition =>
        if (corporatePosition == CorporatePosition.Upe) {
          if (isAllSecondaryInfoProvided & isAllPrimaryContactInfoProvided) true else false
        } else {
          if (isAllNewFilingMemberInfoProvided & isAllSecondaryInfoProvided & isAllPrimaryContactInfoProvided) true else false
        }
      )
      .getOrElse(false)

  private def isAllNewFilingMemberInfoProvided: Boolean =
    get(RfmUkBasedPage)
      .map { ukBased =>
        if (ukBased) {
          (for {
            _ <- get(RfmEntityTypePage)
            _ <- get(RfmGrsDataPage)
          } yield true).getOrElse(false)
        } else {
          (for {
            _ <- get(RfmNameRegistrationPage)
            _ <- get(RfmRegisteredAddressPage)
          } yield true).getOrElse(false)
        }
      }
      .getOrElse(false)

  def rfmAnsweredSecurityQuestions: Boolean = {
    val isReferenceDefined = get(RfmPillar2ReferencePage).isDefined
    val isDateDefined      = get(RfmRegistrationDatePage).isDefined

    (isReferenceDefined, isDateDefined) match {
      case (true, true) => true
      case _            => false
    }
  }

}
