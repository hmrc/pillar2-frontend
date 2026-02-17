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

package models

import base.SpecBase
import play.api.libs.json.{JsError, JsSuccess, Json}

import java.time.{LocalDate, LocalDateTime}

class AccountActivityResponseSpec extends SpecBase {

  val sampleJson: String =
    """{
      |  "processingDate": "2025-01-06T10:30:00",
      |  "transactionDetails": [
      |    {
      |      "transactionType": "PAYMENT",
      |      "transactionDesc": "Pillar 2 Payment on Account",
      |      "transactionDate": "2025-10-15",
      |      "originalAmount": 1000,
      |      "clearedAmount": 1000,
      |      "clearingDetails": [
      |        {
      |          "transactionDesc": "UKTR - DTT",
      |          "chargeRefNo": "X123456789012",
      |          "dueDate": "2025-12-31",
      |          "amount": 1000,
      |          "clearingDate": "2025-10-15",
      |          "clearingReason": "Allocated to Charge"
      |        }
      |      ]
      |    },
      |    {
      |      "transactionType": "CREDIT",
      |      "transactionDesc": "Repayment interest - UKTR",
      |      "chargeRefNo": "XR23456789012",
      |      "transactionDate": "2025-03-15",
      |      "originalAmount": -100,
      |      "outstandingAmount": -100
      |    },
      |    {
      |      "transactionType": "DEBIT",
      |      "transactionDesc": "UKTR - DTT",
      |      "startDate": "2025-01-01",
      |      "endDate": "2025-12-31",
      |      "accruedInterest": 35,
      |      "chargeRefNo": "X123456789012",
      |      "transactionDate": "2025-02-15",
      |      "dueDate": "2025-12-31",
      |      "originalAmount": 2000,
      |      "outstandingAmount": 1000,
      |      "standOverAmount": 500,
      |      "appealFlag": true
      |    }
      |  ]
      |}""".stripMargin

  val expectedResponse: AccountActivityResponse = AccountActivityResponse(
    processingDate = LocalDateTime.of(2025, 1, 6, 10, 30, 0),
    transactionDetails = Seq(
      AccountActivityTransaction(
        transactionType = TransactionType.Payment,
        transactionDesc = "Pillar 2 Payment on Account",
        startDate = None,
        endDate = None,
        accruedInterest = None,
        chargeRefNo = None,
        transactionDate = LocalDate.of(2025, 10, 15),
        dueDate = None,
        originalAmount = BigDecimal(1000),
        outstandingAmount = None,
        clearedAmount = Some(BigDecimal(1000)),
        standOverAmount = None,
        appealFlag = None,
        clearingDetails = Some(
          Seq(
            AccountActivityClearance(
              transactionDesc = "UKTR - DTT",
              chargeRefNo = Some("X123456789012"),
              dueDate = Some(LocalDate.of(2025, 12, 31)),
              amount = BigDecimal(1000),
              clearingDate = LocalDate.of(2025, 10, 15),
              clearingReason = Some("Allocated to Charge")
            )
          )
        )
      ),
      AccountActivityTransaction(
        transactionType = TransactionType.Credit,
        transactionDesc = "Repayment interest - UKTR",
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
      ),
      AccountActivityTransaction(
        transactionType = TransactionType.Debit,
        transactionDesc = "UKTR - DTT",
        startDate = Some(LocalDate.of(2025, 1, 1)),
        endDate = Some(LocalDate.of(2025, 12, 31)),
        accruedInterest = Some(BigDecimal(35)),
        chargeRefNo = Some("X123456789012"),
        transactionDate = LocalDate.of(2025, 2, 15),
        dueDate = Some(LocalDate.of(2025, 12, 31)),
        originalAmount = BigDecimal(2000),
        outstandingAmount = Some(BigDecimal(1000)),
        clearedAmount = None,
        standOverAmount = Some(BigDecimal(500)),
        appealFlag = Some(true),
        clearingDetails = None
      )
    )
  )

  "AccountActivityResponse" should {
    "parse JSON correctly" in {
      val result = Json.parse(sampleJson).validate[AccountActivityResponse]
      result mustBe JsSuccess(expectedResponse)
    }

    "serialize to JSON correctly" in {
      val json   = Json.toJson(expectedResponse)
      val result = json.validate[AccountActivityResponse]
      result mustBe JsSuccess(expectedResponse)
    }
  }

