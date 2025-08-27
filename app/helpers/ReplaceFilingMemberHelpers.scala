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

import models.rfm.CorporatePosition
import models.subscription.{ContactDetailsType, NewFilingMemberDetail}
import models.{GovUKMarginBottom9, NonUKAddress, UserAnswers}
import pages._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utils.RowStatus
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers._
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
      RfmContactByPhoneSummary.row(self),
      RfmCapturePrimaryPhoneSummary.row(self)
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
      RfmSecondaryPhonePreferenceSummary.row(self),
      RfmSecondaryPhoneSummary.row(self)
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
        } yield ContactDetailsType(name = secondaryName, phone = getSecondaryPhone, emailAddress = secondaryEmail)
      } else {
        None
      }
    }

  private def getSecondaryPhone: Option[String] =
    get(RfmSecondaryPhonePreferencePage).flatMap { nominated =>
      if (nominated) get(RfmSecondaryCapturePhonePage) else None
    }

  def getNewFilingMemberDetail: Option[NewFilingMemberDetail] =
    for {
      securityAnswerUserReference    <- get(RfmPillar2ReferencePage)
      securityAnswerRegistrationDate <- get(RfmRegistrationDatePage)
      plrReference                   <- get(RfmPillar2ReferencePage)
      corporatePosition              <- get(RfmCorporatePositionPage)
      primaryContactPhonePreference  <- get(RfmContactByPhonePage)
      primaryContactName             <- get(RfmPrimaryContactNamePage)
      primaryContactEmail            <- get(RfmPrimaryContactEmailPage)
      contactAddress                 <- get(RfmContactAddressPage)
      addSecondaryContact            <- get(RfmAddSecondaryContactPage)
    } yield NewFilingMemberDetail(
      securityAnswerUserReference = securityAnswerUserReference,
      securityAnswerRegistrationDate = securityAnswerRegistrationDate,
      plrReference = plrReference,
      corporatePosition = corporatePosition,
      ukBased = getUkBased,
      nameRegistration = getNameRegistration,
      registeredAddress = getRegistrationAddress,
      primaryContactName = primaryContactName,
      primaryContactEmail = primaryContactEmail,
      primaryContactPhonePreference = primaryContactPhonePreference,
      primaryContactPhoneNumber = getPrimaryPhone,
      addSecondaryContact = addSecondaryContact,
      secondaryContactInformation = getSecondaryContact,
      contactAddress = contactAddress
    )

  private def getUkBased: Option[Boolean] =
    get(RfmCorporatePositionPage) match {
      case Some(CorporatePosition.Upe)    => None
      case Some(CorporatePosition.NewNfm) => get(RfmUkBasedPage)
    }

  private def getNameRegistration: Option[String] =
    get(RfmUkBasedPage).flatMap { ukBased =>
      if (ukBased) None else get(RfmNameRegistrationPage)
    }

  private def getRegistrationAddress: Option[NonUKAddress] =
    get(RfmUkBasedPage).flatMap { ukBased =>
      if (ukBased) None else get(RfmRegisteredAddressPage)
    }

  private def getPrimaryPhone: Option[String] =
    get(RfmContactByPhonePage).flatMap { nominated =>
      if (nominated) get(RfmCapturePrimaryPhonePage) else None
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
      primaryPhoneNominated <- get(RfmContactByPhonePage)
    } yield
      if (primaryPhoneNominated) {
        get(RfmCapturePrimaryPhonePage).map(_ => true).getOrElse(false)
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

}
