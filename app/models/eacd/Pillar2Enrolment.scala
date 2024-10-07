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

package models.eacd

// Case class for Pillar2Enrolment
case class Pillar2Enrolment(serviceName: String = "HMRC-PILLAR2-ORG", identifierKey: String = "pillar2Reference")

// Companion object to preserve the same static behavior
object Pillar2Enrolment {
  val ServiceName:   String = "HMRC-PILLAR2-ORG"
  val IdentifierKey: String = "pillar2Reference"

  // Function to retrieve default values (if needed in future)
  def defaultEnrolment: Pillar2Enrolment = Pillar2Enrolment()

  // Providing backward compatibility for the static-like calls
  def getServiceName:   String = ServiceName
  def getIdentifierKey: String = IdentifierKey
}
