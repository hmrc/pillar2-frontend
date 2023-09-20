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

package controllers.testdata

import models.fm.{ContactNFMByTelephone, FilingMember, NfmRegisteredAddress, WithoutIdNfmData}
import models.grs.{EntityType, GrsCreateRegistrationResponse}
import models.registration._
import models.subscription.{AccountingPeriod, ContactByTelephone, Subscription, SubscriptionAddress}
import models.{ContactUPEByTelephone, MneOrDomestic, NfmRegisteredInUkConfirmation, NfmRegistrationConfirmation, UPERegisteredInUKConfirmation, UpeRegisteredAddress, UserAnswers}
import models.{ContactUPEByTelephone, MneOrDomestic, NfmRegisteredInUkConfirmation, NfmRegistrationConfirmation, UPERegisteredInUKConfirmation, UpeRegisteredAddress, UseContactPrimary, UserAnswers}
import play.api.libs.json.{JsObject, Json}
import utils.RowStatus

import java.time.{LocalDate, ZoneOffset}
trait Pillar2TestData {

  def upeCheckAnswerData() = new Registration(
    isUPERegisteredInUK = UPERegisteredInUKConfirmation.No,
    isRegistrationStatus = RowStatus.InProgress,
    withoutIdRegData = Some(
      WithoutIdRegData(
        upeNameRegistration = "Paddington",
        upeContactName = Some("Paddington ltd"),
        contactUpeByTelephone = Some(ContactUPEByTelephone.Yes),
        telephoneNumber = Some("123444"),
        emailAddress = Some("example@gmail.com"),
        upeRegisteredAddress = Some(
          UpeRegisteredAddress(
            addressLine1 = "1",
            addressLine2 = Some("2"),
            addressLine3 = "3",
            addressLine4 = Some("4"),
            postalCode = Some("5"),
            countryCode = "GB"
          )
        )
      )
    )
  )
  def upeCheckAnswerDataWithoutPhone() = new Registration(
    isUPERegisteredInUK = UPERegisteredInUKConfirmation.No,
    isRegistrationStatus = RowStatus.InProgress,
    withoutIdRegData = Some(
      WithoutIdRegData(
        upeNameRegistration = "Paddington",
        upeContactName = Some("Paddington ltd"),
        contactUpeByTelephone = Some(ContactUPEByTelephone.No),
        emailAddress = Some("example@gmail.com"),
        upeRegisteredAddress = Some(
          UpeRegisteredAddress(
            addressLine1 = "1",
            addressLine2 = Some("2"),
            addressLine3 = "3",
            addressLine4 = Some("4"),
            postalCode = Some("5"),
            countryCode = "GB"
          )
        )
      )
    )
  )
  def nfmCheckAnswerData() =
    new FilingMember(
      nfmConfirmation = NfmRegistrationConfirmation.Yes,
      isNfmRegisteredInUK = Some(NfmRegisteredInUkConfirmation.No),
      isNFMnStatus = RowStatus.InProgress,
      withoutIdRegData = Some(
        WithoutIdNfmData(
          registeredFmName = "Nfm name ",
          fmContactName = Some("Ashley Smith"),
          fmEmailAddress = Some("test@test.com"),
          contactNfmByTelephone = Some(ContactNFMByTelephone.Yes),
          telephoneNumber = Some("122223444"),
          registeredFmAddress = Some(
            NfmRegisteredAddress(
              addressLine1 = "1",
              addressLine2 = Some("2"),
              addressLine3 = "3",
              addressLine4 = Some("4"),
              postalCode = Some("5"),
              countryCode = "GB"
            )
          )
        )
      )
    )

  def subCheckAnswerData() =
    new Subscription(
      domesticOrMne = MneOrDomestic.Uk,
      groupDetailStatus = RowStatus.InProgress,
      contactDetailsStatus = RowStatus.NotStarted,
      accountingPeriod = Some(AccountingPeriod(LocalDate.parse("2023-12-31"), LocalDate.parse("2024-05-01")))
    )

