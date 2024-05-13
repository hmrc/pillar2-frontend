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
import controllers.routes
import models.UserAnswers
import models.tasklist.SectionStatus.{CannotStart, NotStarted}
import play.api.mvc.Call
import utils.RowStatus

object ReviewAndSubmitSection extends Section {
  override def toRequiredSection(ss: SectionStatus): Call = routes.CheckYourAnswersController.onPageLoad

  override def name(ss: SectionStatus): String = "taskList.task.review"

  override def progress(answers: UserAnswers): SectionStatus = {
    import answers._
    answers.finalCYAStatus(upeStatus, fmStatus, groupDetailStatus, contactsStatus) match {
      case RowStatus.NotStarted => NotStarted
      case _                    => CannotStart
    }
  }

  override def prerequisiteSections(answers: UserAnswers): Set[Section] =
    Set(UltimateParentDetailSection, FilingMemberDetailSection, FurtherGroupDetailSection, ContactDetailSection)
}
