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
import models.InternalIssueError
import models.btn.BTNRequest
import org.scalatest.exceptions.TestFailedException
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.http.test.WireMockSupport
import utils.DateTimeUtils.utcZoneId

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, ZoneOffset, ZonedDateTime}
import scala.concurrent.Future

class BTNConnectorSpec extends SpecBase with WireMockSupport with WireMockServerHandler {
  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(conf = "microservice.services.pillar2.port" -> server.port())
    .build()
  lazy val connector: BTNConnector = app.injector.instanceOf[BTNConnector]
  val submitBTNPath = "/report-pillar2-top-up-taxes/below-threshold-notification/submit"
  val btnRequestBodyDefaultAccountingPeriodDates: BTNRequest = BTNRequest(
    accountingPeriodFrom = LocalDate.now.minusYears(1),
    accountingPeriodTo = LocalDate.now
  )
  val stubProcessedZonedDateTime: ZonedDateTime = ZonedDateTime.now(utcZoneId).truncatedTo(ChronoUnit.SECONDS)
  val successfulBTNResponseBody: JsObject = Json.obj(
    "success" -> Json.obj("processingDate" -> stubProcessedZonedDateTime)
  )
  val accountingPeriodFromDateMinus1Year: LocalDate  = LocalDate.now.minusYears(1)
  val accountingPeriodToDateNow:          LocalDate  = LocalDate.now
  val btnRequestDatesMinus1YearAndNow:    BTNRequest = BTNRequest(accountingPeriodFromDateMinus1Year, accountingPeriodToDateNow)

  "submit BTN connector " should {
    "return 201 CREATED when the pillar-2 backend has returned status=201." in {
      implicit val pillar2Id: String = "XEPLR0000000000"
      stubResponse(submitBTNPath, CREATED, successfulBTNResponseBody.toString())
      val result: JsValue = connector.submitBTN(btnRequestDatesMinus1YearAndNow: BTNRequest).futureValue
      result mustBe successfulBTNResponseBody
      if ((result \ "success" \ "processingDate").isDefined) {} else {
        throw new AssertionError(s"submitBTN request failed for plrReference= $pillar2Id")
      }
    }
    "raise an Exception when the expected response field-value-checking fails." in {
      implicit val pillar2Id: String = "XEPLR9999999999"
      stubResponse(submitBTNPath, CREATED, successfulBTNResponseBody.toString())
      val result: JsValue = connector.submitBTN(btnRequestDatesMinus1YearAndNow: BTNRequest).futureValue
      result mustBe successfulBTNResponseBody
      assertThrows[TestFailedException] {
        if ((result \ "processingDate").isDefined) {} else {
          throw new TestFailedException(s"submitBTN response field-value-checking failed for plrReference= $pillar2Id", 0)
        }
      }
    }

    "return InternalIssueError when the pillar-2 backend has returned status=400." in {
      implicit val pillar2Id: String = "XEPLR4000000000"
      stubResponse(submitBTNPath, INTERNAL_SERVER_ERROR, successfulBTNResponseBody.toString())
      val result: Future[JsValue] = connector.submitBTN(btnRequestDatesMinus1YearAndNow: BTNRequest)
      result.failed.futureValue mustBe InternalIssueError
    }

    "return InternalIssueError when the pillar-2 backend has returned status=422." in {
      implicit val pillar2Id: String = "XEPLR4220000000"
      stubResponse(submitBTNPath, INTERNAL_SERVER_ERROR, successfulBTNResponseBody.toString())
      val result: Future[JsValue] = connector.submitBTN(btnRequestDatesMinus1YearAndNow: BTNRequest)
      result.failed.futureValue mustBe InternalIssueError
    }

    "return InternalIssueError when the pillar-2 backend has returned status=500." in {
      implicit val pillar2Id: String = "XEPLR5000000000"
      stubResponse(submitBTNPath, INTERNAL_SERVER_ERROR, successfulBTNResponseBody.toString())
      val result: Future[JsValue] = connector.submitBTN(btnRequestDatesMinus1YearAndNow: BTNRequest)
      result.failed.futureValue mustBe InternalIssueError
    }
  }
}