  def ContactCheckAnswerWithSecondaryContactData() =
    new Subscription(
      domesticOrMne = MneOrDomestic.Uk,
      groupDetailStatus = RowStatus.Completed,
      contactDetailsStatus = RowStatus.InProgress,
      accountingPeriod = Some(AccountingPeriod(LocalDate.parse("2023-12-31"), LocalDate.parse("2024-05-01"))),
      useContactPrimary = Some(UseContactPrimary.Yes),
      primaryContactName = Some("Test Contact Name"),
      primaryContactEmail = Some("Test@test.com"),
      contactByTelephone = Some(ContactByTelephone.Yes),
      telephoneNumber = Some("1234567789"),
      addSecondaryContact = Some(true),
      secondaryContactName = Some("second Test name"),
      secondaryContactEmail = Some("secondemail@test.com"),
      secondaryTelephonePreference = Some(true),
      secondaryContactTelephone = Some("1234554221"),
      correspondenceAddress = Some(
        new SubscriptionAddress(
          addressLine1 = "ad1",
          addressLine2 = Some("ad2"),
          addressLine3 = "newcastle",
          addressLine4 = Some("northeast"),
          postalCode = Some("NE5 2DH"),
          countryCode = "BB"
        )
      )
    )

  def primaryContactCheckAnswerData() =
    new Subscription(
      domesticOrMne = MneOrDomestic.Uk,
      groupDetailStatus = RowStatus.Completed,
      contactDetailsStatus = RowStatus.InProgress,
      accountingPeriod = Some(AccountingPeriod(LocalDate.parse("2023-12-31"), LocalDate.parse("2024-05-01"))),
      useContactPrimary = Some(UseContactPrimary.Yes),
      primaryContactName = Some("Test Contact Name"),
      primaryContactEmail = Some("Test@test.com"),
      contactByTelephone = Some(ContactByTelephone.Yes),
      telephoneNumber = Some("1234567789"),
      correspondenceAddress = Some(
        new SubscriptionAddress(
          addressLine1 = "ad1",
          addressLine2 = Some("ad2"),
          addressLine3 = "newcastle",
          addressLine4 = Some("northeast"),
          postalCode = Some("NE5 2DH"),
          countryCode = "BB"
        )
      )
    )
  def subCheckAnswerDataUkAndOther() =
    new Subscription(
      domesticOrMne = MneOrDomestic.UkAndOther,
      groupDetailStatus = RowStatus.InProgress,
      contactDetailsStatus = RowStatus.NotStarted,
      accountingPeriod = Some(AccountingPeriod(LocalDate.parse("2023-12-31"), LocalDate.parse("2024-05-01")))
    )

  def validSubEmailData(
    domesticOrMne:        MneOrDomestic = MneOrDomestic.Uk,
    groupDetailStatus:    RowStatus = RowStatus.Completed,
    accountingPeriod:     AccountingPeriod = AccountingPeriod(LocalDate.parse("2023-12-31"), LocalDate.parse("2024-05-01")),
    contactDetailsStatus: RowStatus = RowStatus.InProgress,
    useContactPrimary:    UseContactPrimary = UseContactPrimary.No,
    primaryContactName:   String = "Ashley Smith"
  ) =
    new Subscription(
      domesticOrMne = domesticOrMne,
      groupDetailStatus = groupDetailStatus,
      accountingPeriod = Some(accountingPeriod),
      contactDetailsStatus = contactDetailsStatus,
      useContactPrimary = Some(useContactPrimary),
      primaryContactName = Some(primaryContactName)
    )

  def validSubPhoneCaptureData(
    domesticOrMne:        MneOrDomestic = MneOrDomestic.Uk,
    groupDetailStatus:    RowStatus = RowStatus.Completed,
    accountingPeriod:     AccountingPeriod = AccountingPeriod(LocalDate.parse("2023-12-31"), LocalDate.parse("2024-05-01")),
    contactDetailsStatus: RowStatus = RowStatus.InProgress,
    useContactPrimary:    UseContactPrimary = UseContactPrimary.No,
    primaryContactName:   String = "TestName",
    contactByTelephone:   ContactByTelephone = ContactByTelephone.No
  ) =
    new Subscription(
      domesticOrMne = domesticOrMne,
      groupDetailStatus = groupDetailStatus,
      accountingPeriod = Some(accountingPeriod),
      contactDetailsStatus = contactDetailsStatus,
      useContactPrimary = Some(useContactPrimary),
      primaryContactName = Some(primaryContactName),
      contactByTelephone = Some(contactByTelephone)
    )

