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
import cats.syntax.either.*
import connectors.BTNConnector
import models.InternalIssueError
import models.btn.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Application
import play.api.inject.bind
import play.api.test.Helpers.*
import services.BTNServiceSpec.*
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{LocalDate, ZonedDateTime}
import scala.concurrent.{ExecutionContext, Future}

class BTNServiceSpec extends SpecBase {
  implicit val pillar2Id: String = pillar2IdForValidResponse

  val application: Application = applicationBuilder()
    .overrides(
      bind[BTNConnector].toInstance(mockBTNConnector)
    )
    .build()

  "BTNService" must {
    "return a BtnSuccess when connector returns a success" in
      running(application) {
        when(mockBTNConnector.submitBTN(any())(any[HeaderCarrier], any(), any[ExecutionContext]))
          .thenReturn(Future.successful(successResponse))
        val service: BTNService = application.injector.instanceOf[BTNService]
        val result = service.submitBTN(btnRequestBodyDefaultAccountingPeriodDates).futureValue
        result mustBe successResponse
      }

    "return a BtnError when connector returns a modelled failure" in
      running(application) {
        when(mockBTNConnector.submitBTN(any())(any[HeaderCarrier], any(), any[ExecutionContext]))
          .thenReturn(Future.successful(errorResponse))
        val service: BTNService = application.injector.instanceOf[BTNService]
        val result = service.submitBTN(btnRequestBodyDefaultAccountingPeriodDates).futureValue
        result mustBe errorResponse
      }

    "return InternalIssueError when BTN connector returns InternalIssueError" in
      running(application) {
        when(mockBTNConnector.submitBTN(any())(any[HeaderCarrier], any(), any[ExecutionContext]))
          .thenReturn(Future.failed(InternalIssueError))
        val service: BTNService = application.injector.instanceOf[BTNService]
        val failure = service.submitBTN(btnRequestBodyDefaultAccountingPeriodDates).failed.futureValue
        failure mustBe InternalIssueError
      }

    "throw an Exception when BTN connector throws a RuntimeException" in
      running(application) {
        when(mockBTNConnector.submitBTN(any())(any[HeaderCarrier], any(), any[ExecutionContext]))
          .thenReturn(Future.failed(new RuntimeException("runtime error")))
        val service: BTNService = application.injector.instanceOf[BTNService]
        val failure = service.submitBTN(btnRequestBodyDefaultAccountingPeriodDates).failed.futureValue
        failure mustBe InternalIssueError
      }

    "handle exceptions other than InternalIssueError" in
      running(application) {
        when(mockBTNConnector.submitBTN(any())(any[HeaderCarrier], any(), any[ExecutionContext]))
          .thenReturn(Future.failed(new IllegalArgumentException("Test exception")))
        val service: BTNService = application.injector.instanceOf[BTNService]
        val failure = service.submitBTN(btnRequestBodyDefaultAccountingPeriodDates).failed.futureValue
        failure mustBe InternalIssueError
      }
    "handle any other unexpected error" in
      running(application) {
        when(mockBTNConnector.submitBTN(any())(any[HeaderCarrier], any(), any[ExecutionContext]))
          .thenReturn(Future.failed(new Error("Unexpected error")))
        val service: BTNService = application.injector.instanceOf[BTNService]
        val failure = service.submitBTN(btnRequestBodyDefaultAccountingPeriodDates).failed.futureValue
        failure mustBe InternalIssueError
      }
  }
}

object BTNServiceSpec {
  val btnRequestBodyDefaultAccountingPeriodDates: BTNRequest = BTNRequest(
    accountingPeriodFrom = LocalDate.now.minusYears(1),
    accountingPeriodTo = LocalDate.now
  )
  val pillar2IdForValidResponse = "XEPLR0000000000"
  val testZonedDateTime: String        = "2025-01-10T16:54:26Z"
  val zonedTestDateTime: ZonedDateTime = ZonedDateTime.parse(testZonedDateTime)

  val btnSuccess: BtnSuccess = BtnSuccess(zonedTestDateTime)
  val btnError:   BtnError   = BtnError("400", "Invalid Message")

  val successResponse: BtnResponse = BtnResponse(btnSuccess.asRight, CREATED)
  val errorResponse:   BtnResponse = BtnResponse(btnError.asLeft, BAD_REQUEST)
}