  "TransactionType" should {
    "parse Payment correctly" in {
      Json.parse("\"PAYMENT\"").validate[TransactionType] mustBe JsSuccess(TransactionType.Payment)
    }

    "parse Credit correctly" in {
      Json.parse("\"CREDIT\"").validate[TransactionType] mustBe JsSuccess(TransactionType.Credit)
    }

    "parse Debit correctly" in {
      Json.parse("\"DEBIT\"").validate[TransactionType] mustBe JsSuccess(TransactionType.Debit)
    }

    "fail for unknown transaction type" in {
      Json.parse("\"Unknown\"").validate[TransactionType] mustBe a[JsError]
    }

    "serialize correctly" in {
      Json.toJson[TransactionType](TransactionType.Payment) mustBe Json.parse("\"PAYMENT\"")
      Json.toJson[TransactionType](TransactionType.Credit) mustBe Json.parse("\"CREDIT\"")
      Json.toJson[TransactionType](TransactionType.Debit) mustBe Json.parse("\"DEBIT\"")
    }
  }

  "AccountActivityResponse.toFinancialHistory" should {
    "return empty list when transactionDetails is empty" in {
      val response = AccountActivityResponse(
        processingDate = LocalDateTime.now(),
        transactionDetails = Seq.empty
      )
      response.toTransactions mustBe empty
    }

    "keep multiple Payment transactions as separate entries" in {
      val response = AccountActivityResponse(
        processingDate = LocalDateTime.now(),
        transactionDetails = Seq(
          AccountActivityTransaction(
            transactionType = TransactionType.Payment,
            transactionDesc = "Pillar 2 Payment on Account",
            startDate = None,
            endDate = None,
            accruedInterest = None,
            chargeRefNo = None,
            transactionDate = LocalDate.of(2025, 1, 10),
            dueDate = None,
            originalAmount = BigDecimal(300),
            outstandingAmount = None,
            clearedAmount = None,
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = None
          ),
          AccountActivityTransaction(
            transactionType = TransactionType.Payment,
            transactionDesc = "Pillar 2 Payment on Account",
            startDate = None,
            endDate = None,
            accruedInterest = None,
            chargeRefNo = None,
            transactionDate = LocalDate.of(2025, 1, 15),
            dueDate = None,
            originalAmount = BigDecimal(500),
            outstandingAmount = None,
            clearedAmount = None,
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = None
          ),
          AccountActivityTransaction(
            transactionType = TransactionType.Payment,
            transactionDesc = "Pillar 2 Payment on Account",
            startDate = None,
            endDate = None,
            accruedInterest = None,
            chargeRefNo = None,
            transactionDate = LocalDate.of(2025, 1, 5),
            dueDate = None,
            originalAmount = BigDecimal(200),
            outstandingAmount = None,
            clearedAmount = None,
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = None
          )
        )
      )

      val result = response.toTransactions

      result must have size 3
      // Sorted descending (newest first)
      result(0).date mustBe LocalDate.of(2025, 1, 15)
      result(0).paymentType mustBe "payment"
      result(0).amountPaid mustBe BigDecimal(500)
      result(1).date mustBe LocalDate.of(2025, 1, 10)
      result(1).amountPaid mustBe BigDecimal(300)
      result(2).date mustBe LocalDate.of(2025, 1, 5)
      result(2).amountPaid mustBe BigDecimal(200)
    }

    "convert Payment transactions to 'Payment' display name" in {
      val response = AccountActivityResponse(
        processingDate = LocalDateTime.now(),
        transactionDetails = Seq(
          AccountActivityTransaction(
            transactionType = TransactionType.Payment,
            transactionDesc = "Pillar 2 Payment on Account",
            startDate = None,
            endDate = None,
            accruedInterest = None,
            chargeRefNo = None,
            transactionDate = LocalDate.of(2025, 1, 15),
            dueDate = None,
            originalAmount = BigDecimal(500),
            outstandingAmount = None,
            clearedAmount = None,
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = None
          )
        )
      )

      val result = response.toTransactions

      result must have size 1
      result.head.date mustBe LocalDate.of(2025, 1, 15)
      result.head.paymentType mustBe "payment"
      result.head.amountPaid mustBe BigDecimal(500)
      result.head.amountRepaid mustBe BigDecimal(0)
    }

    "extract Repayment from clearingDetails with 'Outgoing payment - Paid' reason" in {
      val response = AccountActivityResponse(
        processingDate = LocalDateTime.now(),
        transactionDetails = Seq(
          AccountActivityTransaction(
            transactionType = TransactionType.Payment,
            transactionDesc = "Pillar 2 Payment on Account",
            startDate = None,
            endDate = None,
            accruedInterest = None,
            chargeRefNo = Some("X123456789012"),
            transactionDate = LocalDate.of(2025, 2, 1),
            dueDate = None,
            originalAmount = BigDecimal(500),
            outstandingAmount = None,
            clearedAmount = Some(BigDecimal(500)),
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = Some(
              Seq(
                AccountActivityClearance(
                  transactionDesc = "Repayment to taxpayer",
                  chargeRefNo = None,
                  dueDate = None,
                  amount = BigDecimal(500),
                  clearingDate = LocalDate.of(2025, 2, 20),
                  clearingReason = Some("Outgoing payment - Paid")
                )
              )
            )
          )
        )
      )

      val result = response.toTransactions

      result must have size 1
      result.head.date mustBe LocalDate.of(2025, 2, 20)
      result.head.paymentType mustBe "repayment"
      result.head.amountPaid mustBe BigDecimal(0)
      result.head.amountRepaid mustBe BigDecimal(500)
    }

    "filter out non-PaymentOnAccount transactions" in {
      val response = AccountActivityResponse(
        processingDate = LocalDateTime.now(),
        transactionDetails = Seq(
          AccountActivityTransaction(
            transactionType = TransactionType.Debit,
            transactionDesc = "Charge",
            startDate = None,
            endDate = None,
            accruedInterest = None,
            chargeRefNo = None,
            transactionDate = LocalDate.of(2025, 3, 10),
            dueDate = None,
            originalAmount = BigDecimal(300),
            outstandingAmount = None,
            clearedAmount = None,
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = None
          )
        )
      )

      val result = response.toTransactions

      result mustBe empty
    }

    "convert separate PaymentOnAccount transactions for payments and repayments" in {
      val response = AccountActivityResponse(
        processingDate = LocalDateTime.now(),
        transactionDetails = Seq(
          // Payment transaction (no repayment clearingDetails)
          AccountActivityTransaction(
            transactionType = TransactionType.Payment,
            transactionDesc = "Pillar 2 Payment on Account",
            startDate = None,
            endDate = None,
            accruedInterest = None,
            chargeRefNo = None,
            transactionDate = LocalDate.of(2025, 1, 1),
            dueDate = None,
            originalAmount = BigDecimal(100),
            outstandingAmount = None,
            clearedAmount = Some(BigDecimal(100)),
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = Some(
              Seq(
                AccountActivityClearance(
                  transactionDesc = "UKTR - DTT",
                  chargeRefNo = Some("X123456789012"),
                  dueDate = None,
                  amount = BigDecimal(100),
                  clearingDate = LocalDate.of(2025, 1, 1),
                  clearingReason = Some("Allocated to Charge")
                )
              )
            )
          ),
          // Repayment transaction (has repayment clearingDetails)
          AccountActivityTransaction(
            transactionType = TransactionType.Payment,
            transactionDesc = "Pillar 2 Payment on Account",
            startDate = None,
            endDate = None,
            accruedInterest = None,
            chargeRefNo = None,
            transactionDate = LocalDate.of(2025, 1, 2),
            dueDate = None,
            originalAmount = BigDecimal(50),
            outstandingAmount = None,
            clearedAmount = Some(BigDecimal(50)),
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = Some(
              Seq(
                AccountActivityClearance(
                  transactionDesc = "Repayment",
                  chargeRefNo = None,
                  dueDate = None,
                  amount = BigDecimal(50),
                  clearingDate = LocalDate.of(2025, 1, 3),
                  clearingReason = Some("Outgoing payment - Paid")
                )
              )
            )
          ),
          // Debit - should be filtered
          AccountActivityTransaction(
            transactionType = TransactionType.Debit,
            transactionDesc = "Charge - should be filtered",
            startDate = None,
            endDate = None,
            accruedInterest = None,
            chargeRefNo = None,
            transactionDate = LocalDate.of(2025, 1, 2),
            dueDate = None,
            originalAmount = BigDecimal(500),
            outstandingAmount = None,
            clearedAmount = None,
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = None
          ),
          // Credit (not RPI) - should be ignored
          AccountActivityTransaction(
            transactionType = TransactionType.Credit,
            transactionDesc = "Some Credit - should be ignored",
            startDate = None,
            endDate = None,
            accruedInterest = None,
            chargeRefNo = None,
            transactionDate = LocalDate.of(2025, 1, 4),
            dueDate = None,
            originalAmount = BigDecimal(-50),
            outstandingAmount = None,
            clearedAmount = None,
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = None
          )
        )
      )

      val result = response.toTransactions

      result must have size 2
      // Sorted descending: repayment (Jan 3) before payment (Jan 1)
      result.head.paymentType mustBe "repayment"
      result.head.date mustBe LocalDate.of(2025, 1, 3)
      result.head.amountRepaid mustBe BigDecimal(50)
      result(1).paymentType mustBe "payment"
      result(1).date mustBe LocalDate.of(2025, 1, 1)
      result(1).amountPaid mustBe BigDecimal(100)
    }

    "extract Repayment Interest from Credit transactions with RPI description" in {
      val response = AccountActivityResponse(
        processingDate = LocalDateTime.now(),
        transactionDetails = Seq(
          AccountActivityTransaction(
            transactionType = TransactionType.Credit,
            transactionDesc = "Repayment interest - UKTR",
            startDate = None,
            endDate = None,
            accruedInterest = None,
            chargeRefNo = Some("XR23456789012"),
            transactionDate = LocalDate.of(2025, 3, 15),
            dueDate = None,
            originalAmount = BigDecimal(-100),
            outstandingAmount = Some(BigDecimal(-100)),
            clearedAmount = Some(BigDecimal(-100)),
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = Some(
              Seq(
                AccountActivityClearance(
                  transactionDesc = "Repayment",
                  chargeRefNo = Some("X123456789012"),
                  dueDate = None,
                  amount = BigDecimal(100),
                  clearingDate = LocalDate.of(2025, 3, 20),
                  clearingReason = Some("Outgoing payment - Paid")
                )
              )
            )
          )
        )
      )

      val result = response.toTransactions

      result must have size 1
      result.head.paymentType mustBe "repaymentInterest"
      result.head.date mustBe LocalDate.of(2025, 3, 20)
      result.head.amountPaid mustBe BigDecimal(0)
      result.head.amountRepaid mustBe BigDecimal(100)
    }
  }

