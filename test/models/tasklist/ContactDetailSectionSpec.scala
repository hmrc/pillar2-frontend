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
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ContactDetailSectionSpec extends ContactDetailSectionFixture with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "toRequiredSection" should {
    "call the correct url" in {
      val normalUrl = ContactDetailSection.toRequiredSection(SectionStatus.NotStarted)

      normalUrl.url mustBe "/report-pillar2-top-up-taxes/contact-details"
      normalUrl.method mustBe "GET"
    }
  }

  "name" should {
    "give correct edit message given Completed status" in {
      ContactDetailSection.name(SectionStatus.Completed) mustBe "taskList.task.contact.edit"
    }

    "give correct message given any status that is NotStarted or InProgress" in {
      ContactDetailSection.name(SectionStatus.NotStarted) mustBe "taskList.task.contact.add"
      ContactDetailSection.name(SectionStatus.InProgress) mustBe "taskList.task.contact.add"
    }

    "give correct message given CannotStart" in {
      ContactDetailSection.name(SectionStatus.CannotStart) mustBe "taskList.task.contact"
    }
  }

  "progress" should {
    forAll(progressScenarios) { (assertion: String, input: UserAnswers, result: SectionStatus) =>
      s"$assertion" in {
        ContactDetailSection.progress(input) mustBe result
      }
    }
  }

  "prerequisiteSections" should {
    "have UltimateParentDetailSection and FilingMemberDetailSection as prerequisite section" in {
      ContactDetailSection
        .prerequisiteSections(emptyUserAnswers) mustBe Set(UltimateParentDetailSection, FilingMemberDetailSection, FurtherGroupDetailSection)

    }
  }
}

protected trait ContactDetailSectionFixture extends SpecBase {

  val progressScenarios = Table(
    ("assertion", "input", "result"),
    ("return InProgress if all answers for contact detail section has not been completed", contactDetailInProgress, SectionStatus.InProgress),
    (
      "return NotStarted if ultimate parent, filing member, further group detail sections has been completed and no other answers have not been attempted",
      groupDetailCompleted,
      SectionStatus.NotStarted
    ),
    ("return Completed if all answers for further group detail section have been completed", contactDetailCompleted, SectionStatus.Completed)
  )
}
