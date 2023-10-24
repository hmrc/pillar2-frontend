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

package models

case class EnrolmentInfo(
  ctUtr:         Option[String] = None,
  crn:           Option[String] = None,
  nonUkPostcode: Option[String] = None,
  countryCode:   Option[String] = None,
  plrId:         String
) {
  def convertToEnrolmentRequest: EnrolmentRequest =
    EnrolmentRequest(identifiers = Seq(Identifier("PLRID", plrId)), verifiers = buildVerifiers)

  def buildVerifiers: Seq[Verifier] =
    buildOptionalVerifier(ctUtr, "CTUTR") ++
      buildOptionalVerifier(crn, "CRN") ++
      buildOptionalVerifier(nonUkPostcode, "CRN") ++
      buildOptionalVerifier(countryCode, "CountryCode")

  def buildOptionalVerifier(optionalInfo: Option[String], key: String): Seq[Verifier] =
    optionalInfo
      .map(info => Verifier(key, info))
      .toSeq

}
