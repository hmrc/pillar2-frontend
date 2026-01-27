/*
 * Copyright 2026 HM Revenue & Customs
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
import models.*
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import java.time.{LocalDate, LocalDateTime}

class AccountActivityConnectorSpec extends SpecBase with WireMockServerHandler {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.pillar2.port" -> server.port()
    )
    .build()

  lazy val connector: AccountActivityConnector = app.injector.instanceOf[AccountActivityConnector]

  override val fromDate: LocalDate = LocalDate.now()

  override val toDate: LocalDate = LocalDate.now.plusYears(1)

  val accountActivityUrl: String = s"/report-pillar2-top-up-taxes/account-activity?dateFrom=${fromDate.toString}&dateTo=${toDate.toString}"

  val accountActivityResponse: AccountActivityResponse =
    AccountActivityResponse(
      processingDate = LocalDateTime.of(2025, 1, 6, 10, 30, 0),
      transactionDetails = Seq(
        AccountActivityTransaction(
          transactionType = TransactionType.Payment,
          transactionDesc = "On Account Pillar 2 (Payment on Account)",
          startDate = None,
          endDate = None,
          accruedInterest = None,
          chargeRefNo = None,
          transactionDate = LocalDate.of(2025, 10, 15),
          dueDate = None,
          originalAmount = BigDecimal(10000),
          outstandingAmount = Some(BigDecimal(1000)),
          clearedAmount = Some(BigDecimal(9000)),
          standOverAmount = None,
          appealFlag = None,
          clearingDetails = Some(
            Seq(
              AccountActivityClearance(
                transactionDesc = "Pillar 2 UK Tax Return Pillar 2 DTT",
                chargeRefNo = Some("X123456789012"),
                dueDate = Some(LocalDate.of(2025, 12, 31)),
                amount = BigDecimal(2000),
                clearingDate = LocalDate.of(2025, 10, 15),
                clearingReason = Some("Allocated to Charge")
              )
            )
          )
        ),
        AccountActivityTransaction(
          transactionType = TransactionType.Credit,
          transactionDesc = "Pillar 2 UKTR RPI Pillar 2 OECD RPI",
          startDate = None,
          endDate = None,
          accruedInterest = None,
          chargeRefNo = Some("XR23456789012"),
          transactionDate = LocalDate.of(2025, 3, 15),
          dueDate = None,
          originalAmount = BigDecimal(-100),
          outstandingAmount = Some(BigDecimal(-100)),
          clearedAmount = None,
          standOverAmount = None,
          appealFlag = None,
          clearingDetails = None
        )
      )
    )

  ".retrieveAccountActivity" should {
    "return account activity response" in {
      stubGet(accountActivityUrl, expectedStatus = 200, Json.toJson(accountActivityResponse).toString(), Map("X-Pillar2-Id" -> PlrReference))

      val value = connector.retrieveAccountActivity(PlrReference, fromDate, toDate)

      value.futureValue mustBe accountActivityResponse
    }

    "return NoResultFound when there is no results found for plr reference" in {
      stubGet(accountActivityUrl, expectedStatus = 404, "", Map("X-Pillar2-Id" -> PlrReference))

      val value = connector.retrieveAccountActivity(PlrReference, fromDate, toDate)

      value.failed.futureValue mustBe NoResultFound
    }

    "return NoResultFound when ETMP returns 422 no data found (code 014)" in {
      val errorJson =
        """{"errors":{"processingDate":"2025-01-06T10:30:00Z","code":"014","text":"No data found"}}"""
      stubGet(accountActivityUrl, expectedStatus = 422, errorJson, Map("X-Pillar2-Id" -> PlrReference))

      val value = connector.retrieveAccountActivity(PlrReference, fromDate, toDate)

      value.failed.futureValue mustBe NoResultFound
    }

    "return UnexpectedResponse when ETMP returns 422 for a non-014 code" in {
      val errorJson =
        """{"errors":{"processingDate":"2025-01-06T10:30:00Z","code":"003","text":"Request could not be processed"}}"""
      stubGet(accountActivityUrl, expectedStatus = 422, errorJson, Map("X-Pillar2-Id" -> PlrReference))

      val value = connector.retrieveAccountActivity(PlrReference, fromDate, toDate)

      value.failed.futureValue mustBe UnexpectedResponse
    }

    "return UnexpectedResponse when an error is returned" in {
      stubGet(accountActivityUrl, expectedStatus = 500, "", Map("X-Pillar2-Id" -> PlrReference))

      val value = connector.retrieveAccountActivity(PlrReference, fromDate, toDate)

      value.failed.futureValue mustBe UnexpectedResponse
    }
  }
}
