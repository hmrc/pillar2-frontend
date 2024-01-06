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

import controllers.TaskInfo
import play.api.i18n.Messages
import play.api.i18n.Messages
import play.twirl.api.Html

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

  def stringToTaskStatus(status: String): TaskStatus = status match {
    case "Completed"      => TaskStatus.Completed
    case "InProgress"     => TaskStatus.InProgress
    case "NotStarted"     => TaskStatus.NotStarted
    case "CannotStartYet" => TaskStatus.CannotStartYet
    case _                => TaskStatus.Default
  }

  def renderTask(taskInfo: TaskInfo, messageKeyBase: String)(implicit messages: Messages): Html = {
    val actionKey   = taskInfo.action.map(actionToString).getOrElse("default")
    val taskStatus  = statusToString(taskInfo.status)
    val linkTextKey = s"$messageKeyBase.$actionKey"
    val linkText = taskInfo.link
      .map { link =>
        s"""<a href="$link" aria-describedby="eligibility-status">${messages(linkTextKey)}</a>"""
      }
      .getOrElse(messages(s"$messageKeyBase"))
    val statusTag = s"""<span class="hmrc-status-tag">$taskStatus</span>"""
    Html(s"""<li class="app-task-list__item"><span class="app-task-list__task-name">$linkText</span>$statusTag</li>""")
  }

}
