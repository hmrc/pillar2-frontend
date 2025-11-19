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
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

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
  }
}