  def validSubData(
    domesticOrMne:        MneOrDomestic = MneOrDomestic.Uk,
    groupDetailStatus:    RowStatus = RowStatus.Completed,
    accountingPeriod:     AccountingPeriod = AccountingPeriod(LocalDate.parse("2023-12-31"), LocalDate.parse("2024-05-01")),
    contactDetailsStatus: RowStatus = RowStatus.InProgress,
    useContactPrimary:    UseContactPrimary = UseContactPrimary.No,
    primaryContactName:   String = "Test Name",
    primaryContactEmail:  String = "testEmail@email.com"
  ) =
    new Subscription(
      domesticOrMne = domesticOrMne,
      groupDetailStatus = groupDetailStatus,
      accountingPeriod = Some(accountingPeriod),
      contactDetailsStatus = contactDetailsStatus,
      useContactPrimary = Some(useContactPrimary),
      primaryContactName = Some(primaryContactName),
      primaryContactEmail = Some(primaryContactEmail)
    )

  def validSubPhoneData(
    domesticOrMne:        MneOrDomestic = MneOrDomestic.Uk,
    groupDetailStatus:    RowStatus = RowStatus.Completed,
    accountingPeriod:     AccountingPeriod = AccountingPeriod(LocalDate.parse("2023-12-31"), LocalDate.parse("2024-05-01")),
    contactDetailsStatus: RowStatus = RowStatus.InProgress,
    useContactPrimary:    UseContactPrimary = UseContactPrimary.No,
    primaryContactName:   String = "Test Name",
    primaryContactEmail:  String = "testEmail@email.com",
    contactByTelephone:   ContactByTelephone = ContactByTelephone.No
  ) =
    new Subscription(
      domesticOrMne = domesticOrMne,
      groupDetailStatus = groupDetailStatus,
      accountingPeriod = Some(accountingPeriod),
      contactDetailsStatus = contactDetailsStatus,
      useContactPrimary = Some(useContactPrimary),
      primaryContactName = Some(primaryContactName),
      primaryContactEmail = Some(primaryContactEmail),
      contactByTelephone = Some(contactByTelephone)
    )

  def nfmCheckAnswerDataWithoutPhone() = new FilingMember(
    nfmConfirmation = NfmRegistrationConfirmation.Yes,
    isNfmRegisteredInUK = Some(NfmRegisteredInUkConfirmation.No),
    isNFMnStatus = RowStatus.InProgress,
    withoutIdRegData = Some(
      WithoutIdNfmData(
        registeredFmName = "Fm Name Ashley",
        fmContactName = Some("Ashley Smith"),
        fmEmailAddress = Some("test@test.com"),
        contactNfmByTelephone = Some(ContactNFMByTelephone.No),
        registeredFmAddress = Some(
          NfmRegisteredAddress(
            addressLine1 = "1",
            addressLine2 = Some("2"),
            addressLine3 = "3",
            addressLine4 = Some("4"),
            postalCode = Some("ne5 2dh"),
            countryCode = "GB"
          )
        )
      )
    )
  )

