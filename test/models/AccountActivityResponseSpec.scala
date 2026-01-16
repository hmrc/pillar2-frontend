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
      |      "transactionType": "Payment",
      |      "transactionDesc": "On Account Pillar 2 (Payment on Account)",
      |      "transactionDate": "2025-10-15",
      |      "originalAmount": 10000,
      |      "outstandingAmount": 1000,
      |      "clearedAmount": 9000,
      |      "clearingDetails": [
      |        {
      |          "transactionDesc": "Pillar 2 UK Tax Return Pillar 2 DTT",
      |          "chargeRefNo": "X123456789012",
      |          "dueDate": "2025-12-31",
      |          "amount": 2000,
      |          "clearingDate": "2025-10-15",
      |          "clearingReason": "Allocated to Charge"
      |        }
      |      ]
      |    },
      |    {
      |      "transactionType": "Credit",
      |      "transactionDesc": "Pillar 2 UKTR RPI Pillar 2 OECD RPI",
      |      "chargeRefNo": "XR23456789012",
      |      "transactionDate": "2025-03-15",
      |      "originalAmount": -100,
      |      "outstandingAmount": -100
      |    },
      |    {
      |      "transactionType": "Debit",
      |      "transactionDesc": "Pillar 2 UK Tax Return Pillar 2 DTT",
      |      "startDate": "2025-01-01",
      |      "endDate": "2025-12-31",
      |      "accruedInterest": 35,
      |      "chargeRefNo": "X123456789012",
      |      "transactionDate": "2025-02-15",
      |      "dueDate": "2025-12-31",
      |      "originalAmount": 2000,
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
      ),
      AccountActivityTransaction(
        transactionType = TransactionType.Debit,
        transactionDesc = "Pillar 2 UK Tax Return Pillar 2 DTT",
        startDate = Some(LocalDate.of(2025, 1, 1)),
        endDate = Some(LocalDate.of(2025, 12, 31)),
        accruedInterest = Some(BigDecimal(35)),
        chargeRefNo = Some("X123456789012"),
        transactionDate = LocalDate.of(2025, 2, 15),
        dueDate = Some(LocalDate.of(2025, 12, 31)),
        originalAmount = BigDecimal(2000),
        outstandingAmount = None,
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
      Json.parse("\"Payment\"").validate[TransactionType] mustBe JsSuccess(TransactionType.Payment)
    }

    "parse Credit correctly" in {
      Json.parse("\"Credit\"").validate[TransactionType] mustBe JsSuccess(TransactionType.Credit)
    }

    "parse Debit correctly" in {
      Json.parse("\"Debit\"").validate[TransactionType] mustBe JsSuccess(TransactionType.Debit)
    }

    "fail for unknown transaction type" in {
      Json.parse("\"Unknown\"").validate[TransactionType] mustBe a[JsError]
    }

    "serialize correctly" in {
      Json.toJson[TransactionType](TransactionType.Payment) mustBe Json.parse("\"Payment\"")
      Json.toJson[TransactionType](TransactionType.Credit) mustBe Json.parse("\"Credit\"")
      Json.toJson[TransactionType](TransactionType.Debit) mustBe Json.parse("\"Debit\"")
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
            transactionDesc = "On Account Pillar 2 (Payment on Account)",
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
            transactionDesc = "On Account Pillar 2 (Payment on Account)",
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
            transactionDesc = "On Account Pillar 2 (Payment on Account)",
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
      result(0).date mustBe LocalDate.of(2025, 1, 10)
      result(0).paymentType mustBe "payment"
      result(0).amountPaid mustBe BigDecimal(300)
      result(1).date mustBe LocalDate.of(2025, 1, 15)
      result(1).amountPaid mustBe BigDecimal(500)
      result(2).date mustBe LocalDate.of(2025, 1, 5)
      result(2).amountPaid mustBe BigDecimal(200)
    }

    "convert Payment transactions to 'Payment' display name" in {
      val response = AccountActivityResponse(
        processingDate = LocalDateTime.now(),
        transactionDetails = Seq(
          AccountActivityTransaction(
            transactionType = TransactionType.Payment,
            transactionDesc = "On Account Pillar 2 (Payment on Account)",
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
            transactionDesc = "On Account Pillar 2 (Payment on Account)",
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
            transactionDesc = "On Account Pillar 2 (Payment on Account)",
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
                  transactionDesc = "Pillar 2 UK Tax Return Pillar 2 DTT",
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
            transactionDesc = "On Account Pillar 2 (Payment on Account)",
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
      result.head.paymentType mustBe "payment"
      result.head.date mustBe LocalDate.of(2025, 1, 1)
      result.head.amountPaid mustBe BigDecimal(100)
      result(1).paymentType mustBe "repayment"
      result(1).date mustBe LocalDate.of(2025, 1, 3)
      result(1).amountRepaid mustBe BigDecimal(50)
    }

    "extract Repayment Interest from Credit transactions with RPI description" in {
      val response = AccountActivityResponse(
        processingDate = LocalDateTime.now(),
        transactionDetails = Seq(
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
}
