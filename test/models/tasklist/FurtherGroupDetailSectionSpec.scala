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

class FurtherGroupDetailSectionSpec extends FurtherGroupDetailSectionFixture with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "toRequiredSection" should {
    "call the correct url" in {
      val normalUrl = FurtherGroupDetailSection.toRequiredSection(SectionStatus.NotStarted)

      normalUrl.url mustBe "/report-pillar2-top-up-taxes/further-details/group-status"
      normalUrl.method mustBe "GET"
    }
  }

  "name" should {
    "give correct edit message given Completed status" in {
      FurtherGroupDetailSection.name(SectionStatus.Completed) mustBe "taskList.task.business.sub.edit"
    }

    "give correct message given any status that is NotStarted or InProgress" in {
      FurtherGroupDetailSection.name(SectionStatus.NotStarted) mustBe "taskList.task.business.sub.add"
      FurtherGroupDetailSection.name(SectionStatus.InProgress) mustBe "taskList.task.business.sub.add"
    }

    "give correct message given CannotStart" in {
      FurtherGroupDetailSection.name(SectionStatus.CannotStart) mustBe "taskList.task.business.more"
    }
  }

  "progress" should {
    forAll(progressScenarios) { (assertion: String, input: UserAnswers, result: SectionStatus) =>
      s"$assertion" in {
        FurtherGroupDetailSection.progress(input) mustBe result
      }
    }
  }

  "prerequisiteSections" should {
    "have UltimateParentDetailSection and FilingMemberDetailSection as prerequisite section" in {
      FurtherGroupDetailSection.prerequisiteSections(emptyUserAnswers) mustBe Set(UltimateParentDetailSection, FilingMemberDetailSection)

    }
  }
}

protected trait FurtherGroupDetailSectionFixture extends SpecBase {

  val progressScenarios: TableFor3[String, UserAnswers, SectionStatus] = Table(
    ("assertion", "input", "result"),
    ("return InProgress if all answers for further group detail section has not been completed", groupDetailInProgress, SectionStatus.InProgress),
    (
      "return NotStarted if ultimate parent and filing member detail sections has been completed and no other answers have not been attempted",
      fmCompletedGrsResponse,
      SectionStatus.NotStarted
    ),
    ("return Completed if all answers for further group detail section have been completed", groupDetailCompleted, SectionStatus.Completed)
  )
}
