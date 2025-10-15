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
import connectors.ObligationsAndSubmissionsConnector
import models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._

import java.time.ZonedDateTime

class ObligationAndSubmissionsConnectorSpec extends SpecBase with WireMockServerHandler {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(conf = "microservice.services.pillar2.port" -> server.port())
    .build()

  val url:       String = s"/report-pillar2-top-up-taxes/obligations-and-submissions/$fromDate/$toDate"
  val pillar2Id: String = PlrReference

  lazy val connector: ObligationsAndSubmissionsConnector = app.injector.instanceOf[ObligationsAndSubmissionsConnector]

  "getData" should {
    "return obligations and submissions when the backend returns 200 OK with data" in {
      stubGet(
        url,
        OK,
        obligationsAndSubmissionsSuccessResponseJson.toString(),
        Map("X-Pillar2-Id" -> PlrReference)
      )

      val result = connector.getData(pillar2Id, fromDate, toDate).futureValue
      result mustBe obligationsAndSubmissionsSuccessResponse().success
    }

    "fail when the backend returns a non-200 status" in {
      stubGet(
        url,
        INTERNAL_SERVER_ERROR,
        headers = Map("X-Pillar2-Id" -> PlrReference)
      )

      whenReady(connector.getData(pillar2Id, fromDate, toDate).failed)(ex => ex mustBe an[Exception])
    }

    "fail when the response cannot be parsed" in {
      stubGet(
        url,
        OK,
        "invalid json",
        Map("X-Pillar2-Id" -> PlrReference)
      )

      whenReady(connector.getData(pillar2Id, fromDate, toDate).failed)(ex => ex mustBe an[Exception])
    }

    "return empty response when feature flag is enabled and backend returns 500 error" in {
      val appWithFeatureFlag: Application = new GuiceApplicationBuilder()
        .configure(
          conf = "microservice.services.pillar2.port" -> server.port(),
          "features.handleObligationsAndSubmissions500Errors" -> true
        )
        .build()

      val connectorWithFeatureFlag: ObligationsAndSubmissionsConnector = appWithFeatureFlag.injector.instanceOf[ObligationsAndSubmissionsConnector]

      stubGet(
        url,
        INTERNAL_SERVER_ERROR,
        headers = Map("X-Pillar2-Id" -> PlrReference)
      )

      val result = connectorWithFeatureFlag.getData(pillar2Id, fromDate, toDate).futureValue

      result.processingDate mustBe a[ZonedDateTime]
      result.accountingPeriodDetails mustBe Seq.empty
    }

    "fail when feature flag is disabled and backend returns 500 error" in {
      val appWithFeatureFlagDisabled: Application = new GuiceApplicationBuilder()
        .configure(
          conf = "microservice.services.pillar2.port" -> server.port(),
          "features.handleObligationsAndSubmissions500Errors" -> false
        )
        .build()

      val connectorWithFeatureFlagDisabled: ObligationsAndSubmissionsConnector =
        appWithFeatureFlagDisabled.injector.instanceOf[ObligationsAndSubmissionsConnector]

      stubGet(
        url,
        INTERNAL_SERVER_ERROR,
        headers = Map("X-Pillar2-Id" -> PlrReference)
      )

      whenReady(connectorWithFeatureFlagDisabled.getData(pillar2Id, fromDate, toDate).failed)(ex => ex mustBe an[Exception])
    }

    "fail when feature flag is enabled but backend returns other error status" in {
      val appWithFeatureFlag: Application = new GuiceApplicationBuilder()
        .configure(
          conf = "microservice.services.pillar2.port" -> server.port(),
          "features.handleObligationsAndSubmissions500Errors" -> true
        )
        .build()

      val connectorWithFeatureFlag: ObligationsAndSubmissionsConnector = appWithFeatureFlag.injector.instanceOf[ObligationsAndSubmissionsConnector]

      stubGet(
        url,
        BAD_REQUEST,
        headers = Map("X-Pillar2-Id" -> PlrReference)
      )

      whenReady(connectorWithFeatureFlag.getData(pillar2Id, fromDate, toDate).failed)(ex => ex mustBe an[Exception])
    }
  }
}
