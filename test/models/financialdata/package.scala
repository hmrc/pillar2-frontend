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

package models

import cats.syntax.option._
import models.financialdata.FinancialTransaction.{OutstandingCharge, Payment}
import org.scalacheck.{Arbitrary, Gen}

import java.time.LocalDate

package object financialdata {

  private val anyDate: Gen[LocalDate] =
    Gen.choose(LocalDate.of(2023, 1, 1).toEpochDay, LocalDate.now.plusYears(3).toEpochDay).map(LocalDate.ofEpochDay) // scalastyle:ignore magic.number
  private val anyTaxPeriod: Gen[TaxPeriod] = anyDate.map(date => TaxPeriod(date.minusYears(1), date))
  private val anyMainTransactionChargeRef: Gen[EtmpMainTransactionRef.ChargeRef] = Gen.oneOf(EtmpMainTransactionRef.values.collect {
    case chargeRef: EtmpMainTransactionRef.ChargeRef => chargeRef
  })
  private val anySubTransactionRef: Gen[EtmpSubtransactionRef] = Gen.oneOf(EtmpSubtransactionRef.values)
  val anyOutstandingFinancialItem: Gen[FinancialItem] = for {
    dueDate      <- anyDate
    clearingDate <- Gen.option(Gen.const(dueDate.plusDays(7))) // scalastyle:ignore magic.number
  } yield FinancialItem(dueDate.some, clearingDate)

  private val anyOutstandingChargeFields: Gen[(TaxPeriod, EtmpSubtransactionRef, BigDecimal, OutstandingCharge.FinancialItems)] = for {
    taxPeriod         <- anyTaxPeriod
    subTxRef          <- anySubTransactionRef
    outstandingAmount <- Gen.choose(0.01, 100000000.00).map(BigDecimal.valueOf)
    items             <- Gen.listOfN(3, anyOutstandingFinancialItem)
  } yield (taxPeriod, subTxRef, outstandingAmount, OutstandingCharge.FinancialItems(taxPeriod.to, items))

  private val anyPaymentTransaction: Gen[Payment] = Gen.listOfN(3, anyOutstandingFinancialItem).map { items =>
    Payment(Payment.FinancialItems(items))
  }

  val outstandingTransaction: Gen[OutstandingCharge] = for {
    mainTxRef <- anyMainTransactionChargeRef
    fields    <- anyOutstandingChargeFields
  } yield (OutstandingCharge.apply _)(mainTxRef).tupled(fields)

  implicit val anyTransactions: Arbitrary[Seq[FinancialTransaction]] = Arbitrary {
    Gen.choose(1, 5).flatMap(Gen.listOfN(_, Gen.oneOf(anyPaymentTransaction, outstandingTransaction))) // scalastyle:ignore magic.number
  }

}
