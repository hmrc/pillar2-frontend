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

package services

import base.SpecBase
import connectors.BTNConnector
import models.btn.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.Helpers.*
import services.BTNServiceSpec.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpResponse

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class BTNServiceSpec extends SpecBase {
  given pillar2Id: String = pillar2IdForValidResponse

  val application: Application = applicationBuilder()
    .overrides(
      bind[BTNConnector].toInstance(mockBTNConnector)
    )
    .build()

  "BTNService" must {
    "return the response when connector returns a success" in
      running(application) {
        val response = HttpResponse(201, Json.obj("processingDate" -> "2024-03-14T09:26:17Z").toString())
        when(mockBTNConnector.submitBTN(any())(using any[HeaderCarrier], any(), any[ExecutionContext]))
          .thenReturn(Future.successful(response))
        val service: BTNService = application.injector.instanceOf[BTNService]
        val result = service.submitBTN(btnRequestBodyDefaultAccountingPeriodDates).futureValue
        result mustBe response
      }

    "return the response when connector returns a failure" in
      running(application) {
        val response = HttpResponse(400, "Bad Request")
        when(mockBTNConnector.submitBTN(any())(using any[HeaderCarrier], any(), any[ExecutionContext]))
          .thenReturn(Future.successful(response))
        val service: BTNService = application.injector.instanceOf[BTNService]
        val result = service.submitBTN(btnRequestBodyDefaultAccountingPeriodDates).futureValue
        result mustBe response
      }
  }
}

object BTNServiceSpec {
  val btnRequestBodyDefaultAccountingPeriodDates: BTNRequest = BTNRequest(
    accountingPeriodFrom = LocalDate.now.minusYears(1),
    accountingPeriodTo = LocalDate.now
  )
  val pillar2IdForValidResponse = "XEPLR0000000000"
}
