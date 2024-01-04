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

import base.SpecBase
import models.subscription.{AmendResponse, AmendSubscriptionRequestParameters}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import java.time.LocalDate

class AmendSubscriptionConnectorSpec extends SpecBase {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.pillar2.port" -> server.port()
    )
    .build()

  lazy val connector: AmendSubscriptionConnector = app.injector.instanceOf[AmendSubscriptionConnector]

  val successfulAmendSubscriptionJsonResponse: String =
    """
    |{
    | "success": {
    |   "processingDate": "2023-09-22T00:00",
    |   "formBundleNumber": "119000004320"
    | }
    |}""".stripMargin

  val validAmendSubscriptionParameter = AmendSubscriptionRequestParameters(id = "id")

  val validAmendSubscriptionSuccessResponse =
    AmendResponse(
      processingDate = LocalDate.parse("2023-09-22").atStartOfDay(),
      formBundleNumber = "119000004320"
    )

  "AmendSubscriptionConnector" when {

    "amendSubscription must handle the response correctly for a successful amend subscription" in {
      val expectedEndpoint = "/report-pillar2-top-up-taxes/subscription/amend-subscription"
      val expectedStatus   = 200
      val successResponse = Json.obj(
        "success" -> Json.obj(
          "processingDate"   -> "2022-01-31T09:26:17Z",
          "formBundleNumber" -> "119000004320"
        )
      )
      stubResponseForPutRequest(expectedEndpoint, expectedStatus, Some(successResponse.toString()))

      val result = connector.amendSubscription(validAmendSubscriptionParameter)

      whenReady(result) { response =>
        response mustBe Some(successResponse)
      }
    }

  }

}
