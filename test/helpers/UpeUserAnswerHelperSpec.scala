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

  "UserAnswerConnector" when {
    "must return the correct UPE contact name" in {
      val userAnswer = emptyUserAnswers.set(RegistrationPage, validNoIdRegData()).success.value
      userAnswer.upeUserName mustEqual "TestName"
    }

    "must return an empty string if no contact name is found" in {
      val userAnswer = emptyUserAnswers.set(RegistrationPage, validWithIdRegDataForLimitedCompany).success.value
      userAnswer.upeUserName mustEqual ""
    }
  }

}