  def validNoIdRegDataforSub(
    isUPERegisteredInUK:   UPERegisteredInUKConfirmation = UPERegisteredInUKConfirmation.No,
    isRegistrationStatus:  RowStatus = RowStatus.Completed,
    upeNameRegistration:   String = "Test Name",
    upeContactName:        Option[String] = Some("TestName"),
    contactUpeByTelephone: Option[ContactUPEByTelephone] = Some(ContactUPEByTelephone.Yes),
    telephoneNumber:       Option[String] = Some("1234567"),
    emailAddress:          Option[String] = Some("test@test.com"),
    addressLine1:          String = "Line1",
    addressLine2:          Option[String] = Some("Line2"),
    addressLine3:          String = "Line3",
    addressLine4:          Option[String] = Some("Line4"),
    postalCode:            Option[String] = Some("VR11 3PA"),
    countryCode:           String = "GB"
  ) =
    new Registration(
      isUPERegisteredInUK = isUPERegisteredInUK,
      isRegistrationStatus = isRegistrationStatus,
      withoutIdRegData = Some(
        WithoutIdRegData(
          upeNameRegistration = upeNameRegistration,
          upeContactName = upeContactName,
          contactUpeByTelephone = contactUpeByTelephone,
          telephoneNumber = telephoneNumber,
          emailAddress = emailAddress,
          upeRegisteredAddress = Some(
            UpeRegisteredAddress(
              addressLine1 = addressLine1,
              addressLine2 = addressLine2,
              addressLine3 = addressLine3,
              addressLine4 = addressLine4,
              postalCode = postalCode,
              countryCode = countryCode
            )
          )
        )
      )
    )
  def validNoIdRegData(
    isUPERegisteredInUK:   UPERegisteredInUKConfirmation = UPERegisteredInUKConfirmation.No,
    isRegistrationStatus:  RowStatus = RowStatus.InProgress,
    upeNameRegistration:   String = "Test Name",
    upeContactName:        Option[String] = Some("TestName"),
    contactUpeByTelephone: Option[ContactUPEByTelephone] = Some(ContactUPEByTelephone.Yes),
    telephoneNumber:       Option[String] = Some("1234567"),
    emailAddress:          Option[String] = Some("test@test.com"),
    addressLine1:          String = "Line1",
    addressLine2:          Option[String] = Some("Line2"),
    addressLine3:          String = "Line3",
    addressLine4:          Option[String] = Some("Line4"),
    postalCode:            Option[String] = Some("VR11 3PA"),
    countryCode:           String = "GB"
  ) =
    new Registration(
      isUPERegisteredInUK = isUPERegisteredInUK,
      isRegistrationStatus = isRegistrationStatus,
      withoutIdRegData = Some(
        WithoutIdRegData(
          upeNameRegistration = upeNameRegistration,
          upeContactName = upeContactName,
          contactUpeByTelephone = contactUpeByTelephone,
          telephoneNumber = telephoneNumber,
          emailAddress = emailAddress,
          upeRegisteredAddress = Some(
            UpeRegisteredAddress(
              addressLine1 = addressLine1,
              addressLine2 = addressLine2,
              addressLine3 = addressLine3,
              addressLine4 = addressLine4,
              postalCode = postalCode,
              countryCode = countryCode
            )
          )
        )
      )
    )

  def validWithIdFmData(
    nfmConfirmation:     NfmRegistrationConfirmation = NfmRegistrationConfirmation.Yes,
    isNfmRegisteredInUK: Option[NfmRegisteredInUkConfirmation] = None,
    isNFMnStatus:        RowStatus = RowStatus.InProgress,
    orgType:             Option[EntityType] = None,
    withIdRegData:       Option[GrsResponse] = None,
    withoutIdRegData:    Option[WithoutIdNfmData] = None
  ) =
    new FilingMember(
      nfmConfirmation = nfmConfirmation,
      isNfmRegisteredInUK = isNfmRegisteredInUK,
      isNFMnStatus = isNFMnStatus,
      orgType = orgType,
      withIdRegData = withIdRegData,
      withoutIdRegData = withoutIdRegData
    )

  def validSubscriptionData(
  ) =
    new Subscription(
      domesticOrMne = MneOrDomestic.Uk,
      accountingPeriod = Some(AccountingPeriod(LocalDate.parse("2023-12-31"), LocalDate.parse("2024-05-01"))),
      groupDetailStatus = RowStatus.Completed,
      contactDetailsStatus = RowStatus.InProgress
    )

  def validSubscriptionDataWithUsePrimaryName(
  ) =
    new Subscription(
      domesticOrMne = MneOrDomestic.Uk,
      groupDetailStatus = RowStatus.Completed,
      useContactPrimary = Some(UseContactPrimary.No),
      contactDetailsStatus = RowStatus.InProgress
    )
  def validNoIdFmData(
    nfmConfirmation:       NfmRegistrationConfirmation = NfmRegistrationConfirmation.Yes,
    isNfmRegisteredInUK:   Option[NfmRegisteredInUkConfirmation] = None,
    isNFMnStatus:          RowStatus = RowStatus.InProgress,
    nfmNameRegistration:   String = "Test Name",
    nfmContactName:        Option[String] = Some("TestName"),
    contactNfmByTelephone: Option[ContactNFMByTelephone] = Some(ContactNFMByTelephone.Yes),
    telephoneNumber:       Option[String] = Some("1234567"),
    fmEmailAddress:        Option[String] = Some("test@test.com"),
    fmAddressLine1:        String = "Line1",
    fmAddressLine2:        Option[String] = Some("Line2"),
    fmAddressLine3:        String = "Line3",
    fmAddressLine4:        Option[String] = Some("Line4"),
    fmPostalCode:          Option[String] = Some("VR11 3PA"),
    fmCountryCode:         String = "GB"
  ) =
    new FilingMember(
      nfmConfirmation = nfmConfirmation,
      isNfmRegisteredInUK = isNfmRegisteredInUK,
      isNFMnStatus = isNFMnStatus,
      withoutIdRegData = Some(
        WithoutIdNfmData(
          registeredFmName = nfmNameRegistration,
          fmContactName = nfmContactName,
          fmEmailAddress = fmEmailAddress,
          contactNfmByTelephone = contactNfmByTelephone,
          telephoneNumber = telephoneNumber,
          registeredFmAddress = Some(
            NfmRegisteredAddress(
              addressLine1 = fmAddressLine1,
              addressLine2 = fmAddressLine2,
              addressLine3 = fmAddressLine3,
              addressLine4 = fmAddressLine4,
              postalCode = fmPostalCode,
              countryCode = fmCountryCode
            )
          )
        )
      )
    )

