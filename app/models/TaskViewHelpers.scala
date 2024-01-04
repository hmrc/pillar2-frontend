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

package models

import play.api.i18n.Messages

object TaskViewHelpers {
  def statusToString(status: TaskStatus)(implicit messages: Messages): String = status match {
    case TaskStatus.Completed      => messages("task.status.completed")
    case TaskStatus.InProgress     => messages("task.status.inProgress")
    case TaskStatus.NotStarted     => messages("task.status.notStarted")
    case TaskStatus.CannotStartYet => messages("task.status.cannotStartYet")
    case TaskStatus.Default        => messages("task.status.default")
  }

  def actionToString(action: TaskAction): String = action match {
    case TaskAction.Edit    => "edit"
    case TaskAction.Add     => "add"
    case TaskAction.Default => "default"
  }
}
