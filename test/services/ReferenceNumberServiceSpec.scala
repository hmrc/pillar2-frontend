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

package services

import base.SpecBase
import pages.plrReferencePage
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}

class ReferenceNumberServiceSpec extends SpecBase {

  "ReferenceNumberService" when {
    val enrolments: Set[Enrolment] = Set(
      Enrolment(
        key = "HMRC-PILLAR2-ORG",
        identifiers = Seq(
          EnrolmentIdentifier("PLRID", "enrolmentID"),
          EnrolmentIdentifier("UTR", "ABC12345")
        ),
        state = "activated"
      )
    )
    "return an optional pillar2 ID if it exists in the enrolment" in {
      val service = app.injector.instanceOf[ReferenceNumberService]
      service.get(emptyUserAnswers, Some(enrolments)) mustBe Some("enrolmentID")
    }
    "return a pillar2 id from userAnswers if none can be found in their enrolment" in {
      val userAnswers = emptyUserAnswers.setOrException(plrReferencePage, "databaseID")
      val service     = app.injector.instanceOf[ReferenceNumberService]
      service.get(userAnswers, None) mustBe Some("databaseID")
    }
    "priorities the ID received from enrolment" in {
      val userAnswers = emptyUserAnswers.setOrException(plrReferencePage, "databaseID")
      val service     = app.injector.instanceOf[ReferenceNumberService]
      service.get(userAnswers, Some(enrolments)) mustBe Some("enrolmentID")
    }
  }

}