  def validNoIdNfmDataForContactEmail = new FilingMember(
    NfmRegistrationConfirmation.Yes,
    Some(NfmRegisteredInUkConfirmation.No),
    isNFMnStatus = RowStatus.InProgress,
    withoutIdRegData = Some(WithoutIdNfmData("test name", fmContactName = Some("Ashley Smith")))
  )

  def validNoIdNfmDataDefForContactName = new FilingMember(
    NfmRegistrationConfirmation.Yes,
    Some(NfmRegisteredInUkConfirmation.No),
    isNFMnStatus = RowStatus.InProgress,
    withoutIdRegData = Some(WithoutIdNfmData("test name", registeredFmAddress = Some(validNfmRegisteredAddress)))
  )

  val validNfmRegisteredAddress = new NfmRegisteredAddress(
    addressLine1 = "Line1",
    addressLine2 = Some("Line2"),
    addressLine3 = "Line3",
    addressLine4 = Some("Line4"),
    postalCode = Some("VR11 3PA"),
    countryCode = "IN"
  )
  def validWithIdFmDataName(
    nfmConfirmation:     NfmRegistrationConfirmation = NfmRegistrationConfirmation.Yes,
    isNfmRegisteredInUK: Option[NfmRegisteredInUkConfirmation] = Some(NfmRegisteredInUkConfirmation.No),
    isNFMnStatus:        RowStatus = RowStatus.InProgress,
    orgType:             Option[EntityType] = None,
    withIdRegData:       Option[GrsResponse] = None,
    withoutIdRegData:    Option[WithoutIdNfmData] = None
  ) =
    new FilingMember(
      nfmConfirmation = nfmConfirmation,
      isNfmRegisteredInUK = isNfmRegisteredInUK,
      isNFMnStatus = isNFMnStatus,
      orgType = orgType,
      withIdRegData = withIdRegData,
      withoutIdRegData = withoutIdRegData
    )

  def validWithoutIdFmDataAddress(
    nfmConfirmation:     NfmRegistrationConfirmation = NfmRegistrationConfirmation.Yes,
    isNfmRegisteredInUK: Option[NfmRegisteredInUkConfirmation] = Some(NfmRegisteredInUkConfirmation.Yes),
    isNFMnStatus:        RowStatus = RowStatus.InProgress,
    orgType:             Option[EntityType] = None,
    withIdRegData:       Option[GrsResponse] = None,
    withoutIdRegData:    Option[WithoutIdNfmData] = Some(WithoutIdNfmData(registeredFmName = "Name"))
  ) =
    new FilingMember(
      nfmConfirmation = nfmConfirmation,
      isNfmRegisteredInUK = isNfmRegisteredInUK,
      isNFMnStatus = isNFMnStatus,
      orgType = orgType,
      withIdRegData = withIdRegData,
      withoutIdRegData = withoutIdRegData
    )

  def validNoIdNfmData = new FilingMember(
    NfmRegistrationConfirmation.Yes,
    Some(NfmRegisteredInUkConfirmation.No),
    isNFMnStatus = RowStatus.InProgress,
    withoutIdRegData = Some(WithoutIdNfmData("test name", registeredFmAddress = Some(validNfmRegisteredAddress)))
  )

  def validYesIdNfmData = new FilingMember(
    NfmRegistrationConfirmation.Yes,
    Some(NfmRegisteredInUkConfirmation.Yes),
    isNFMnStatus = RowStatus.InProgress,
    withoutIdRegData = Some(WithoutIdNfmData("test name", registeredFmAddress = Some(validNfmRegisteredAddress)))
  )

