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

import models.subscription.UpeDetails._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

import java.time.LocalDate

final case class ReadSubscriptionResponse(upeDetails: UpeDetails, accountStatus: Option[AccountStatus])

final case class UpeDetails(organisationName: String, registrationDate: LocalDate)

object ReadSubscriptionResponse {

  implicit val reads: Reads[ReadSubscriptionResponse] = (
    (JsPath \ "success" \ "upeDetails").read[UpeDetails] and
      (JsPath \ "success" \ "accountStatus").readNullable[AccountStatus]
  )(ReadSubscriptionResponse.apply _)

  implicit val writes: OWrites[ReadSubscriptionResponse] = Json.writes[ReadSubscriptionResponse]
}

object UpeDetails {
  implicit val format: OFormat[UpeDetails] = Json.format[UpeDetails]
}
