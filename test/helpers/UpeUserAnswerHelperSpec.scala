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

package helpers

import base.SpecBase
import pages.RegistrationPage

class UpeUserAnswerHelperSpec extends SpecBase {

  "UpeUserAnswerConnector" when {
    "upeContactName" should {
      "return the correct UPE contact name" in {
        val userAnswer = emptyUserAnswers.set(RegistrationPage, validNoIdRegData()).success.value
        userAnswer.upeContactName mustEqual "TestName"
      }
      "return an empty string if it there is no contact name" in {
        val userAnswer = emptyUserAnswers.set(RegistrationPage, validIdRegistrationData).success.value
        userAnswer.upeContactName mustEqual null
      }
    }
    "upeGRSBookmarkLogic" should {

      "return Some true if the UPE is registered in the UK , status is in progress" in {
        val userAnswer = emptyUserAnswers.set(RegistrationPage, validIdRegistrationData).success.value
        userAnswer.upeGRSBookmarkLogic mustBe Some(true)
      }

      "return None if the UPE is not registered in the UK" in {
        val userAnswer = emptyUserAnswers.set(RegistrationPage, validNoIdRegDataforSub()).success.value
        userAnswer.upeGRSBookmarkLogic mustBe None
      }

      "return None if any upe data from the no ID journey exists in the database" in {
        val userAnswer = emptyUserAnswers.set(RegistrationPage, validNoIdRegDataforSub()).success.value
        userAnswer.upeGRSBookmarkLogic mustBe None
      }
    }

    "upeNameRegistration" should {
      "return the correct UPE name" in {
        val userAnswer = emptyUserAnswers.set(RegistrationPage, validNoIdRegData()).success.value
        userAnswer.upeNameRegistration mustEqual "Test Name"
      }
      "return an empty string if it there is no name" in {
        val userAnswer = emptyUserAnswers.set(RegistrationPage, validIdRegistrationData).success.value
        userAnswer.upeNameRegistration mustEqual null
      }
    }

    "upeNoIDBookmark" should {
      "return None if upe has gone through any steps of the GRS journey" in {
        val userAnswer = emptyUserAnswers.set(RegistrationPage, validIdRegistrationData).success.value
        userAnswer.upeNoIDBookmarkLogic mustBe None
      }
      "return true if upe is at any point during their NO ID flow" in {
        val userAnswer = emptyUserAnswers.set(RegistrationPage, validWithoutIdRegDataWithoutName()).success.value
        userAnswer.upeNoIDBookmarkLogic mustBe Some(true)
      }
    }

  }
}