  def validWithIdRegDataForLimitedCompany =
    new Registration(
      isUPERegisteredInUK = UPERegisteredInUKConfirmation.Yes,
      isRegistrationStatus = RowStatus.InProgress,
      orgType = Some(EntityType.UkLimitedCompany),
      withIdRegData = Some(
        new GrsResponse(
          incorporatedEntityRegistrationData = Some(Json.parse(validRegistrationWithIdResponse()).as[IncorporatedEntityRegistrationData])
        )
      )
    )

  def validWithIdRegDataForLLP =
    new Registration(
      isUPERegisteredInUK = UPERegisteredInUKConfirmation.Yes,
      isRegistrationStatus = RowStatus.InProgress,
      orgType = Some(EntityType.LimitedLiabilityPartnership),
      withIdRegData = Some(
        new GrsResponse(
          partnershipEntityRegistrationData = Some(Json.parse(validRegistrationWithIdResponseForLLP()).as[PartnershipEntityRegistrationData])
        )
      )
    )

  def validWithIdFmRegistrationDataForLimitedComp =
    new FilingMember(
      nfmConfirmation = NfmRegistrationConfirmation.Yes,
      isNfmRegisteredInUK = Some(NfmRegisteredInUkConfirmation.Yes),
      isNFMnStatus = RowStatus.InProgress,
      orgType = Some(EntityType.UkLimitedCompany),
      withIdRegData = Some(
        new GrsResponse(
          incorporatedEntityRegistrationData = Some(Json.parse(validRegistrationWithIdResponse()).as[IncorporatedEntityRegistrationData])
        )
      )
    )

  def validWithIdFmRegistrationDataForPartnership =
    new FilingMember(
      nfmConfirmation = NfmRegistrationConfirmation.Yes,
      isNfmRegisteredInUK = Some(NfmRegisteredInUkConfirmation.Yes),
      isNFMnStatus = RowStatus.InProgress,
      orgType = Some(EntityType.LimitedLiabilityPartnership),
      withIdRegData = Some(
        new GrsResponse(
          partnershipEntityRegistrationData = Some(Json.parse(validRegistrationWithIdResponseForLLP()).as[PartnershipEntityRegistrationData])
        )
      )
    )
  def validWithoutIdRegData(
    isUPERegisteredInUK:  UPERegisteredInUKConfirmation = UPERegisteredInUKConfirmation.No,
    isRegistrationStatus: RowStatus = RowStatus.InProgress
  ) =
    new Registration(isUPERegisteredInUK = isUPERegisteredInUK, isRegistrationStatus = isRegistrationStatus, withoutIdRegData = None)

  def validWithoutIdRegDataWithName(
    isUPERegisteredInUK:  UPERegisteredInUKConfirmation = UPERegisteredInUKConfirmation.No,
    isRegistrationStatus: RowStatus = RowStatus.InProgress
  ) =
    new Registration(
      isUPERegisteredInUK = isUPERegisteredInUK,
      isRegistrationStatus = isRegistrationStatus,
      withoutIdRegData = Some(WithoutIdRegData(upeNameRegistration = "Test Name"))
    )

  def validWithoutIdRegDataWithoutName(
    isUPERegisteredInUK:  UPERegisteredInUKConfirmation = UPERegisteredInUKConfirmation.No,
    isRegistrationStatus: RowStatus = RowStatus.InProgress
  ) =
    new Registration(
      isUPERegisteredInUK = isUPERegisteredInUK,
      isRegistrationStatus = isRegistrationStatus
    )

  def validUpeRegisteredAddressed = new UpeRegisteredAddress(
    addressLine1 = "Line1",
    addressLine2 = Some("Line2"),
    addressLine3 = "Line3",
    addressLine4 = Some("Line4"),
    postalCode = Some("VR11 3PA"),
    countryCode = "GB"
  )

