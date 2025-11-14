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

package services

import base.SpecBase
import cats.syntax.option._
import connectors.FinancialDataConnector
import models.financialdata.FinancialTransaction.OutstandingCharge
import models.financialdata.FinancialTransaction.OutstandingCharge.{LatePaymentInterestOutstandingCharge, RepaymentInterestOutstandingCharge, UktrMainOutstandingCharge}
import models.financialdata._
import models.subscription.AccountingPeriod
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.bind
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.Future

class FinancialDataServiceSpec extends SpecBase with OptionValues with ScalaCheckPropertyChecks with ScalaFutures {

  val application: Application = applicationBuilder()
    .overrides(
      bind[FinancialDataConnector].toInstance(mockFinancialDataConnector)
    )
    .build()

  val service: FinancialDataService = application.injector.instanceOf[FinancialDataService]

  val today: LocalDate = LocalDate.now()

  val dueApiFinancialTransaction: FinancialDataResponse.FinancialItem =
    FinancialDataResponse.FinancialItem(dueDate = Some(today.plusDays(1)), clearingDate = None)
  val clearedApiFinancialTransaction: FinancialDataResponse.FinancialItem =
    FinancialDataResponse.FinancialItem(dueDate = Some(today.minusDays(8)), clearingDate = Some(today.minusDays(1)))
  val uktrMainApiTransaction: FinancialDataResponse.FinancialTransaction = FinancialDataResponse.FinancialTransaction(
    mainTransaction = Some("6500"),
    subTransaction = Some("6233"),
    taxPeriodFrom = Some(today.minusMonths(1)),
    taxPeriodTo = Some(today),
    outstandingAmount = Some(BigDecimal(100)),
    items = Seq(dueApiFinancialTransaction, clearedApiFinancialTransaction)
  )

  val parsedOutstandingChargeValues: (AccountingPeriod, EtmpSubtransactionRef.Dtt.type, BigDecimal, OutstandingCharge.FinancialItems) = (
    AccountingPeriod(uktrMainApiTransaction.taxPeriodFrom.value, uktrMainApiTransaction.taxPeriodTo.value),
    EtmpSubtransactionRef.Dtt,
    uktrMainApiTransaction.outstandingAmount.value,
    OutstandingCharge.FinancialItems(
      earliestDueDate = clearedApiFinancialTransaction.dueDate.value,
      items = Seq(
        FinancialItem(dueApiFinancialTransaction.dueDate, dueApiFinancialTransaction.clearingDate),
        FinancialItem(clearedApiFinancialTransaction.dueDate, clearedApiFinancialTransaction.clearingDate)
      )
    )
  )

  val parsedUktrMainCharge: UktrMainOutstandingCharge = (UktrMainOutstandingCharge.apply _).tupled(parsedOutstandingChargeValues)

