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

package models.tasklist

import base.SpecBase
import models.UserAnswers
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.TableFor3
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class FilingMemberDetailSectionSpec extends FilingMemberDetailSectionFixture with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "toRequiredSection" should {
    "call the correct url" in {
      val normalUrl = FilingMemberDetailSection.toRequiredSection(SectionStatus.NotStarted)

      normalUrl.url mustBe "/report-pillar2-top-up-taxes/business-matching/filing-member/nominate"
      normalUrl.method mustBe "GET"
    }
  }

  "name" should {
    "give correct edit message given Completed status" in {
      FilingMemberDetailSection.name(SectionStatus.Completed) mustBe "taskList.task.business.filingMember.edit"
    }

    "give correct message given any status that is NotStarted or InProgress" in {
      FilingMemberDetailSection.name(SectionStatus.NotStarted) mustBe "taskList.task.business.filingMember.add"
      FilingMemberDetailSection.name(SectionStatus.InProgress) mustBe "taskList.task.business.filingMember.add"
    }

    "give correct message given CannotStart" in {
      FilingMemberDetailSection.name(SectionStatus.CannotStart) mustBe "taskList.task.business.filingMember"
    }
  }

  "progress" should {
    forAll(progressScenarios) { (assertion: String, input: UserAnswers, result: SectionStatus) =>
      s"$assertion" in {
        FilingMemberDetailSection.progress(input) mustBe result
      }
    }
  }

  "prerequisiteSections" should {
    "have UltimateParentDetailSection as prerequisite section" in {
      FilingMemberDetailSection.prerequisiteSections(emptyUserAnswers) mustBe Set(UltimateParentDetailSection)

    }
  }
}

protected trait FilingMemberDetailSectionFixture extends SpecBase {

  val progressScenarios: TableFor3[String, UserAnswers, SectionStatus] = Table(
    ("assertion", "input", "result"),
    ("return InProgress if all answers for filing member detail section has not been completed", fmNoNameReg, SectionStatus.InProgress),
    (
      "return NotStarted if ultimate parent detail section has been completed and no other answers have not been attempted",
      upeCompletedGrsStatus,
      SectionStatus.NotStarted
    ),
    ("return Completed if all answers for filing member detail section have been completed", fmCompletedGrsResponse, SectionStatus.Completed)
  )
}
