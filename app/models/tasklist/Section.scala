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

import models.UserAnswers
import models.tasklist.SectionStatus.{CannotStart, Completed, NotStarted}
import play.api.mvc.Call

trait Section {

  def toRequiredSection(ss: SectionStatus): Call

  def name(ss: SectionStatus): String

  def progress(answers: UserAnswers): SectionStatus

  def prerequisiteSections(answers: UserAnswers): Set[Section]

  def status(answers: UserAnswers): SectionStatus =
    progress(answers) match {
      case NotStarted if anyIncompletePrerequisites(answers) => CannotStart
      case status                                            => status
    }
  def asViewModel(answers: UserAnswers): SectionViewModel = {
    val sectionStatus = status(answers)

    val url = sectionStatus match {
      case CannotStart => None
      case ss @ _      => Some(toRequiredSection(ss))
    }

    SectionViewModel(name(sectionStatus), url, sectionStatus)
  }

  private[tasklist] def anyIncompletePrerequisites(answers: UserAnswers): Boolean =
    prerequisiteSections(answers).exists(_.progress(answers) != Completed)

}
