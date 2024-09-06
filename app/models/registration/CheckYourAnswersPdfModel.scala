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

package models.registration

import models.{MneOrDomestic, NonUKAddress, UKAddress, UserAnswers}
import pages._
import utils.countryOptions.CountryOptions

import java.time.LocalDate

case class CheckYourAnswersPdfModel(
  upe:              Upe,
  nfm:              Nfm,
  groupDetail:      GroupDetails,
  primaryContact:   PrimaryContact,
  secondaryContact: SecondaryContact,
  address:          NonUKAddress
)

case class Upe(
  NameRegistration:                           String,
  RegisteredAddress:                          UKAddress,
  ContactName:                                String,
  ContactEmail:                               String,
  TelephonePreference:                        Boolean,
  ContactTelephone:                           String,
  EntityTypeIncorporatedCompanyNameUpe:       String,
  EntityTypeIncorporatedRegUpe:               String,
  EntityTypeIncorporatedUtrUpe:               String,
  EntityTypePartnershipCompanyNameUpeSummary: String,
  EntityTypePartnershipCompanyRegUpeSummary:  String,
  EntityTypePartnershipCompanyUtrUpeSummary:  String
)

object Upe {
  def populate(answers: UserAnswers): Option[Upe] =
    for {
      nameRegistration  <- answers.get(UpeNameRegistrationPage)
      registeredAddress <- answers.get(UpeRegisteredAddressPage)
      contactName       <- answers.get(UpeContactNamePage)
      contactEmail      <- answers.get(UpeContactEmailPage)
      phonePreference   <- answers.get(UpePhonePreferencePage)
      contactTelephone  <- answers.get(UpeCapturePhonePage)
      grsResponse       <- answers.get(UpeGRSResponsePage)
      entityTypeIncorporatedCompanyName = grsResponse.incorporatedEntityRegistrationData.map(_.companyProfile.companyName).getOrElse("")
      entityTypeIncorporatedReg         = grsResponse.incorporatedEntityRegistrationData.map(_.companyProfile.companyNumber).getOrElse("")
      entityTypeIncorporatedUtr         = grsResponse.incorporatedEntityRegistrationData.map(_.ctutr).getOrElse("")
      entityTypePartnershipCompanyName = grsResponse.partnershipEntityRegistrationData
                                           .flatMap(_.companyProfile.map(_.companyName))
                                           .getOrElse("")
      entityTypePartnershipCompanyReg = grsResponse.partnershipEntityRegistrationData
                                          .flatMap(_.companyProfile.map(_.companyNumber))
                                          .getOrElse("")
      entityTypePartnershipCompanyUtr = grsResponse.partnershipEntityRegistrationData
                                          .flatMap(_.sautr)
                                          .getOrElse("")
    } yield Upe(
      NameRegistration = nameRegistration,
      RegisteredAddress = registeredAddress,
      ContactName = contactName,
      ContactEmail = contactEmail,
      TelephonePreference = phonePreference,
      ContactTelephone = contactTelephone,
      EntityTypeIncorporatedCompanyNameUpe = entityTypeIncorporatedCompanyName,
      EntityTypeIncorporatedRegUpe = entityTypeIncorporatedReg,
      EntityTypeIncorporatedUtrUpe = entityTypeIncorporatedUtr,
      EntityTypePartnershipCompanyNameUpeSummary = entityTypePartnershipCompanyName,
      EntityTypePartnershipCompanyRegUpeSummary = entityTypePartnershipCompanyReg,
      EntityTypePartnershipCompanyUtrUpeSummary = entityTypePartnershipCompanyUtr
    )
}
case class Nfm(
  nfmNominateYesNoSummary:              Boolean,
  nameRegistration:                     String,
  registeredAddress:                    NonUKAddress,
  contactName:                          String,
  emailAddress:                         String,
  telephonePreference:                  Boolean,
  contactTelephone:                     String,
  entityTypeIncorporatedCompanyNameNfm: String,
  entityTypeIncorporatedCompanyRegNfm:  String,
  entityTypeIncorporatedCompanyUtrNfm:  String,
  entityTypePartnershipCompanyNameNfm:  String,
  entityTypePartnershipCompanyRegNfm:   String,
  entityTypePartnershipCompanyUtrNfm:   String
)

