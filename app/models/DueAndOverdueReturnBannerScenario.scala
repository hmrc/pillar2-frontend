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

import enumeratum.{Enum, EnumEntry}

sealed trait DueAndOverdueReturnBannerScenario extends EnumEntry

object DueAndOverdueReturnBannerScenario extends Enum[DueAndOverdueReturnBannerScenario] {

  case object Due extends DueAndOverdueReturnBannerScenario
  case object Overdue extends DueAndOverdueReturnBannerScenario
  case object Incomplete extends DueAndOverdueReturnBannerScenario
  case object Received extends DueAndOverdueReturnBannerScenario

  implicit val ordering: Ordering[DueAndOverdueReturnBannerScenario] =
    Ordering.by {
      case Overdue    => 4
      case Incomplete => 3
      case Due        => 2
      case Received   => 1
    }

  val values = findValues
}
