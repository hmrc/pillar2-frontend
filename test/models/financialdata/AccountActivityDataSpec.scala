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

package models.financialdata

import base.{SpecBase, TestDateTimeUtils}
import models.{AccountActivityTransaction, TransactionType}
import org.scalatest.LoneElement
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.*

class AccountActivityDataSpec extends SpecBase with ScalaCheckPropertyChecks with LoneElement with TestDateTimeUtils {

  def createDebit(
    originalAmount:    BigDecimal = BigDecimal(100),
    outstandingAmount: Option[BigDecimal] = Some(BigDecimal(100.00)),
    dueDate:           Option[LocalDate] = Some(today.plusDays(10)),
    startDate:         Option[LocalDate] = Some(today.minusMonths(1)),
    endDate:           Option[LocalDate] = Some(today),
    isInterest:        Boolean = false
  ): AccountActivityTransaction =
    AccountActivityTransaction(
      transactionType = TransactionType.Debit,
      transactionDesc = if isInterest then "Pillar 2 UKTR Interest Pillar 2 DTT Int" else "Pillar 2 UK Tax Return Pillar 2 DTT",
      startDate = startDate,
      endDate = endDate,
      accruedInterest = if isInterest then Some(5.00) else None,
      chargeRefNo = Some("someChargeRef"),
      transactionDate = today.minusDays(20),
      dueDate = dueDate,
      originalAmount = originalAmount,
      outstandingAmount = outstandingAmount,
      clearedAmount = None,
      standOverAmount = None,
      appealFlag = None,
      clearingDetails = None
    )

  def createPayment(transactionDate: LocalDate = today.minusDays(1)): AccountActivityTransaction =
    AccountActivityTransaction(
      transactionType = TransactionType.Payment,
      transactionDesc = "Payment on Account",
      startDate = None,
      endDate = None,
      accruedInterest = None,
      chargeRefNo = None,
      transactionDate = transactionDate,
      dueDate = None,
      originalAmount = BigDecimal(100),
      outstandingAmount = None,
      clearedAmount = Some(BigDecimal(100)),
      standOverAmount = None,
      appealFlag = None,
      clearingDetails = None
    )

  "AccountActivityData" when {

    "fetching onlyOutstandingCharges" should {
      "filter transactions correctly" in {
        val outstandingDebit = createDebit(outstandingAmount = Some(BigDecimal(100)))
        val paidDebit        = createDebit(outstandingAmount = None)
        val debitWithNoDates = createDebit(startDate = None, endDate = None)
        val payment          = createPayment(today)

        val data = AccountActivityData(Seq(outstandingDebit, paidDebit, debitWithNoDates, payment))

        data.onlyOutstandingCharges.loneElement mustEqual outstandingDebit
      }
    }

    "calculateOutstandingAmount" should {
      "return the sum of all outstanding amounts from valid transactions" in {
        val firstOutstandingDebit  = createDebit()
        val secondOutstandingDebit = createDebit()
        val thirdOutstandingDebit  = createDebit(outstandingAmount = Some(BigDecimal(500)))

        val data = AccountActivityData(Seq(firstOutstandingDebit, secondOutstandingDebit, thirdOutstandingDebit))

        data.calculateOutstandingAmount mustEqual BigDecimal(700)
      }

      "return 0 when there are no outstanding charges" in {
        val data = AccountActivityData(Seq.empty)
        data.calculateOutstandingAmount mustEqual BigDecimal(0)
      }
    }

    "onlyOverdueOutstandingCharges" should {
      "return any overdue outstanding charges" in {
        val overdueOutstandingCharge       = createDebit(dueDate = Some(today.minusDays(1)))
        val outstandingButNotOverdueCharge = createDebit()

        val data = AccountActivityData(Seq(overdueOutstandingCharge, outstandingButNotOverdueCharge))

        data.onlyOverdueOutstandingCharges.loneElement mustEqual overdueOutstandingCharge
      }

      "return an empty collection when there are no overdue outstanding charges" in {
        val outstandingButNotOverdueCharge = createDebit()

        val data = AccountActivityData(Seq(outstandingButNotOverdueCharge))

        data.onlyOverdueOutstandingCharges mustEqual Seq.empty
      }
    }

    "hasRecentPayment" should {
      "be true when the passed account activity data contains a payment which cleared within the configured leeway" in {
        val firstPayment  = createPayment(transactionDate = today.minusDays(applicationConfig.maxDaysAgoToConsiderPaymentAsRecent))
        val secondPayment = createPayment(transactionDate = today.minusDays(Integer.MAX_VALUE))

        val data = AccountActivityData(Seq(firstPayment, secondPayment))

        data.hasRecentPayment mustEqual true
      }

      "be false when the passed account activity data only contains a payment which cleared beyond the configured leeway" in {
        val payment =
          createPayment(transactionDate = today.minusDays(Integer.MAX_VALUE))

        val data = AccountActivityData(Seq(payment))

        data.hasRecentPayment mustEqual false
      }
    }

    "fetching onlyPayments" should {
      "drop any non-payment transactions" in {
        val payment = createPayment()
        val debit   = createDebit()

        val data = AccountActivityData(Seq(payment, debit))

        data.onlyPayments.loneElement mustEqual payment
      }
    }

  }
}
