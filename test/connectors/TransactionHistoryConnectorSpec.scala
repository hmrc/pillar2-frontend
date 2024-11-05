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
import models._
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import java.time.LocalDate

class TransactionHistoryConnectorSpec extends SpecBase with WireMockServerHandler {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.pillar2.port" -> server.port()
    )
    .build()

  lazy val connector: TransactionHistoryConnector = app.injector.instanceOf[TransactionHistoryConnector]

  val dateFrom: LocalDate = LocalDate.now()
  val dateTo:   LocalDate = LocalDate.now.plusYears(1)

  val TransactionHistoryUrl: String = s"/report-pillar2-top-up-taxes/transaction-history/$PlrReference/${dateFrom.toString}/${dateTo.toString}"

  val transactionHistoryResponse: TransactionHistory =
    TransactionHistory(
      PlrReference,
      List(
        FinancialHistory(LocalDate.now.plusDays(1), "Payment", 100.0, 0.00),
        FinancialHistory(LocalDate.now.plusDays(2), "Repayment", 0.0, 100.0)
      )
    )

  "Transaction history connector" must {
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

}