  "FinancialDataService" when {

    "retrieveFinancialData" should {
      "return financial data from the connector" in {
        val apiResponse = FinancialDataResponse(Seq(uktrMainApiTransaction))

        when(mockFinancialDataConnector.retrieveFinancialData(any(), any(), any())(any[HeaderCarrier]))
          .thenReturn(Future.successful(apiResponse))

        val result = service.retrieveFinancialData("test-plr", LocalDate.now.minusYears(1), LocalDate.now)

        whenReady(result) { data =>
          data mustBe FinancialData(Seq(parsedUktrMainCharge))
        }
      }

      "propagate errors from the connector" in {
        val application = applicationBuilder()
          .overrides(
            bind[FinancialDataConnector].toInstance(mockFinancialDataConnector)
          )
          .build()

        val service = application.injector.instanceOf[FinancialDataService]

        val exception = new RuntimeException("Connector error")

        when(mockFinancialDataConnector.retrieveFinancialData(any(), any(), any())(any[HeaderCarrier]))
          .thenReturn(Future.failed(exception))

        val result = service.retrieveFinancialData("test-plr", LocalDate.now.minusYears(1), LocalDate.now)

        whenReady(result.failed) { error =>
          error mustBe exception
        }
      }
    }

    "parsing the domain model from the connector response" should {

      "parse transactions for all modelled outstanding charges" when {

        val expectedUktrMain: EtmpSubtransactionRef => UktrMainOutstandingCharge =
          subTxRef => parsedUktrMainCharge.copy(subTransactionRef = subTxRef)
        val expectedUktrLatePaymentInterest: EtmpSubtransactionRef => LatePaymentInterestOutstandingCharge =
          subTxRef => (LatePaymentInterestOutstandingCharge.apply _).tupled(parsedOutstandingChargeValues).copy(subTransactionRef = subTxRef)
        val expectedUktrRepaymentInterest: EtmpSubtransactionRef => RepaymentInterestOutstandingCharge =
          subTxRef => (RepaymentInterestOutstandingCharge.apply _).tupled(parsedOutstandingChargeValues).copy(subTransactionRef = subTxRef)

        forAll(
          Table(
            ("name", "api transaction", "expected output"),
            ("UKTR main transaction", uktrMainApiTransaction, expectedUktrMain),
            ("UKTR late payment interest", uktrMainApiTransaction.copy(mainTransaction = Some("6503")), expectedUktrLatePaymentInterest),
            ("UKTR repayment interest", uktrMainApiTransaction.copy(mainTransaction = Some("6504")), expectedUktrRepaymentInterest)
          )
        ) { (name, apiResponse, parsedExpected) =>
          s"parsing a $name charge transaction" in forAll(Gen.oneOf(EtmpSubtransactionRef.values)) { subTxRef =>
            when(mockFinancialDataConnector.retrieveFinancialData(any(), any(), any())(any[HeaderCarrier]))
              .thenReturn(Future.successful(FinancialDataResponse(Seq(apiResponse.copy(subTransaction = subTxRef.value.some)))))

            val result = service.retrieveFinancialData("fake-plr", LocalDate.now().minusYears(1), LocalDate.now()).futureValue

            result mustBe FinancialData(Seq(parsedExpected(subTxRef)))
          }
        }

      }

      "parse payment transactions" which {

        val populatedFinancialItem = FinancialDataResponse.FinancialItem(
          dueDate = Some(LocalDate.now().minusDays(7)),
          clearingDate = Some(LocalDate.now().minusDays(1))
        )
        val minimumPaymentApiResponse = FinancialDataResponse.FinancialTransaction(
          mainTransaction = Some("0060"),
          subTransaction = None,
          taxPeriodFrom = None,
          taxPeriodTo = None,
          outstandingAmount = None,
          items = Seq.empty
        )

        val apiFinancialItem = Gen.oneOf(
          populatedFinancialItem,
          populatedFinancialItem.copy(dueDate = None),
          populatedFinancialItem.copy(clearingDate = None),
          FinancialDataResponse.FinancialItem(None, None)
        )
        val apiFinancialItems   = Gen.choose(0, 3).flatMap(Gen.listOfN(_, apiFinancialItem))
        val paymentApiResponses = apiFinancialItems.map(items => minimumPaymentApiResponse.copy(items = items))

        "have the required data" in forAll(paymentApiResponses) { paymentResponse =>
          when(mockFinancialDataConnector.retrieveFinancialData(any(), any(), any())(any[HeaderCarrier]))
            .thenReturn(Future.successful(FinancialDataResponse(Seq(paymentResponse))))

          val result        = service.retrieveFinancialData("fake-plr", LocalDate.now().minusYears(1), LocalDate.now()).futureValue
          val expectedItems = paymentResponse.items.map(respItem => FinancialItem(respItem.dueDate, respItem.clearingDate))

          result.financialTransactions                            must have size 1
          result.onlyPayments                                     must have size 1
          result.onlyPayments.headOption.value.paymentItems.items must contain theSameElementsInOrderAs expectedItems
        }
      }

      "drop anything which doesn't match validation from the parsed options" when {

        val unmodelledMainTransactionRef = Gen.numStr.retryUntil(EtmpMainTransactionRef.withValueOpt(_).isEmpty)
        val unmodelledSubTransactionRef  = Gen.numStr.retryUntil(EtmpSubtransactionRef.withValueOpt(_).isEmpty)
        val validApiTransaction          = Gen.const(uktrMainApiTransaction)

        val missingMainTransaction    = validApiTransaction.map(_.copy(mainTransaction = None))
        val unmodelledMainTransaction = unmodelledMainTransactionRef.flatMap(ref => validApiTransaction.map(_.copy(mainTransaction = Some(ref))))

        val missingSubTransaction    = validApiTransaction.map(_.copy(subTransaction = None))
        val unmodelledSubTransaction = unmodelledSubTransactionRef.flatMap(ref => validApiTransaction.map(_.copy(subTransaction = Some(ref))))

        val missingTaxPeriodFrom = validApiTransaction.map(_.copy(taxPeriodFrom = None))
        val missingTaxPeriodTo   = validApiTransaction.map(_.copy(taxPeriodTo = None))

        val missingOutstandingAmount = validApiTransaction.map(_.copy(outstandingAmount = None))
        val outstandingAmountZeroOrLess =
          Gen.choose(Long.MinValue, 0).flatMap(outstanding => validApiTransaction.map(_.copy(outstandingAmount = BigDecimal(outstanding).some)))

        val allChargeItemsMissingDueDate = validApiTransaction.map(tx => tx.copy(items = tx.items.map(_.copy(dueDate = None))))

        forAll(
          Table(
            "reason response is dropped"             -> "invalid response",
            "main transaction ref is not missing"    -> missingMainTransaction,
            "main transaction ref is not recognised" -> unmodelledMainTransaction,
            "sub transaction ref is not missing"     -> missingSubTransaction,
            "sub transaction ref is not recognised"  -> unmodelledSubTransaction,
            "tax period from is missing"             -> missingTaxPeriodFrom,
            "tax period to is missing"               -> missingTaxPeriodTo,
            "outstanding amount is missing"          -> missingOutstandingAmount,
            "outstanding amount is zero or less"     -> outstandingAmountZeroOrLess,
            "no charge items have a due date"        -> allChargeItemsMissingDueDate
          )
        ) { (failureReason, responsesToDrop) =>
          s"transaction is dropped because $failureReason" in forAll(responsesToDrop) { responseToDrop =>
            when(mockFinancialDataConnector.retrieveFinancialData(any(), any(), any())(any[HeaderCarrier]))
              .thenReturn(Future.successful(FinancialDataResponse(Seq(responseToDrop))))

            val result = service.retrieveFinancialData("fake-plr", LocalDate.now().minusYears(1), LocalDate.now()).futureValue

            result.financialTransactions mustBe empty
          }
        }
      }
    }
  }
}
