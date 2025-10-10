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

package models.subscription

import enumeratum.{Enum, EnumEntry}
import play.api.libs.json.{JsPath, OFormat}

sealed trait AccountStatus extends EnumEntry

object AccountStatus extends Enum[AccountStatus] {
  case object ActiveAccount extends AccountStatus
  case object InactiveAccount extends AccountStatus

  val values = findValues

  implicit val format: OFormat[AccountStatus] = (JsPath \ "inactive")
    .format[Boolean]
    .bimap(
      {
        case true  => InactiveAccount
        case false => ActiveAccount
      },
      {
        case InactiveAccount => true
        case ActiveAccount   => false
      }
    )
}
