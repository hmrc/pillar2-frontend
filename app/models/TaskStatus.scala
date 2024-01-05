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

sealed trait TaskStatus
object TaskStatus {
  case object Completed extends TaskStatus
  case object InProgress extends TaskStatus
  case object NotStarted extends TaskStatus
  case object CannotStartYet extends TaskStatus
  case object Default extends TaskStatus
}

sealed trait TaskAction
object TaskAction {
  case object Edit extends TaskAction
  case object Add extends TaskAction
  case object Default extends TaskAction
}