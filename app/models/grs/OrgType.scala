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

package models.grs

import config.FrontendAppConfig
import models.Enumerable
import play.api.i18n.Messages
import play.api.libs.json._

import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait OrgType

object OrgType {
  case object UkLimitedCompany extends OrgType
  case object LimitedLiabilityPartnership extends OrgType
  case object Other extends OrgType

  val values: Seq[OrgType] = Seq(
    UkLimitedCompany,
    LimitedLiabilityPartnership,
    Other
  )

  implicit val enumerable: Enumerable[OrgType] = Enumerable(values.map(v => (v.toString, v)): _*)

  implicit val format: Format[OrgType] = new Format[OrgType] {
    override def reads(json: JsValue): JsResult[OrgType] = json.validate[String] match {
      case JsSuccess(value, _) =>
        value match {
          case "UkLimitedCompany"            => JsSuccess(UkLimitedCompany)
          case "LimitedLiabilityPartnership" => JsSuccess(LimitedLiabilityPartnership)
          case "Other"                       => JsSuccess(Other)
          case s                             => JsError(s"$s is not a valid OrgType")
        }
      case e: JsError => e
    }

    override def writes(o: OrgType): JsValue = JsString(o.toString)
  }
}
