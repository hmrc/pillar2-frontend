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
import helpers.FinancialDataHelper.{Pillar2RepaymentInterestName, Pillar2UktrName}
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

  val firstApUktrUTPR: FinancialTransaction = FinancialTransaction(
    taxPeriodFrom = Some(periodFrom),
    taxPeriodTo = Some(periodTo),
    mainTransaction = Some("6500"),
    subTransaction = Some("6235"),
    outstandingAmount = Some(1000.00),
    items = Seq(FinancialItem(dueDate = Some(testDate)))
  )

  val firstApUktrIIR: FinancialTransaction = FinancialTransaction(
    taxPeriodFrom = Some(periodFrom),
    taxPeriodTo = Some(periodTo),
    mainTransaction = Some("6500"),
    subTransaction = Some("6234"),
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

<<<<<<< HEAD
  val secondApInterestDTT: FinancialTransaction = FinancialTransaction(
    taxPeriodFrom = Some(periodFrom.minusYears(1)),
    taxPeriodTo = Some(periodTo.minusYears(1)),
    mainTransaction = Some("6503"),
    subTransaction = Some("6239"),
    outstandingAmount = Some(1000.00),
    items = Seq(FinancialItem(dueDate = Some(testDate.minusYears(1))))
  )

  val secondApInterestIIR: FinancialTransaction = FinancialTransaction(
    taxPeriodFrom = Some(periodFrom.minusYears(1)),
    taxPeriodTo = Some(periodTo.minusYears(1)),
    mainTransaction = Some("6503"),
    subTransaction = Some("6236"),
    outstandingAmount = Some(1000.00),
    items = Seq(FinancialItem(dueDate = Some(testDate.minusYears(1))))
  )

  val secondApInterestUTPR: FinancialTransaction = FinancialTransaction(
    taxPeriodFrom = Some(periodFrom.minusYears(1)),
    taxPeriodTo = Some(periodTo.minusYears(1)),
    mainTransaction = Some("6503"),
    subTransaction = Some("6238"),
    outstandingAmount = Some(1000.00),
    items = Seq(FinancialItem(dueDate = Some(testDate.minusYears(1))))
  )

  val validResponse: FinancialData = FinancialData(financialTransactions =
    Seq(firstApUktrDTT, firstApUktrIIR, firstApUktrUTPR, secondApInterestDTT, secondApInterestIIR, secondApInterestUTPR)
  )

  val validResponseExpectedSummary: Seq[FinancialSummary] = Seq(
    FinancialSummary(
      AccountingPeriod(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31)),
      Seq(TransactionSummary("UK tax return", 3000.00, LocalDate.of(2024, 1, 1)))
    ),
    FinancialSummary(
      AccountingPeriod(LocalDate.of(2023, 1, 1).minusYears(1), LocalDate.of(2023, 12, 31).minusYears(1)),
      Seq(TransactionSummary("Late Payment Interest", 3000.00, LocalDate.of(2024, 1, 1).minusYears(1)))
=======
  lazy val fistAccountingPeriodUktrRepaymentInterest: FinancialTransaction = FinancialTransaction(
    mainTransaction = Some("6504"),
    subTransaction = Some("6237"),
    taxPeriodFrom = Some(periodFrom),
    taxPeriodTo = Some(periodTo),
    outstandingAmount = Some(1234.56),
    items = Seq(FinancialItem(dueDate = Some(testDate)))
  )

  val validResponse: FinancialData =
    FinancialData(financialTransactions = Seq(firstApUktrDTT, firstApUktrMTT, secondApUktrDTT, fistAccountingPeriodUktrRepaymentInterest))

  val validResponseExpectedSummary: Seq[FinancialSummary] = Seq(
    FinancialSummary(
      AccountingPeriod(periodFrom, periodTo),
      Seq(
        TransactionSummary(Pillar2UktrName, 2000.00, testDate),
        TransactionSummary(Pillar2RepaymentInterestName, 1234.56, testDate)
      )
    ),
    FinancialSummary(
      AccountingPeriod(periodFrom.minusYears(1), periodTo.minusYears(1)),
      Seq(TransactionSummary(Pillar2UktrName, 1000.00, testDate.minusYears(1)))
>>>>>>> 081bf916 (Refactored ETMP Reference numbers in FinancialDataHelper)
    )
  )

  val transactionMissingAp: FinancialTransaction = FinancialTransaction(
    taxPeriodFrom = None,
    taxPeriodTo = None,
    mainTransaction = Some("6500"),
    subTransaction = Some("6233"),
    outstandingAmount = Some(1000),
    items = Seq(FinancialItem(dueDate = Some(testDate)))
  )

  val transactionMissingAmount: FinancialTransaction = FinancialTransaction(
    taxPeriodFrom = Some(periodFrom),
    taxPeriodTo = Some(periodTo),
    mainTransaction = Some("6500"),
    subTransaction = Some("6233"),
    outstandingAmount = None,
    items = Seq(FinancialItem(dueDate = Some(testDate)))
  )

  val transactionInvalidMainTransaction: FinancialTransaction = FinancialTransaction(
    taxPeriodFrom = Some(periodFrom),
    taxPeriodTo = Some(periodTo),
    mainTransaction = Some("0"),
    subTransaction = Some("6233"),
    outstandingAmount = Some(1000),
    items = Seq(FinancialItem(dueDate = Some(testDate)))
  )

  val transactionInvalidSubTransaction: FinancialTransaction = FinancialTransaction(
    taxPeriodFrom = Some(periodFrom),
    taxPeriodTo = Some(periodTo),
    mainTransaction = Some("6500"),
    subTransaction = Some("0"),
    outstandingAmount = Some(1000),
    items = Seq(FinancialItem(dueDate = Some(testDate)))
  )

  val transactionNoDueDate: FinancialTransaction = FinancialTransaction(
    taxPeriodFrom = None,
    taxPeriodTo = None,
    mainTransaction = Some("6500"),
    subTransaction = Some("6230"),
    outstandingAmount = Some(1000),
    items = Seq(FinancialItem(dueDate = None))
  )

  val redundantTransaction: FinancialData = FinancialData(financialTransactions =
    Seq(
      firstApUktrDTT,
      firstApUktrDTT,
      transactionMissingAp,
      transactionMissingAmount,
      transactionInvalidMainTransaction,
      transactionInvalidSubTransaction,
      transactionNoDueDate
    )
  )

  val redundantTransactionExpectedSummary: Seq[FinancialSummary] = Seq(
    FinancialSummary(AccountingPeriod(periodFrom, periodTo), Seq(TransactionSummary(Pillar2UktrName, 2000.00, testDate)))
  )
}
