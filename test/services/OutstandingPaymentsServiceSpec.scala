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
import connectors.FinancialDataConnector
import helpers.FinancialDataHelper.PILLAR2_UKTR
import models._
import models.subscription.AccountingPeriod
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject
import services.OutstandingPaymentsServiceSpec._

import java.time.LocalDate
import scala.concurrent.Future

class OutstandingPaymentsServiceSpec extends SpecBase {

  val service: OutstandingPaymentsService = applicationBuilder()
    .overrides(inject.bind[FinancialDataConnector].toInstance(mockFinancialDataConnector))
    .build()
    .injector
    .instanceOf[OutstandingPaymentsService]

  "retrieveData" should {
    "successfully transform financial transactions into financial summaries" in {
      when(mockFinancialDataConnector.retrieveFinancialData(any(), any(), any())(any()))
        .thenReturn(Future.successful(validResponse))

      service.retrieveData(pillar2Id, testDate, testDate).futureValue mustBe validResponseExpectedSummary
    }

    "filter out transactions with missing required fields" in {
      when(mockFinancialDataConnector.retrieveFinancialData(any(), any(), any())(any()))
        .thenReturn(Future.successful(redundantTransaction))

      service.retrieveData(pillar2Id, testDate, testDate).futureValue mustBe redundantTransactionExpectedSummary
    }
  }
}

object OutstandingPaymentsServiceSpec {
  val pillar2Id:  String    = "XMPLR0123456789"
  val testDate:   LocalDate = LocalDate.of(2024, 1, 1)
  val periodFrom: LocalDate = LocalDate.of(2023, 1, 1)
  val periodTo:   LocalDate = LocalDate.of(2023, 12, 31)

  val firstApUktrDTT: FinancialTransaction = FinancialTransaction(
    taxPeriodFrom = Some(periodFrom),
    taxPeriodTo = Some(periodTo),
    mainTransaction = Some("6500"),
    subTransaction = Some("6233"),
    outstandingAmount = Some(1000.00),
    items = Seq(FinancialItem(dueDate = Some(testDate)))
  )

  val firstApUktrMTT: FinancialTransaction = FinancialTransaction(
    taxPeriodFrom = Some(periodFrom),
    taxPeriodTo = Some(periodTo),
    mainTransaction = Some("6500"),
    subTransaction = Some("6324"),
    outstandingAmount = Some(1000.00),
    items = Seq(FinancialItem(dueDate = Some(testDate)))
  )

  val secondApUktrDTT: FinancialTransaction = FinancialTransaction(
    taxPeriodFrom = Some(periodFrom.minusYears(1)),
    taxPeriodTo = Some(periodTo.minusYears(1)),
    mainTransaction = Some("6500"),
    subTransaction = Some("6233"),
    outstandingAmount = Some(1000.00),
    items = Seq(FinancialItem(dueDate = Some(testDate.minusYears(1))))
  )

  val validResponse: FinancialData = FinancialData(financialTransactions = Seq(firstApUktrDTT, firstApUktrMTT, secondApUktrDTT))

  val validResponseExpectedSummary: Seq[FinancialSummary] = Seq(
    FinancialSummary(AccountingPeriod(periodFrom, periodTo), Seq(TransactionSummary(PILLAR2_UKTR, 2000.00, testDate))),
    FinancialSummary(
      AccountingPeriod(periodFrom.minusYears(1), periodTo.minusYears(1)),
      Seq(TransactionSummary(PILLAR2_UKTR, 1000.00, testDate.minusYears(1)))
    )
  )

  val invalidTransaction: FinancialTransaction = FinancialTransaction(
    taxPeriodFrom = None,
    taxPeriodTo = None,
    mainTransaction = Some("6500"),
    subTransaction = Some("6233"),
    outstandingAmount = None,
    items = Seq(FinancialItem(dueDate = Some(testDate)))
  )

  val redundantTransaction: FinancialData = FinancialData(financialTransactions = Seq(firstApUktrDTT, firstApUktrDTT, invalidTransaction))

  val redundantTransactionExpectedSummary: Seq[FinancialSummary] = Seq(
    FinancialSummary(AccountingPeriod(periodFrom, periodTo), Seq(TransactionSummary(PILLAR2_UKTR, 2000.00, testDate)))
  )
}
