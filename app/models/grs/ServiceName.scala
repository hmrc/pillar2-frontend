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

package models.grs

import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.json.{Json, OFormat}

final case class ServiceName(en: OptServiceName, cy: OptServiceName)

object ServiceName {
  def apply(journeyType: JourneyType)(implicit messagesApi: MessagesApi): ServiceName =
    journeyType match {
      case JourneyType.Rfm =>
        ServiceName(
          OptServiceName(optServiceName = messagesApi("rfm.service.name")(Lang("en"))),
          OptServiceName(optServiceName = messagesApi("rfm.service.name")(Lang("cy")))
        )
      case _ =>
        ServiceName(
          OptServiceName(optServiceName = messagesApi("service.name")(Lang("en"))),
          OptServiceName(optServiceName = messagesApi("service.name")(Lang("cy")))
        )
    }

  implicit val format: OFormat[ServiceName] =
    Json.format[ServiceName]
}

final case class OptServiceName(optServiceName: String)

object OptServiceName {
  implicit val format: OFormat[OptServiceName] =
    Json.format[OptServiceName]
}
