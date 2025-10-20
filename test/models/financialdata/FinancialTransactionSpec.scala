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

package models.financialdata

import base.SpecBase
import cats.syntax.option._
import models.EtmpMainTransactionRef._
import models.EtmpSubtransactionRef
import models.financialdata.FinancialTransaction.OutstandingCharge.{LatePaymentInterestOutstandingCharge, RepaymentInterestOutstandingCharge, UktrMainOutstandingCharge}
import models.financialdata.FinancialTransaction.{OutstandingCharge, Payment}
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import java.time.LocalDate
import scala.reflect.ClassTag

class FinancialTransactionSpec extends SpecBase with ScalaCheckDrivenPropertyChecks {
  "Creating outstanding charges" should {

    "result in a charge transaction based on the input main transaction reference" in {

      behave like createsChargeOfType[UktrMainOutstandingCharge](mainTxRef = UkTaxReturnMain)
      behave like createsChargeOfType[LatePaymentInterestOutstandingCharge](mainTxRef = LatePaymentInterest)
      behave like createsChargeOfType[RepaymentInterestOutstandingCharge](mainTxRef = RepaymentInterest)

      def createsChargeOfType[ExpectedType <: OutstandingCharge: ClassTag](mainTxRef: ChargeRef): Assertion = {
        val createdCharge = OutstandingCharge.apply(mainTxRef)(
          TaxPeriod(from = LocalDate.now().minusYears(1), LocalDate.now()),
          EtmpSubtransactionRef.Dtt,
          outstandingAmount = 10000.99,
          OutstandingCharge.FinancialItems(earliestDueDate = LocalDate.now(), items = Seq.empty)
        )
        createdCharge.mainTransactionRef mustBe mainTxRef
        createdCharge mustBe an[ExpectedType]
      }
    }
  }

  "Payment financial items" when {
    "finding the latest clearing date" should {
      "return the furthest-forward clearing date from the related items" in forAll(
        anyOutstandingFinancialItem,
        anyOutstandingFinancialItem,
        anyOutstandingFinancialItem
      ) { (item1, item2, item3) =>
        val paymentItems = Payment.FinancialItems(
          Seq(
            FinancialItem(dueDate = None, clearingDate = LocalDate.MAX.some),
            item1,
            item2,
            item3
          )
        )

        paymentItems.latestClearingDate.value mustBe LocalDate.MAX
      }

      "return None when no clearing dates are defined" in forAll(anyOutstandingFinancialItem) { baseFinancialItem =>
        val missingClearingDate = baseFinancialItem.copy(clearingDate = None)
        Payment.FinancialItems(Seq(missingClearingDate)).latestClearingDate must not be defined
      }

      "return None when the  no items" in {
        Payment.FinancialItems(Seq.empty).latestClearingDate must not be defined
      }
    }
  }
}
