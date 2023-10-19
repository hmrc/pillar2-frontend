/*
 * Copyright 2023 HM Revenue & Customs
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

package models.fm

import models.grs.EntityType
import models.registration.GrsResponse
import play.api.libs.json.{Json, OFormat}
import utils.RowStatus

case class FilingMember(
  nfmConfirmation:     Boolean,
  isNfmRegisteredInUK: Option[Boolean] = None,
  orgType:             Option[EntityType] = None,
  isNFMnStatus:        RowStatus,
  withIdRegData:       Option[GrsResponse] = None,
  withoutIdRegData:    Option[WithoutIdNfmData] = None,
  safeId:              Option[String] = None
)

object FilingMember {
  implicit val format: OFormat[FilingMember] = Json.format[FilingMember]
}
