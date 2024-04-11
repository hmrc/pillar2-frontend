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

import base.SpecBase
import models.registration._
import models.rfm.RegistrationDate
import models.{EnrolmentInfo, NonUKAddress, UKAddress, UserAnswers}
import pages._
import utils.RowStatus

import java.time.LocalDate
import scala.util.Try

class ReplaceFilingMemberHelpersSpec extends SpecBase {

  val primaryAndSecondaryContactData: UserAnswers = emptyUserAnswers
    .setOrException(RfmPrimaryContactNamePage, "primary name")
    .setOrException(RfmPrimaryContactEmailPage, "email@address.com")
    .setOrException(RfmContactByTelephonePage, true)
    .setOrException(RfmCapturePrimaryTelephonePage, "1234567890")
    .setOrException(RfmAddSecondaryContactPage, true)
    .setOrException(RfmSecondaryContactNamePage, "secondary name")
    .setOrException(RfmSecondaryEmailPage, "email@address.com")
    .setOrException(RfmSecondaryPhonePreferencePage, true)
    .setOrException(RfmSecondaryCapturePhonePage, "1234567891")
    .setOrException(RfmContactAddressPage, NonUKAddress("line1", None, "line3", None, None, countryCode = "US"))

  "Replace Filing Member Helper" when {
    "Contact Detail Status" should {
      "return true if all questions are answered" in {
        val userAnswer = primaryAndSecondaryContactData
        userAnswer.rfmContactDetailStatus mustEqual true
      }
      "return true if no primary telephone contact and all other contact questions are answered" in {
        val userAnswer = primaryAndSecondaryContactData.setOrException(RfmContactByTelephonePage, false)
        userAnswer.rfmContactDetailStatus mustEqual true
      }
      "return true if no primary & secondary telephone contact and all other contact questions are answered" in {
        val userAnswer = primaryAndSecondaryContactData
          .setOrException(RfmContactByTelephonePage, false)
          .setOrException(RfmSecondaryPhonePreferencePage, false)
        userAnswer.rfmContactDetailStatus mustEqual true
      }
      "return true if primary telephone and no secondary contact and all other contact questions are answered" in {
        val userAnswer = primaryAndSecondaryContactData
          .setOrException(RfmContactByTelephonePage, false)
          .setOrException(RfmAddSecondaryContactPage, false)
        userAnswer.rfmContactDetailStatus mustEqual true
      }
      "return true if no secondary contact and all other contact questions are answered" in {
        val userAnswer = primaryAndSecondaryContactData
          .setOrException(RfmAddSecondaryContactPage, false)
        userAnswer.rfmContactDetailStatus mustEqual true
      }
      "return false if primary contact name is not answered" in {
        val userAnswer = primaryAndSecondaryContactData.remove(RfmPrimaryContactNamePage).success.value
        userAnswer.rfmContactDetailStatus mustBe false
      }
      "return false if primary contact email is not answered" in {
        val userAnswer = primaryAndSecondaryContactData.remove(RfmPrimaryContactEmailPage).success.value
        userAnswer.rfmContactDetailStatus mustBe false
      }
      "return false if primary contact by telephone is true and primary telephone is not answered" in {
        val userAnswer = primaryAndSecondaryContactData.remove(RfmCapturePrimaryTelephonePage).success.value
        userAnswer.rfmContactDetailStatus mustBe false
      }
      "return false if add secondary contact is true and secondary contact name is not answered" in {
        val userAnswer = primaryAndSecondaryContactData.remove(RfmSecondaryContactNamePage).success.value
        userAnswer.rfmContactDetailStatus mustBe false
      }
      "return false if add secondary contact is true and secondary contact email is not answered" in {
        val userAnswer = primaryAndSecondaryContactData.remove(RfmSecondaryEmailPage).success.value
        userAnswer.rfmContactDetailStatus mustBe false
      }
      "return false if secondary contact by telephone is true and secondary contact telephone is not answered" in {
        val userAnswer = primaryAndSecondaryContactData.remove(RfmSecondaryCapturePhonePage).success.value
        userAnswer.rfmContactDetailStatus mustBe false
      }
      "return false if contact address is not answered" in {
        val userAnswer = primaryAndSecondaryContactData.remove(RfmContactAddressPage).success.value
        userAnswer.rfmContactDetailStatus mustBe false
      }
    }
  }

}
