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

import models.registration.*

case class EnrolmentInfo(
  ctUtr:         Option[String] = None,
  crn:           Option[String] = None,
  nonUkPostcode: Option[String] = None,
  countryCode:   Option[String] = None,
  plrId:         String
) {
  def convertToEnrolmentRequest: EnrolmentRequest =
    EnrolmentRequest(identifiers = Seq(Identifier(Pillar2Identifier.toString, plrId)), verifiers = buildVerifiers)

  def buildVerifiers: Seq[Verifier] =
    buildOptionalVerifier(ctUtr, UTR.toString) ++
      buildOptionalVerifier(crn, CRN.toString) ++
      buildOptionalVerifier(nonUkPostcode, NonUkPostCode.toString) ++
      buildOptionalVerifier(countryCode, CountryCode.toString)

  def buildOptionalVerifier(optionalInfo: Option[String], key: String): Seq[Verifier] =
    optionalInfo
      .map(info => Verifier(key, info))
      .toSeq

}
