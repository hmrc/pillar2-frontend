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
import org.scalatest.prop.TableDrivenPropertyChecks.*
import org.scalatest.prop.TableFor3
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class UltimateParentDetailSectionSpec extends UltimateParentDetailSectionFixture with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "toRequiredSection" should {
    "call the correct url" in {
      val normalUrl = UltimateParentDetailSection.toRequiredSection(SectionStatus.NotStarted)

      normalUrl.url mustBe "/report-pillar2-top-up-taxes/business-matching/match-hmrc-records"
      normalUrl.method mustBe "GET"
    }
  }

  "name" should {
    "give correct edit message given Completed status" in {
      UltimateParentDetailSection.name(SectionStatus.Completed) mustBe "taskList.task.business.ultimate.edit"
    }

    "give correct add message given any status that is not Completed" in {
      UltimateParentDetailSection.name(SectionStatus.InProgress) mustBe "taskList.task.business.ultimate.add"
      UltimateParentDetailSection.name(SectionStatus.NotStarted) mustBe "taskList.task.business.ultimate.add"
      UltimateParentDetailSection.name(SectionStatus.CannotStart) mustBe "taskList.task.business.ultimate.add"
    }
  }

  "progress" should
    forAll(progressScenarios) { (assertion: String, input: UserAnswers, result: SectionStatus) =>
      s"$assertion" in {
        UltimateParentDetailSection.progress(input) mustBe result
      }
    }

  "prerequisiteSections" should {
    "be empty" in {
      UltimateParentDetailSection.prerequisiteSections(emptyUserAnswers) mustBe Set.empty
    }
  }
}

protected trait UltimateParentDetailSectionFixture extends SpecBase {

  val progressScenarios: TableFor3[String, UserAnswers, SectionStatus] = Table(
    ("assertion", "input", "result"),
    ("return InProgress if all answers for ultimate parent detail section has not been completed", upeInProgressUserAnswer, SectionStatus.InProgress),
    ("return NotStarted if answers have not been attempted", emptyUserAnswers, SectionStatus.NotStarted),
    ("return Completed if all answers for ultimate parent detail section have been completed", upeCompletedNoPhoneNumber, SectionStatus.Completed)
  )
}
