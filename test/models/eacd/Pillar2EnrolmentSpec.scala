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
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class Pillar2EnrolmentSpec extends AnyWordSpec with Matchers {

  "Pillar2Enrolment" should {

    "correctly initialize the case class with default values" in {
      val enrolment = Pillar2Enrolment()
      enrolment.serviceName mustBe "HMRC-PILLAR2-ORG"
      enrolment.identifierKey mustBe "pillar2Reference"
    }

    "correctly return default values from the companion object" in {
      Pillar2Enrolment.ServiceName mustBe "HMRC-PILLAR2-ORG"
      Pillar2Enrolment.IdentifierKey mustBe "pillar2Reference"
    }

    "allow custom values for case class instantiation" in {
      val customEnrolment = Pillar2Enrolment("Custom-Service", "Custom-Identifier")
      customEnrolment.serviceName mustBe "Custom-Service"
      customEnrolment.identifierKey mustBe "Custom-Identifier"
    }

    "return the same instance from getServiceName and getIdentifierKey" in {
      Pillar2Enrolment.getServiceName mustBe "HMRC-PILLAR2-ORG"
      Pillar2Enrolment.getIdentifierKey mustBe "pillar2Reference"
    }

    "have a default instance returned from defaultEnrolment method" in {
      val defaultInstance = Pillar2Enrolment.defaultEnrolment
      defaultInstance.serviceName mustBe "HMRC-PILLAR2-ORG"
      defaultInstance.identifierKey mustBe "pillar2Reference"
    }
  }
}
