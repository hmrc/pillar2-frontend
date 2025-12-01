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

import play.api.libs.json.{Json, OFormat, OWrites}

case class Identifier(key: String, value: String)

object Identifier {
  given format: OFormat[Identifier] = Json.format[Identifier]

  given writes: OWrites[Identifier] = OWrites[Identifier] { identifier =>
    Json.obj(
      "key"   -> identifier.key,
      "value" -> identifier.value
    )
  }
}

case class Verifier(key: String, value: String)

object Verifier {
  given format: OFormat[Verifier] = Json.format[Verifier]

  given writes: OWrites[Verifier] = OWrites[Verifier] { verifier =>
    Json.obj(
      "key"   -> verifier.key,
      "value" -> verifier.value
    )
  }
}

case class EnrolmentRequest(identifiers: Seq[Identifier], verifiers: Seq[Verifier])

object EnrolmentRequest {
  given format: OFormat[EnrolmentRequest] = Json.format[EnrolmentRequest]

  given writes: OWrites[EnrolmentRequest] = OWrites[EnrolmentRequest] { enrolmentRequest =>
    Json.obj(
      "identifiers" -> enrolmentRequest.identifiers,
      "verifiers"   -> enrolmentRequest.verifiers
    )
  }

  case class AllocateEnrolmentParameters(
    userId:       String,
    friendlyName: String = "Allocate pillar 2 enrolment to new filing member",
    `type`:       String = "principal",
    verifiers:    Seq[Verifier]
  )

  object AllocateEnrolmentParameters {
    given format: OFormat[AllocateEnrolmentParameters] = Json.format[AllocateEnrolmentParameters]
  }

  case class KnownFacts(key: String, value: String)
  object KnownFacts {
    given format: OFormat[KnownFacts] = Json.format[KnownFacts]
  }

  case class KnownFactsParameters(service: String = "HMRC-PILLAR2-ORG", knownFacts: Seq[KnownFacts])
  object KnownFactsParameters {
    given format: OFormat[KnownFactsParameters] = Json.format[KnownFactsParameters]
  }

  case class KnownFactsResponse(service: String = "HMRC-PILLAR2-ORG", enrolments: Seq[EnrolmentRequest])
  object KnownFactsResponse {
    given format: OFormat[KnownFactsResponse] = Json.format[KnownFactsResponse]
  }
}
