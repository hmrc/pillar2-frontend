/*
 * Copyright 2025 HM Revenue & Customs
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
import config.FrontendAppConfig
import controllers.payments.{routes => paymentRoutes}
import controllers.routes
import models.{OPSRedirectRequest, OPSRedirectResponse}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

class OPSConnectorTest extends SpecBase with WireMockServerHandler {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.ops.port" -> server.port()
    )
    .build()

  lazy val classUnderTest: OPSConnector = app.injector.instanceOf[OPSConnector]
  lazy val opsEndpoint:    String       = app.injector.instanceOf[FrontendAppConfig].opsStartUrl

  "OPS Connector" should {
    "connect to OPS for a redirect journey" in {
      val response = OPSRedirectResponse("journeyId", "nextUrl")
      val expectedRequest = OPSRedirectRequest(
        reference = "pillar2Id",
        amountInPence = 0,
        returnUrl = s"http://localhost:10050${routes.TransactionHistoryController.onPageLoadTransactionHistory(None)}",
        backUrl = s"http://localhost:10050${paymentRoutes.MakeAPaymentDashboardController.onPageLoad.url}"
      )
      stubResponse(opsEndpoint, expectedRequest)(CREATED, Json.toJson(response).toString())
      val result = classUnderTest.getRedirectLocation("pillar2Id").futureValue
      result mustEqual response.nextUrl
    }
  }

}
