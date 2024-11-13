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

import base.{SpecBase, WireMockServerHandler}
import models.UnexpectedResponse
import org.apache.pekko.Done
import org.scalacheck.Gen
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

class RepaymentConnectorSpec extends SpecBase with WireMockServerHandler {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.pillar2.port" -> server.port()
    )
    .build()

  lazy val connector: RepaymentConnector = app.injector.instanceOf[RepaymentConnector]

  private val errorCodes: Gen[Int] =
    Gen.oneOf(
      Seq(
        BAD_REQUEST,
        FORBIDDEN,
        NOT_FOUND,
        INTERNAL_SERVER_ERROR,
        BAD_GATEWAY,
        GATEWAY_TIMEOUT,
        SERVICE_UNAVAILABLE,
        OK,
        ACCEPTED,
        NON_AUTHORITATIVE_INFORMATION,
        NO_CONTENT,
        RESET_CONTENT
      )
    )
  "RepaymentConnector" when {

    "Create repayment must return Done for successful submission to ETMP" in {
      stubResponse("/report-pillar2-top-up-taxes/repayment", CREATED, "")
      val result = connector.repayment(validRepaymentPayloadUkBank)
      result.futureValue mustBe Done
    }

    "must return a failed result for any non 201 response received from ETMP" in {
      stubResponse("/report-pillar2-top-up-taxes/repayment", errorCodes.sample.value, "")
      val result = connector.repayment(validRepaymentPayloadUkBank)
      result.failed.futureValue mustBe UnexpectedResponse
    }

  }

}