  val validIdRegistrationData =
    new Registration(
      isUPERegisteredInUK = UPERegisteredInUKConfirmation.Yes,
      isRegistrationStatus = RowStatus.InProgress,
      orgType = Some(EntityType.UkLimitedCompany),
      withIdRegData = Some(
        GrsResponse(incorporatedEntityRegistrationData = Some(Json.parse(validRegistrationWithIdResponse()).as[IncorporatedEntityRegistrationData]))
      )
    )
  def validIdRegistrationDataWithNoOrgType =
    new Registration(
      isUPERegisteredInUK = UPERegisteredInUKConfirmation.Yes,
      isRegistrationStatus = RowStatus.InProgress,
      orgType = None,
      withIdRegData = None
    )

  val validGrsResponse = new GrsResponse(
    incorporatedEntityRegistrationData = Some(Json.parse(validRegistrationWithIdResponse()).as[IncorporatedEntityRegistrationData])
  )
  val validRegisterWithIdResponse = Json.parse(validRegistrationWithIdResponse()).as[IncorporatedEntityRegistrationData]

  val validRegisterWithIdResponseForLLP = Json.parse(validRegistrationWithIdResponseForLLP()).as[PartnershipEntityRegistrationData]

  val validUserAnswersGrsDataForLimitedCompany = UserAnswers(
    "testId",
    data = Json.parse(validDataObjectForUKLimtedCompany).as[JsObject]
  )
  val validUserAnswersGrsDataForLLP = UserAnswers(
    "testId",
    data = Json.parse(validDataObjectForLLP()).as[JsObject]
  )

  val validFilingMemberUserAnswersGrsDataForLimitedCompany = UserAnswers(
    "testId",
    data = Json.parse(validFilingMemberDataObjectForUKLimtedCompany).as[JsObject]
  )

  val validFilingMemberUserAnswersGrsDataForLLP = UserAnswers(
    "testId",
    data = Json.parse(validFilingMemberDataObjectForLLP).as[JsObject]
  )

  val validGrsCreateRegistrationResponse = new GrsCreateRegistrationResponse("http://journey-start")

  def validRegistrationWithIdResponse(): String =
    s"""{
       |            "companyProfile" : {
       |                "companyName" : "Test Example Company Name",
       |                "companyNumber" : "76543210",
       |                "dateOfIncorporation" : "2010-12-12",
       |                "unsanitisedCHROAddress" : {
       |                    "address_line_1" : "Address Line 1",
       |                    "address_line_2" : "Address Line 2",
       |                    "locality" : "Town",
       |                    "postal_code" : "AB12 3CD",
       |                    "region" : "Region"
       |                }
       |            },
       |            "ctutr" : "1234567890",
       |            "identifiersMatch" : true,
       |            "registration" : {
       |                "registrationStatus" : "REGISTERED",
       |                "registeredBusinessPartnerId" : "XB0000000000001"
       |            }
       |  }

       """.stripMargin

  def validRegistrationWithIdResponseForLLP(): String =
    s"""{
       |  "companyProfile" : {
       |                "companyName" : "Test Example Partnership Name",
       |                "companyNumber" : "76543210",
       |                "dateOfIncorporation" : "2010-12-12",
       |                "unsanitisedCHROAddress" : {
       |                    "address_line_1" : "Address Line 1",
       |                    "address_line_2" : "Address Line 2",
       |                    "country" : "United Kingdom",
       |                    "locality" : "Town",
       |                    "postal_code" : "AB12 3CD",
       |                    "region" : "Region"
       |                }
       |            },
       |            "sautr" : "1234567890",
       |            "postcode" : "AA11AA",
       |            "identifiersMatch" : true,
       |            "registration" : {
       |                "registrationStatus" : "REGISTERED",
       |                "registeredBusinessPartnerId" : "XB0000000000001"
       |            }
       |  }

       """.stripMargin

  def validDataObjectForUKLimtedCompany(): String =
    s"""
      {
       |        "Registration" : {
       |            "isUPERegisteredInUK" : "yes",
       |            "isRegistrationStatus" : "InProgress",
       |            "orgType" : "ukLimitedCompany",
       |            "withIdRegData" : {
       |                "incorporatedEntityRegistrationData" : {
       |                    "companyProfile" : {
       |                        "companyName" : "Test Example Company Name",
       |                        "companyNumber" : "76543210",
       |                        "dateOfIncorporation" : "2010-12-12",
       |                        "unsanitisedCHROAddress" : {
       |                            "address_line_1" : "Address Line 1",
       |                            "address_line_2" : "Address Line 2",
       |                            "country" : "United Kingdom",
       |                            "locality" : "Town",
       |                            "postal_code" : "AB12 3CD",
       |                            "region" : "Region"
       |                        }
       |                    },
       |                    "ctutr" : "1234567890",
       |                    "identifiersMatch" : true,
       |                    "registration" : {
       |                        "registrationStatus" : "REGISTERED",
       |                        "registeredBusinessPartnerId" : "XB0000000000001"
       |                    }
       |                }
       |            }
       |        }
       |    }
       """.stripMargin

