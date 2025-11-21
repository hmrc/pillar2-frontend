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

class ReviewAndSubmitSectionSpec extends ReviewAndSubmitSectionFixture with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "toRequiredSection" should {
    "call the correct call" in {
      val call = ReviewAndSubmitSection.toRequiredSection(SectionStatus.NotStarted)

      call.url mustBe "/report-pillar2-top-up-taxes/review-submit/check-answers"
      call.method mustBe "GET"
    }
  }

  "name" should {
    "give correct name for any status" in {
      ReviewAndSubmitSection.name(SectionStatus.Completed) mustBe "taskList.task.review"
      ReviewAndSubmitSection.name(SectionStatus.InProgress) mustBe "taskList.task.review"
      ReviewAndSubmitSection.name(SectionStatus.NotStarted) mustBe "taskList.task.review"
      ReviewAndSubmitSection.name(SectionStatus.CannotStart) mustBe "taskList.task.review"
    }
  }

  "progress" should
    forAll(progressScenarios) { (assertion: String, input: UserAnswers, result: SectionStatus) =>
      s"$assertion" in {
        ReviewAndSubmitSection.progress(input) mustBe result
      }
    }

  "prerequisiteSections" should {
    "have UltimateParentDetailSection and FilingMemberDetailSection FurtherGroupDetailSection and ContactDetailSection as prerequisite section" in {
      ReviewAndSubmitSection.prerequisiteSections(emptyUserAnswers) mustBe Set(
        UltimateParentDetailSection,
        FilingMemberDetailSection,
        FurtherGroupDetailSection,
        ContactDetailSection
      )

    }
  }
}

protected trait ReviewAndSubmitSectionFixture extends SpecBase {

  val progressScenarios: TableFor3[String, UserAnswers, SectionStatus] = Table(
    ("assertion", "input", "result"),
    ("return CannotStart if sections have not been answered", emptyUserAnswers, SectionStatus.CannotStart),
    (
      "return NotStarted if all prerequisite sections have been completed",
      allSectionsCompleted,
      SectionStatus.NotStarted
    )
  )
}
