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

package connectors

import akka.Done
import base.SpecBase
import models.EnrolmentRequest.AllocateEnrolmentParameters
import models.{EnrolmentInfo, Verifier}
import org.scalacheck.Gen
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

class TaxEnrolmentsConnectorSpec extends SpecBase {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.tax-enrolments.port" -> server.port()
    )
    .build()

  lazy val connector: TaxEnrolmentConnector = app.injector.instanceOf[TaxEnrolmentConnector]

  private val errorCodes: Gen[Int] =
    Gen.oneOf(Seq(BAD_REQUEST, FORBIDDEN, NOT_FOUND, INTERNAL_SERVER_ERROR, BAD_GATEWAY, GATEWAY_TIMEOUT, SERVICE_UNAVAILABLE))
  "TaxEnrolmentsConnector" when {
    val enrolmentInfo = EnrolmentInfo(crn = Some("crn"), ctUtr = Some("utr"), plrId = "plrId")

    "createEnrolment" should {
      "createEnrolment must return status as 204 for successful Tax Enrolment call" in {

        stubResponseForPutRequest("/tax-enrolments/service/HMRC-PILLAR2-ORG/enrolment", NO_CONTENT)
        val result = connector.enrolAndActivate(enrolmentInfo)
        result.futureValue mustBe Done
      }

      "must return status as 400 and BadRequest error" in {

        stubResponseForPutRequest("/tax-enrolments/service/HMRC-PILLAR2-ORG/enrolment", BAD_REQUEST)
        val result = connector.enrolAndActivate(enrolmentInfo).failed.futureValue
        result mustBe models.InternalIssueError
      }

      "must return status ServiceUnavailable Error" in {

        stubResponseForPutRequest(s"/tax-enrolments/service/HMRC-PILLAR2-ORG/enrolment", INTERNAL_SERVER_ERROR)
        val result = connector.enrolAndActivate(enrolmentInfo).failed.futureValue
        result mustBe models.InternalIssueError
      }
    }
    "allocate Enrolment" should {
      val allocateBody = AllocateEnrolmentParameters("id", verifiers = Seq(Verifier("nonUkPostalCode", "1231"), Verifier("countryCode", "DS")))
      "return done in case of a CREATED response from tax enrolment " in {
        stubResponse("/tax-enrolments/groups/id/enrolments/HMRC-PILLAR2-ORG~PLRID~plrId", CREATED, "")
        val result = connector.allocateEnrolment("id", "plrId", allocateBody)
        result.futureValue mustBe Done
      }
      "return a failed result in case of any response else than 201" in {
        stubResponse("/tax-enrolments/groups/id/enrolments/HMRC-PILLAR2-ORG~PLRID~plrId", errorCodes.sample.value, "")
        val result = connector.allocateEnrolment("id", "plrId", allocateBody).failed.futureValue
        result mustBe models.InternalIssueError
      }

    }

    "revoke Enrolment" should {
      "return done in case of a No content response from tax enrolment " in {
        stubDelete("/tax-enrolments/groups/id/enrolments/HMRC-PILLAR2-ORG~PLRID~plrId", NO_CONTENT, "")
        val result = connector.revokeEnrolment("id", "plrId")
        result.futureValue mustBe Done
      }
      "return a failed result in case of any response else than 204" in {
        stubDelete("/tax-enrolments/groups/id/enrolments/HMRC-PILLAR2-ORG~PLRID~plrId", errorCodes.sample.value, "")
        val result = connector.revokeEnrolment("id", "plrId").failed.futureValue
        result mustBe models.InternalIssueError
      }
    }

  }
}