  "AccountActivityClearance" should {
    "parse clearance details" in {
      val clearanceJson =
        """{
          |  "transactionDesc": "Pillar 2 UK Tax Return",
          |  "amount": 500,
          |  "clearingDate": "2025-06-15"
          |}""".stripMargin

      val result = Json.parse(clearanceJson).validate[AccountActivityClearance]
      result.isSuccess mustBe true
      result.get.transactionDesc mustBe "Pillar 2 UK Tax Return"
      result.get.amount mustBe BigDecimal(500)
      result.get.clearingDate mustBe LocalDate.of(2025, 6, 15)
      result.get.chargeRefNo mustBe None
      result.get.clearingReason mustBe None
    }
  }

  "AccountActivityResponse.toOutstandingPayments" should {
    "return empty list when no outstanding debits exist" in {
      val response = AccountActivityResponse(
        processingDate = LocalDateTime.now(),
        transactionDetails = Seq(
          AccountActivityTransaction(
            transactionType = TransactionType.Payment,
            transactionDesc = "Pillar 2 Payment on Account",
            startDate = None,
            endDate = None,
            accruedInterest = None,
            chargeRefNo = None,
            transactionDate = LocalDate.of(2025, 1, 15),
            dueDate = None,
            originalAmount = BigDecimal(500),
            outstandingAmount = None,
            clearedAmount = None,
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = None
          )
        )
      )

      response.toOutstandingPayments mustBe empty
    }

    "return empty list when outstanding amount is 0" in {
      val response = AccountActivityResponse(
        processingDate = LocalDateTime.now(),
        transactionDetails = Seq(
          AccountActivityTransaction(
            transactionType = TransactionType.Debit,
            transactionDesc = "UKTR - DTT",
            startDate = Some(LocalDate.of(2025, 1, 1)),
            endDate = Some(LocalDate.of(2025, 12, 31)),
            accruedInterest = None,
            chargeRefNo = None,
            transactionDate = LocalDate.of(2025, 2, 15),
            dueDate = Some(LocalDate.of(2025, 12, 31)),
            originalAmount = BigDecimal(2000),
            outstandingAmount = Some(BigDecimal(0)),
            clearedAmount = Some(BigDecimal(2000)),
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = None
          )
        )
      )

      response.toOutstandingPayments mustBe empty
    }

    "filter out transactions without startDate and endDate" in {
      val response = AccountActivityResponse(
        processingDate = LocalDateTime.now(),
        transactionDetails = Seq(
          AccountActivityTransaction(
            transactionType = TransactionType.Debit,
            transactionDesc = "UKTR - DTT",
            startDate = None,
            endDate = None,
            accruedInterest = None,
            chargeRefNo = None,
            transactionDate = LocalDate.of(2025, 2, 15),
            dueDate = Some(LocalDate.of(2025, 12, 31)),
            originalAmount = BigDecimal(2000),
            outstandingAmount = Some(BigDecimal(1000)),
            clearedAmount = None,
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = None
          )
        )
      )

      response.toOutstandingPayments mustBe empty
    }

    "convert outstanding debit transactions to OutstandingPaymentSummary grouped by accounting period" in {
      val response = AccountActivityResponse(
        processingDate = LocalDateTime.now(),
        transactionDetails = Seq(
          AccountActivityTransaction(
            transactionType = TransactionType.Debit,
            transactionDesc = "UKTR - DTT",
            startDate = Some(LocalDate.of(2025, 1, 1)),
            endDate = Some(LocalDate.of(2025, 12, 31)),
            accruedInterest = None,
            chargeRefNo = Some("X123456789012"),
            transactionDate = LocalDate.of(2025, 2, 15),
            dueDate = Some(LocalDate.of(2025, 12, 31)),
            originalAmount = BigDecimal(2000),
            outstandingAmount = Some(BigDecimal(1000)),
            clearedAmount = Some(BigDecimal(1000)),
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = None
          ),
          AccountActivityTransaction(
            transactionType = TransactionType.Debit,
            transactionDesc = "UKTR - MTT (IIR)",
            startDate = Some(LocalDate.of(2025, 1, 1)),
            endDate = Some(LocalDate.of(2025, 12, 31)),
            accruedInterest = None,
            chargeRefNo = Some("X123456789013"),
            transactionDate = LocalDate.of(2025, 2, 15),
            dueDate = Some(LocalDate.of(2025, 12, 30)),
            originalAmount = BigDecimal(1500),
            outstandingAmount = Some(BigDecimal(1500)),
            clearedAmount = None,
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = None
          )
        )
      )

      val result = response.toOutstandingPayments

      result must have size 1
      result.head.accountingPeriod.startDate mustBe LocalDate.of(2025, 1, 1)
      result.head.accountingPeriod.endDate mustBe LocalDate.of(2025, 12, 31)
      result.head.items must have size 2
      result.head.items.head.description mustBe "UKTR - DTT"
      result.head.items.head.outstandingAmount mustBe BigDecimal(1000)
      result.head.items.head.dueDate mustBe LocalDate.of(2025, 12, 31)
      result.head.items(1).description mustBe "UKTR - MTT (IIR)"
      result.head.items(1).outstandingAmount mustBe BigDecimal(1500)
      result.head.items(1).dueDate mustBe LocalDate.of(2025, 12, 30)
      // Items should be sorted by dueDate descending
      result.head.items.map(_.dueDate) mustBe Seq(LocalDate.of(2025, 12, 31), LocalDate.of(2025, 12, 30))
    }

    "group transactions by different accounting periods" in {
      val response = AccountActivityResponse(
        processingDate = LocalDateTime.now(),
        transactionDetails = Seq(
          AccountActivityTransaction(
            transactionType = TransactionType.Debit,
            transactionDesc = "UKTR - DTT",
            startDate = Some(LocalDate.of(2025, 1, 1)),
            endDate = Some(LocalDate.of(2025, 12, 31)),
            accruedInterest = None,
            chargeRefNo = Some("X123456789012"),
            transactionDate = LocalDate.of(2025, 2, 15),
            dueDate = Some(LocalDate.of(2025, 12, 31)),
            originalAmount = BigDecimal(2000),
            outstandingAmount = Some(BigDecimal(1000)),
            clearedAmount = None,
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = None
          ),
          AccountActivityTransaction(
            transactionType = TransactionType.Debit,
            transactionDesc = "Determination - DTT",
            startDate = Some(LocalDate.of(2026, 1, 1)),
            endDate = Some(LocalDate.of(2026, 12, 31)),
            accruedInterest = None,
            chargeRefNo = Some("X123456789013"),
            transactionDate = LocalDate.of(2026, 2, 15),
            dueDate = Some(LocalDate.of(2026, 12, 31)),
            originalAmount = BigDecimal(3000),
            outstandingAmount = Some(BigDecimal(3000)),
            clearedAmount = None,
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = None
          )
        )
      )

      val result = response.toOutstandingPayments

      result must have size 2
      // Should be sorted by endDate descending (2026 period first)
      result.head.accountingPeriod.endDate mustBe LocalDate.of(2026, 12, 31)
      result.head.items.head.description mustBe "Determination - DTT"
      result(1).accountingPeriod.endDate mustBe LocalDate.of(2025, 12, 31)
      result(1).items.head.description mustBe "UKTR - DTT"
    }

    "use transactionDate as fallback when dueDate is missing" in {
      val response = AccountActivityResponse(
        processingDate = LocalDateTime.now(),
        transactionDetails = Seq(
          AccountActivityTransaction(
            transactionType = TransactionType.Debit,
            transactionDesc = "UKTR - DTT",
            startDate = Some(LocalDate.of(2025, 1, 1)),
            endDate = Some(LocalDate.of(2025, 12, 31)),
            accruedInterest = None,
            chargeRefNo = Some("X123456789012"),
            transactionDate = LocalDate.of(2025, 2, 15),
            dueDate = None,
            originalAmount = BigDecimal(2000),
            outstandingAmount = Some(BigDecimal(1000)),
            clearedAmount = None,
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = None
          )
        )
      )

      val result = response.toOutstandingPayments

      result.head.items.head.dueDate mustBe LocalDate.of(2025, 2, 15)
    }

    "use transactionDate as fallback when startDate or endDate is missing" in {
      val response = AccountActivityResponse(
        processingDate = LocalDateTime.now(),
        transactionDetails = Seq(
          AccountActivityTransaction(
            transactionType = TransactionType.Debit,
            transactionDesc = "UKTR - DTT",
            startDate = Some(LocalDate.of(2025, 1, 1)),
            endDate = None,
            accruedInterest = None,
            chargeRefNo = Some("X123456789012"),
            transactionDate = LocalDate.of(2025, 2, 15),
            dueDate = Some(LocalDate.of(2025, 12, 31)),
            originalAmount = BigDecimal(2000),
            outstandingAmount = Some(BigDecimal(1000)),
            clearedAmount = None,
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = None
          )
        )
      )

      val result = response.toOutstandingPayments

      result.head.accountingPeriod.startDate mustBe LocalDate.of(2025, 1, 1)
      result.head.accountingPeriod.endDate mustBe LocalDate.of(2025, 2, 15) // Uses transactionDate as fallback
    }

    "map unknown transaction descriptions to original description" in {
      val response = AccountActivityResponse(
        processingDate = LocalDateTime.now(),
        transactionDetails = Seq(
          AccountActivityTransaction(
            transactionType = TransactionType.Debit,
            transactionDesc = "Unknown Transaction Type",
            startDate = Some(LocalDate.of(2025, 1, 1)),
            endDate = Some(LocalDate.of(2025, 12, 31)),
            accruedInterest = None,
            chargeRefNo = None,
            transactionDate = LocalDate.of(2025, 2, 15),
            dueDate = Some(LocalDate.of(2025, 12, 31)),
            originalAmount = BigDecimal(2000),
            outstandingAmount = Some(BigDecimal(1000)),
            clearedAmount = None,
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = None
          )
        )
      )

      val result = response.toOutstandingPayments

      result.head.items.head.description mustBe "Unknown Transaction Type"
    }

    "append accruing interest to ui description when needed" in {
      val response = AccountActivityResponse(
        processingDate = LocalDateTime.now(),
        transactionDetails = Seq(
          AccountActivityTransaction(
            transactionType = TransactionType.Debit,
            transactionDesc = "UKTR - DTT",
            startDate = Some(LocalDate.of(2025, 1, 1)),
            endDate = Some(LocalDate.of(2025, 12, 31)),
            accruedInterest = Some(BigDecimal(500)),
            chargeRefNo = None,
            transactionDate = LocalDate.of(2025, 2, 15),
            dueDate = Some(LocalDate.of(2025, 12, 31)),
            originalAmount = BigDecimal(2000),
            outstandingAmount = Some(BigDecimal(2000)),
            clearedAmount = None,
            standOverAmount = None,
            appealFlag = None,
            clearingDetails = None
          )
        )
      )

      response.toOutstandingPayments.head.items.head.description mustBe "UKTR - DTT accruing interest"
    }
  }

  "AccountActivityResponse.totalAccruedInterest" should {
    "calculate total accrued Interest accurately for relevant transactions" in {
      val accruedInterest = expectedResponse.totalAccruedInterest
      accruedInterest mustEqual 35
    }
  }
}
