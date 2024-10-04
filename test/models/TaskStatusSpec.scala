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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TaskStatusSpec extends AnyWordSpec with Matchers {

  "TaskStatus" should {

    "contain Completed status" in {
      TaskStatus.Completed mustBe a[TaskStatus]
    }

    "contain InProgress status" in {
      TaskStatus.InProgress mustBe a[TaskStatus]
    }

    "contain NotStarted status" in {
      TaskStatus.NotStarted mustBe a[TaskStatus]
    }

    "contain CannotStartYet status" in {
      TaskStatus.CannotStartYet mustBe a[TaskStatus]
    }

    "contain Default status with lowercase value" in {
      TaskStatus.Default mustBe a[TaskStatus]
      TaskStatus.Default.value mustEqual "default" // Check the value field
    }
  }

  "TaskAction" should {

    "contain Edit action" in {
      TaskAction.Edit mustBe a[TaskAction]
    }

    "contain Add action" in {
      TaskAction.Add mustBe a[TaskAction]
    }

    "contain Default action" in {
      TaskAction.Default mustBe a[TaskAction]
    }
  }
}
