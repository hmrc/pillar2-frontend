/*
 * Copyright 2025 HM Revenue & Customs
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

sealed trait DynamicNotificationAreaState

object DynamicNotificationAreaState {

  case object NoNotification extends DynamicNotificationAreaState
  case class AccruingInterestNotification(amountOwed: BigDecimal) extends DynamicNotificationAreaState
  sealed trait ReturnExpectedNotification extends DynamicNotificationAreaState with EnumEntry

  object ReturnExpectedNotification extends Enum[ReturnExpectedNotification] {
    case object Due extends ReturnExpectedNotification
    case object Overdue extends ReturnExpectedNotification
    case object Incomplete extends ReturnExpectedNotification

    val values = findValues
  }
}