  def validDataObjectForLLP(): String =
    s"""
       |{
       |        "Registration" : {
       |            "isUPERegisteredInUK" : "yes",
       |            "isRegistrationStatus" : "InProgress",
       |            "orgType" : "limitedLiabilityPartnership",
       |            "withIdRegData" : {
       |                "partnershipEntityRegistrationData" : {
       |                    "companyProfile" : {
       |                        "companyName" : "Test Example Partnership Name",
       |                        "companyNumber" : "76543210",
       |                        "dateOfIncorporation" : "2010-12-12",
       |                        "unsanitisedCHROAddress" : {
       |                            "address_line_1" : "Address Line 1",
       |                            "address_line_2" : "Address Line 2",
       |                            "country" : "United Kingdom",
       |                            "locality" : "Town",
       |                            "postal_code" : "AB12 3CD",
       |                            "region" : "Region"
       |                        }
       |                    },
       |                    "sautr" : "1234567890",
       |                    "postcode" : "AA11AA",
       |                    "identifiersMatch" : true,
       |                    "registration" : {
       |                        "registrationStatus" : "REGISTERED",
       |                        "registeredBusinessPartnerId" : "XB0000000000001"
       |                    }
       |                }
       |            }
       |        }
       |    }
       """.stripMargin

  def validFilingMemberDataObjectForUKLimtedCompany(): String =
    s"""
      {
       |        "FilingMember" : {
       |            "nfmConfirmation" : "yes",
       |            "isNfmRegisteredInUK" : "yes",
       |            "orgType" : "ukLimitedCompany",
       |            "isNFMnStatus" : "Completed",
       |            "withIdRegData" : {
       |                "incorporatedEntityRegistrationData" : {
       |                    "companyProfile" : {
       |                        "companyName" : "Test Example Company Name",
       |                        "companyNumber" : "76543210",
       |                        "dateOfIncorporation" : "2010-12-12",
       |                        "unsanitisedCHROAddress" : {
       |                            "address_line_1" : "Address Line 1",
       |                            "address_line_2" : "Address Line 2",
       |                            "country" : "United Kingdom",
       |                            "locality" : "Town",
       |                            "postal_code" : "AB12 3CD",
       |                            "region" : "Region"
       |                        }
       |                    },
       |                    "ctutr" : "1234567890",
       |                    "identifiersMatch" : true,
       |                    "registration" : {
       |                        "registrationStatus" : "REGISTERED",
       |                        "registeredBusinessPartnerId" : "XB0000000000001"
       |                    }
       |                }
       |            }
       |        }
       |    }
       """.stripMargin

  def validFilingMemberDataObjectForLLP(): String =
    s"""
      {
       |   "FilingMember" : {
       |            "nfmConfirmation" : "yes",
       |            "isNfmRegisteredInUK" : "yes",
       |            "orgType" : "limitedLiabilityPartnership",
       |            "isNFMnStatus" : "Completed",
       |            "withIdRegData" : {
       |                "partnershipEntityRegistrationData" : {
       |                    "companyProfile" : {
       |                        "companyName" : "Test Example Partnership Name",
       |                        "companyNumber" : "76543210",
       |                        "dateOfIncorporation" : "2010-12-12",
       |                        "unsanitisedCHROAddress" : {
       |                            "address_line_1" : "Address Line 1",
       |                            "address_line_2" : "Address Line 2",
       |                            "country" : "United Kingdom",
       |                            "locality" : "Town",
       |                            "postal_code" : "AB12 3CD",
       |                            "region" : "Region"
       |                        }
       |                    },
       |                    "sautr" : "1234567890",
       |                    "postcode" : "AA11AA",
       |                    "identifiersMatch" : true,
       |                    "registration" : {
       |                        "registrationStatus" : "REGISTERED",
       |                        "registeredBusinessPartnerId" : "XB0000000000001"
       |                    }
       |                }
       |            }
       |        }
       |    }
       """.stripMargin

}
