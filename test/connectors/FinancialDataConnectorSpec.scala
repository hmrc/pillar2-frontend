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
import models.*
import models.financialdata.FinancialDataResponse
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import java.time.LocalDate

class FinancialDataConnectorSpec extends SpecBase with WireMockServerHandler {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.pillar2.port" -> server.port()
    )
    .build()

  lazy val connector: FinancialDataConnector = app.injector.instanceOf[FinancialDataConnector]

  val dateFrom: LocalDate = LocalDate.now()
  val dateTo:   LocalDate = LocalDate.now.plusYears(1)

  val TransactionHistoryUrl: String = s"/report-pillar2-top-up-taxes/transaction-history/$PlrReference/${dateFrom.toString}/${dateTo.toString}"
  val financialDataUrl:      String = s"/report-pillar2-top-up-taxes/financial-data/$PlrReference/${dateFrom.toString}/${dateTo.toString}"

  val transactionHistoryResponse: TransactionHistory =
    TransactionHistory(
      PlrReference,
      List(
        Transaction(LocalDate.now.plusDays(1), "Payment", 100.0, 0.00),
        Transaction(LocalDate.now.plusDays(2), "Repayment", 0.0, 100.0)
      )
    )

  val financialDataResponse: FinancialDataResponse = FinancialDataResponse(
    financialTransactions = Seq(
      FinancialDataResponse.FinancialTransaction(
        mainTransaction = Some("4741"),
        subTransaction = Some("1234"),
        taxPeriodFrom = Some(LocalDate.now.minusMonths(6)),
        taxPeriodTo = Some(LocalDate.now),
        outstandingAmount = Some(BigDecimal(1000.00)),
        items =
          Seq(FinancialDataResponse.FinancialItem(dueDate = Some(LocalDate.now.plusMonths(1)), clearingDate = Some(LocalDate.now.plusMonths(1))))
      ),
      FinancialDataResponse.FinancialTransaction(
        mainTransaction = Some("4742"),
        subTransaction = Some("5678"),
        taxPeriodFrom = Some(LocalDate.now),
        taxPeriodTo = Some(LocalDate.now.plusMonths(6)),
        outstandingAmount = Some(BigDecimal(2000.00)),
        items =
          Seq(FinancialDataResponse.FinancialItem(dueDate = Some(LocalDate.now.plusMonths(2)), clearingDate = Some(LocalDate.now.plusMonths(2))))
      )
    )
  )

  ".retrieveTransactionHistory" should {
    "return a transaction history" in {
      stubGet(TransactionHistoryUrl, expectedStatus = 200, Json.toJson(transactionHistoryResponse).toString())

      val value = connector.retrieveTransactionHistory(PlrReference, dateFrom, dateTo)

      value.futureValue mustBe transactionHistoryResponse
    }

    "return a no result error when there is no results found for plr reference" in {
      stubGet(TransactionHistoryUrl, expectedStatus = 404, "")

      val value = connector.retrieveTransactionHistory(PlrReference, dateFrom, dateTo)

      value.failed.futureValue mustBe NoResultFound
    }

    "return a unexpected response when an error is returned" in {
      stubGet(TransactionHistoryUrl, expectedStatus = 500, "")

      val value = connector.retrieveTransactionHistory(PlrReference, dateFrom, dateTo)

      value.failed.futureValue mustBe UnexpectedResponse
    }
  }

  ".retrieveFinancialData" should {
    "return financial data" in {
      stubGet(financialDataUrl, expectedStatus = 200, Json.toJson(financialDataResponse).toString())

      val value = connector.retrieveFinancialData(PlrReference, dateFrom, dateTo)

      value.futureValue mustBe financialDataResponse
    }

    "return an empty list of financial transactions when no results are found for plr reference" in {
      stubGet(financialDataUrl, expectedStatus = 404, "")

      val value = connector.retrieveFinancialData(PlrReference, dateFrom, dateTo)

      value.futureValue mustBe FinancialDataResponse(Seq.empty)
    }

    "return RetryableGatewayError when the backend returns 500" in {
      stubGet(financialDataUrl, expectedStatus = 500, "")

      val value = connector.retrieveFinancialData(PlrReference, dateFrom, dateTo)

      value.failed.futureValue mustBe RetryableGatewayError
    }

    "return RetryableGatewayError when the backend returns 502" in {
      stubGet(financialDataUrl, expectedStatus = 502, "")

      val value = connector.retrieveFinancialData(PlrReference, dateFrom, dateTo)

      value.failed.futureValue mustBe RetryableGatewayError
    }

    "return UnexpectedResponse when the backend returns other error status" in {
      stubGet(financialDataUrl, expectedStatus = 503, "")

      val value = connector.retrieveFinancialData(PlrReference, dateFrom, dateTo)

      value.failed.futureValue mustBe UnexpectedResponse
    }
  }
}
