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
import controllers.subscription.routes
import models.tasklist.SectionStatus.{CannotStart, Completed}
import models.{NormalMode, UserAnswers}
import play.api.mvc.Call
import utils.RowStatus

object FurtherGroupDetailSection extends Section {

  override def toRequiredSection(ss: SectionStatus): Call = routes.MneOrDomesticController.onPageLoad(NormalMode)

  override def name(ss: SectionStatus): String = ss match {
    case Completed   => "taskList.task.business.sub.edit"
    case CannotStart => "taskList.task.business.more"
    case _           => "taskList.task.business.sub.add"
  }

  override def progress(answers: UserAnswers): SectionStatus =
    answers.groupDetailStatus match {
      case RowStatus.Completed  => SectionStatus.Completed
      case RowStatus.NotStarted => SectionStatus.NotStarted
      case RowStatus.InProgress => SectionStatus.InProgress
      case _                    => SectionStatus.CannotStart
    }

  override def prerequisiteSections(answers: UserAnswers): Set[Section] = Set(FilingMemberDetailSection, UltimateParentDetailSection)
}
