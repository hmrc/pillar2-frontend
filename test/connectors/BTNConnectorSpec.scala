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
import models.btn.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.EitherValues
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.*
import uk.gov.hmrc.http.test.WireMockSupport

import java.time.LocalDate

class BTNConnectorSpec extends SpecBase with WireMockSupport with WireMockServerHandler with EitherValues with ScalaCheckDrivenPropertyChecks {
  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(conf = "microservice.services.pillar2.port" -> server.port())
    .build()
  lazy val connector: BTNConnector = app.injector.instanceOf[BTNConnector]
  val submitBTNPath = "/report-pillar2-top-up-taxes/below-threshold-notification/submit"
  val btnRequestBodyDefaultAccountingPeriodDates: BTNRequest = BTNRequest(
    accountingPeriodFrom = LocalDate.now.minusYears(1),
    accountingPeriodTo = LocalDate.now
  )
  val rawProcessedDateTime:               String     = "2022-01-31T09:26:17Z"
  val successfulBTNResponseBody:          JsObject   = Json.obj("processingDate" -> rawProcessedDateTime)
  val accountingPeriodFromDateMinus1Year: LocalDate  = LocalDate.now.minusYears(1)
  val accountingPeriodToDateNow:          LocalDate  = LocalDate.now
  val btnRequestDatesMinus1YearAndNow:    BTNRequest = BTNRequest(accountingPeriodFromDateMinus1Year, accountingPeriodToDateNow)

  "submit BTN connector" should {
    "return the response when the pillar-2 backend has returned status 201." in {
      given pillar2Id: String = "XEPLR0000000000"
      stubResponse(submitBTNPath, CREATED, successfulBTNResponseBody.toString())
      val result = connector.submitBTN(btnRequestDatesMinus1YearAndNow).futureValue
      result.status mustBe CREATED
      result.body mustBe successfulBTNResponseBody.toString()
    }

    "return the response when dealing with a 400 or 500 error" in forAll(
      Gen.oneOf(BAD_REQUEST, INTERNAL_SERVER_ERROR),
      arbitrary[String],
      arbitrary[String]
    ) { (httpStatusCode, errorCode, errorMessage) =>
      val jsonResponse = Json.obj(
        "code"    -> errorCode,
        "message" -> errorMessage
      )
      given pillar2Id: String = "XEPLR0000000000"
      stubResponse(submitBTNPath, httpStatusCode, jsonResponse.toString())
      val result = connector.submitBTN(btnRequestDatesMinus1YearAndNow).futureValue
      result.status mustBe httpStatusCode
      result.body mustBe jsonResponse.toString()
    }

    "return the response when dealing with a 422 error" in forAll { (errorCode: String, errorMessage: String) =>
      val jsonResponse = Json.obj(
        "processingDate" -> rawProcessedDateTime,
        "code"           -> errorCode,
        "message"        -> errorMessage
      )
      given pillar2Id: String = "XEPLR0000000000"
      stubResponse(submitBTNPath, UNPROCESSABLE_ENTITY, jsonResponse.toString())
      val result = connector.submitBTN(btnRequestDatesMinus1YearAndNow).futureValue
      result.status mustBe UNPROCESSABLE_ENTITY
      result.body mustBe jsonResponse.toString()
    }

    "return the response even when the pillar-2 backend returns any unsupported status." in forAll(
      Gen.posNum[Int].retryUntil(code => !Seq(CREATED, BAD_REQUEST, UNPROCESSABLE_ENTITY, INTERNAL_SERVER_ERROR).contains(code))
    ) { (httpStatus: Int) =>
      given pillar2Id: String = "XEPLR4000000000"
      stubResponse(submitBTNPath, httpStatus, successfulBTNResponseBody.toString())
      val result = connector.submitBTN(btnRequestDatesMinus1YearAndNow).futureValue
      result.status mustBe httpStatus
    }
  }
}
