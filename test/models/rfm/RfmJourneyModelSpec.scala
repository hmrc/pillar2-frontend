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

package models.rfm

import models.grs.GrsRegistrationData
import models.{NonUKAddress, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues, TryValues}
import pages.*

class RfmJourneyModelSpec extends AnyFreeSpec with Matchers with OptionValues with EitherValues with TryValues {

  "from" - {

    val nonUkAddress: NonUKAddress = NonUKAddress("addressLine1", None, "addressLine3", None, None, countryCode = "US")
    val grsData = GrsRegistrationData("id", "name", "utr", "crn")

    "newNfm" - {

      "must return a completed journey model when the user has all answers for a non uk based company" in {
        val answers: UserAnswers = UserAnswers("id")
          .set(RfmCorporatePositionPage, CorporatePosition.NewNfm)
          .success
          .value
          .set(RfmUkBasedPage, false)
          .success
          .value
          .set(RfmNameRegistrationPage, "first last")
          .success
          .value
          .set(RfmRegisteredAddressPage, nonUkAddress)
          .success
          .value
          .set(RfmPrimaryContactNamePage, "primary name")
          .success
          .value
          .set(RfmPrimaryContactEmailPage, "primary@test.com")
          .success
          .value
          .set(RfmContactByPhonePage, true)
          .success
          .value
          .set(RfmCapturePrimaryPhonePage, "0191 123456789")
          .success
          .value
          .set(RfmAddSecondaryContactPage, true)
          .success
          .value
          .set(RfmSecondaryContactNamePage, "secondary name")
          .success
          .value
          .set(RfmSecondaryEmailPage, "secondary@test.com")
          .success
          .value
          .set(RfmSecondaryPhonePreferencePage, true)
          .success
          .value
          .set(RfmSecondaryCapturePhonePage, "0191 987654321")
          .success
          .value
          .set(RfmContactAddressPage, nonUkAddress)
          .success
          .value
        val expected = RfmJourneyModel(
          corporateStructurePosition = CorporatePosition.NewNfm,
          ukRegistered = Some(false),
          grsUkLimited = None,
          name = Some("first last"),
          registeredOfficeAddress = Some(NonUKAddress("addressLine1", None, "addressLine3", None, None, countryCode = "US")),
          primaryContactName = "primary name",
          primaryContactEmail = "primary@test.com",
          primaryContactByPhone = true,
          primaryContactPhone = Some("0191 123456789"),
          secondaryContact = true,
          secondaryContactName = Some("secondary name"),
          secondaryContactEmail = Some("secondary@test.com"),
          secondaryContactByPhone = Some(true),
          secondaryContactPhone = Some("0191 987654321"),
          contactAddress = NonUKAddress("addressLine1", None, "addressLine3", None, None, countryCode = "US")
        )
        RfmJourneyModel.from(answers).toOption.value mustEqual expected
      }

      "must return a completed journey model when the user has minimum answers for a non uk based company" in {
        val answers: UserAnswers = UserAnswers("id")
          .set(RfmCorporatePositionPage, CorporatePosition.NewNfm)
          .success
          .value
          .set(RfmUkBasedPage, false)
          .success
          .value
          .set(RfmNameRegistrationPage, "first last")
          .success
          .value
          .set(RfmRegisteredAddressPage, nonUkAddress)
          .success
          .value
          .set(RfmPrimaryContactNamePage, "primary name")
          .success
          .value
          .set(RfmPrimaryContactEmailPage, "primary@test.com")
          .success
          .value
          .set(RfmContactByPhonePage, false)
          .success
          .value
          .set(RfmAddSecondaryContactPage, false)
          .success
          .value
          .set(RfmContactAddressPage, nonUkAddress)
          .success
          .value
        val expected = RfmJourneyModel(
          corporateStructurePosition = CorporatePosition.NewNfm,
          ukRegistered = Some(false),
          grsUkLimited = None,
          name = Some("first last"),
          registeredOfficeAddress = Some(NonUKAddress("addressLine1", None, "addressLine3", None, None, countryCode = "US")),
          primaryContactName = "primary name",
          primaryContactEmail = "primary@test.com",
          primaryContactByPhone = false,
          primaryContactPhone = None,
          secondaryContact = false,
          secondaryContactName = None,
          secondaryContactEmail = None,
          secondaryContactByPhone = None,
          secondaryContactPhone = None,
          contactAddress = NonUKAddress("addressLine1", None, "addressLine3", None, None, countryCode = "US")
        )
        RfmJourneyModel.from(answers).toOption.value mustEqual expected
      }

      "must return a completed journey model when the user has all answers for a uk limited company" in {
        val answers: UserAnswers = UserAnswers("id")
          .set(RfmCorporatePositionPage, CorporatePosition.NewNfm)
          .success
          .value
          .set(RfmUkBasedPage, true)
          .success
          .value
          .set(RfmGrsDataPage, grsData)
          .success
          .value
          .set(RfmPrimaryContactNamePage, "primary name")
          .success
          .value
          .set(RfmPrimaryContactEmailPage, "primary@test.com")
          .success
          .value
          .set(RfmContactByPhonePage, true)
          .success
          .value
          .set(RfmCapturePrimaryPhonePage, "0191 123456789")
          .success
          .value
          .set(RfmAddSecondaryContactPage, true)
          .success
          .value
          .set(RfmSecondaryContactNamePage, "secondary name")
          .success
          .value
          .set(RfmSecondaryEmailPage, "secondary@test.com")
          .success
          .value
          .set(RfmSecondaryPhonePreferencePage, true)
          .success
          .value
          .set(RfmSecondaryCapturePhonePage, "0191 987654321")
          .success
          .value
          .set(RfmContactAddressPage, nonUkAddress)
          .success
          .value
        val expected = RfmJourneyModel(
          corporateStructurePosition = CorporatePosition.NewNfm,
          ukRegistered = Some(true),
          grsUkLimited = Some(GrsRegistrationData("id", "name", "utr", "crn")),
          name = None,
          registeredOfficeAddress = None,
          primaryContactName = "primary name",
          primaryContactEmail = "primary@test.com",
          primaryContactByPhone = true,
          primaryContactPhone = Some("0191 123456789"),
          secondaryContact = true,
          secondaryContactName = Some("secondary name"),
          secondaryContactEmail = Some("secondary@test.com"),
          secondaryContactByPhone = Some(true),
          secondaryContactPhone = Some("0191 987654321"),
          contactAddress = NonUKAddress("addressLine1", None, "addressLine3", None, None, countryCode = "US")
        )
        RfmJourneyModel.from(answers).toOption.value mustEqual expected
      }

      "must return a completed journey model when the user has minimum answers for a uk based company" in {
        val answers: UserAnswers = UserAnswers("id")
          .set(RfmCorporatePositionPage, CorporatePosition.NewNfm)
          .success
          .value
          .set(RfmUkBasedPage, true)
          .success
          .value
          .set(RfmGrsDataPage, grsData)
          .success
          .value
          .set(RfmPrimaryContactNamePage, "primary name")
          .success
          .value
          .set(RfmPrimaryContactEmailPage, "primary@test.com")
          .success
          .value
          .set(RfmContactByPhonePage, false)
          .success
          .value
          .set(RfmAddSecondaryContactPage, false)
          .success
          .value
          .set(RfmContactAddressPage, nonUkAddress)
          .success
          .value
        val expected = RfmJourneyModel(
          corporateStructurePosition = CorporatePosition.NewNfm,
          ukRegistered = Some(true),
          grsUkLimited = Some(GrsRegistrationData("id", "name", "utr", "crn")),
          name = None,
          registeredOfficeAddress = None,
          primaryContactName = "primary name",
          primaryContactEmail = "primary@test.com",
          primaryContactByPhone = false,
          primaryContactPhone = None,
          secondaryContact = false,
          secondaryContactName = None,
          secondaryContactEmail = None,
          secondaryContactByPhone = None,
          secondaryContactPhone = None,
          contactAddress = NonUKAddress("addressLine1", None, "addressLine3", None, None, countryCode = "US")
        )
        RfmJourneyModel.from(answers).toOption.value mustEqual expected
      }

      "must return all the pages which failed" in {
        val errors = RfmJourneyModel.from(UserAnswers("id")).left.value.toChain.toList
        errors must contain only (
          RfmCorporatePositionPage,
          RfmPrimaryContactNamePage,
          RfmPrimaryContactEmailPage,
          RfmContactByPhonePage,
          RfmAddSecondaryContactPage,
          RfmContactAddressPage
        )
      }

    }

    "upe" - {

      "must return a completed journey model when the user has all answers for a upe" in {
        val answers: UserAnswers = UserAnswers("id")
          .set(RfmCorporatePositionPage, CorporatePosition.Upe)
          .success
          .value
          .set(RfmPrimaryContactNamePage, "primary name")
          .success
          .value
          .set(RfmPrimaryContactEmailPage, "primary@test.com")
          .success
          .value
          .set(RfmContactByPhonePage, true)
          .success
          .value
          .set(RfmCapturePrimaryPhonePage, "0191 123456789")
          .success
          .value
          .set(RfmAddSecondaryContactPage, true)
          .success
          .value
          .set(RfmSecondaryContactNamePage, "secondary name")
          .success
          .value
          .set(RfmSecondaryEmailPage, "secondary@test.com")
          .success
          .value
          .set(RfmSecondaryPhonePreferencePage, true)
          .success
          .value
          .set(RfmSecondaryCapturePhonePage, "0191 987654321")
          .success
          .value
          .set(RfmContactAddressPage, nonUkAddress)
          .success
          .value
        val expected = RfmJourneyModel(
          corporateStructurePosition = CorporatePosition.Upe,
          ukRegistered = None,
          grsUkLimited = None,
          name = None,
          registeredOfficeAddress = None,
          primaryContactName = "primary name",
          primaryContactEmail = "primary@test.com",
          primaryContactByPhone = true,
          primaryContactPhone = Some("0191 123456789"),
          secondaryContact = true,
          secondaryContactName = Some("secondary name"),
          secondaryContactEmail = Some("secondary@test.com"),
          secondaryContactByPhone = Some(true),
          secondaryContactPhone = Some("0191 987654321"),
          contactAddress = NonUKAddress("addressLine1", None, "addressLine3", None, None, countryCode = "US")
        )
        RfmJourneyModel.from(answers).toOption.value mustEqual expected
      }

      "must return a completed journey model when the user has minimum answers for a upe" in {
        val answers: UserAnswers = UserAnswers("id")
          .set(RfmCorporatePositionPage, CorporatePosition.Upe)
          .success
          .value
          .set(RfmPrimaryContactNamePage, "primary name")
          .success
          .value
          .set(RfmPrimaryContactEmailPage, "primary@test.com")
          .success
          .value
          .set(RfmContactByPhonePage, false)
          .success
          .value
          .set(RfmAddSecondaryContactPage, false)
          .success
          .value
          .set(RfmContactAddressPage, nonUkAddress)
          .success
          .value
        val expected = RfmJourneyModel(
          corporateStructurePosition = CorporatePosition.Upe,
          ukRegistered = None,
          grsUkLimited = None,
          name = None,
          registeredOfficeAddress = None,
          primaryContactName = "primary name",
          primaryContactEmail = "primary@test.com",
          primaryContactByPhone = false,
          primaryContactPhone = None,
          secondaryContact = false,
          secondaryContactName = None,
          secondaryContactEmail = None,
          secondaryContactByPhone = None,
          secondaryContactPhone = None,
          contactAddress = NonUKAddress("addressLine1", None, "addressLine3", None, None, countryCode = "US")
        )
        RfmJourneyModel.from(answers).toOption.value mustEqual expected
      }

    }

  }
}