object Nfm {
  def populate(answers: UserAnswers): Option[Nfm] =
    for {
      nfmNominateYesNoSummary <- answers.get(NominateFilingMemberPage)
      nameRegistration        <- answers.get(FmNameRegistrationPage)
      registeredAddress       <- answers.get(FmRegisteredAddressPage)
      contactName             <- answers.get(FmContactNamePage)
      emailAddress            <- answers.get(FmContactEmailPage)
      telephonePreference     <- answers.get(FmPhonePreferencePage)
      contactTelephone        <- answers.get(FmCapturePhonePage)
      grsResponse             <- answers.get(FmGRSResponsePage)

      entityTypeIncorporatedCompanyName = grsResponse.incorporatedEntityRegistrationData.map(_.companyProfile.companyName).getOrElse("")
      entityTypeIncorporatedCompanyReg  = grsResponse.incorporatedEntityRegistrationData.map(_.companyProfile.companyNumber).getOrElse("")
      entityTypeIncorporatedCompanyUtr  = grsResponse.incorporatedEntityRegistrationData.map(_.ctutr).getOrElse("")

      entityTypePartnershipCompanyName = grsResponse.partnershipEntityRegistrationData
                                           .flatMap(_.companyProfile.map(_.companyName))
                                           .getOrElse("")
      entityTypePartnershipCompanyReg = grsResponse.partnershipEntityRegistrationData
                                          .flatMap(_.companyProfile.map(_.companyNumber))
                                          .getOrElse("")
      entityTypePartnershipCompanyUtr = grsResponse.partnershipEntityRegistrationData
                                          .flatMap(_.sautr)
                                          .getOrElse("")

    } yield Nfm(
      nfmNominateYesNoSummary = nfmNominateYesNoSummary,
      nameRegistration = nameRegistration,
      registeredAddress = registeredAddress,
      contactName = contactName,
      emailAddress = emailAddress,
      telephonePreference = telephonePreference,
      contactTelephone = contactTelephone,
      entityTypeIncorporatedCompanyNameNfm = entityTypeIncorporatedCompanyName,
      entityTypeIncorporatedCompanyRegNfm = entityTypeIncorporatedCompanyReg,
      entityTypeIncorporatedCompanyUtrNfm = entityTypeIncorporatedCompanyUtr,
      entityTypePartnershipCompanyNameNfm = entityTypePartnershipCompanyName,
      entityTypePartnershipCompanyRegNfm = entityTypePartnershipCompanyReg,
      entityTypePartnershipCompanyUtrNfm = entityTypePartnershipCompanyUtr
    )
}

case class GroupDetails(
  mneOrDomestic:             MneOrDomestic,
  accountingSummaryPeriod:   String,
  accountingPeriodStartDate: LocalDate,
  accountingPeriodEndDate:   LocalDate
)

object GroupDetails {
  def populate(answers: UserAnswers): Option[GroupDetails] =
    for {
      mneOrDomestic    <- answers.get(SubMneOrDomesticPage)
      accountingPeriod <- answers.get(SubAccountingPeriodPage)
      startDate         = accountingPeriod.startDate
      endDate           = accountingPeriod.endDate
      accountingSummary = s"$startDate to $endDate"
    } yield GroupDetails(
      mneOrDomestic = mneOrDomestic,
      accountingSummaryPeriod = accountingSummary,
      accountingPeriodStartDate = startDate,
      accountingPeriodEndDate = endDate
    )
}
case class PrimaryContact(contactName: String, contactEmailAddress: String, contactByTelephone: Boolean, contactByTelephoneDetails: String)

object PrimaryContact {
  def populate(answers: UserAnswers): Option[PrimaryContact] =
    for {
      contactName               <- answers.get(SubPrimaryContactNamePage)
      contactEmailAddress       <- answers.get(SubPrimaryEmailPage)
      contactByTelephone        <- answers.get(SubPrimaryPhonePreferencePage)
      contactByTelephoneDetails <- answers.get(SubPrimaryCapturePhonePage)
    } yield PrimaryContact(
      contactName = contactName,
      contactEmailAddress = contactEmailAddress,
      contactByTelephone = contactByTelephone,
      contactByTelephoneDetails = contactByTelephoneDetails
    )
}
case class SecondaryContact(
  secondaryContact:             Boolean,
  secondaryContactName:         String,
  secondaryContactEmail:        String,
  secondaryTelephonePreference: Boolean,
  secondaryTelephone:           String
)

object SecondaryContact {
  def populate(answers: UserAnswers): Option[SecondaryContact] =
    for {
      secondaryContact             <- answers.get(SubAddSecondaryContactPage)
      secondaryContactName         <- answers.get(SubSecondaryContactNamePage)
      secondaryContactEmail        <- answers.get(SubSecondaryEmailPage)
      secondaryTelephonePreference <- answers.get(SubSecondaryPhonePreferencePage)
      secondaryTelephone           <- answers.get(SubSecondaryCapturePhonePage)
    } yield SecondaryContact(
      secondaryContact = secondaryContact,
      secondaryContactName = secondaryContactName,
      secondaryContactEmail = secondaryContactEmail,
      secondaryTelephonePreference = secondaryTelephonePreference,
      secondaryTelephone = secondaryTelephone
    )
}
case class Address(nonUkAddress: NonUKAddress)

object Address {
  def populate(answers: UserAnswers, countryOptions: CountryOptions): Option[NonUKAddress] =
    answers.get(SubRegisteredAddressPage)
}
