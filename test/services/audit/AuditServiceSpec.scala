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

package services.audit
import base.SpecBase
import models.audit.RepaymentsAuditEvent
import models.registration.{IncorporatedEntityRegistrationData, PartnershipEntityRegistrationData}
import models.subscription.{AccountingPeriod, NewFilingMemberDetail}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.mockito.captor.ArgCaptor
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import java.time.LocalDate
import scala.concurrent.Future

class AuditServiceSpec extends SpecBase with ScalaFutures with ScalaCheckDrivenPropertyChecks {

  val validRegisterWithIdResponse: IncorporatedEntityRegistrationData =
    Json.parse(validRegistrationWithIdResponse).as[IncorporatedEntityRegistrationData]
  val validRegisterWithIdResponseForLLP: PartnershipEntityRegistrationData =
    Json.parse(validRegistrationWithIdResponseForLLP).as[PartnershipEntityRegistrationData]
  val validReplaceFilingMemberNoId: NewFilingMemberDetail = Json.parse(validReplaceFilingMember).as[NewFilingMemberDetail]
  val validRepayment:               RepaymentsAuditEvent  = Json.parse(validRepaymentDetails).as[RepaymentsAuditEvent]

  def withMockedAuditConnector(test: (AuditService, AuditConnector) => Assertion): Assertion = {
    val mockAuditConnector = mock[AuditConnector]
    test(new AuditService(mockAuditConnector), mockAuditConnector)
  }

  "AuditService" should {
    "return success for auditGrsReturnForLimitedCompany" in withMockedAuditConnector { (auditService, auditConnector) =>
      when(auditConnector.sendExtendedEvent(any())(any(), any()))
        .thenReturn(Future.successful(AuditResult.Success))

      val result = auditService.auditGrsReturnForLimitedCompany(validRegisterWithIdResponse).futureValue
      result mustBe AuditResult.Success
    }

    "return success for auditGrsReturnForLLP" in withMockedAuditConnector { (auditService, auditConnector) =>
      when(auditConnector.sendExtendedEvent(any())(any(), any()))
        .thenReturn(Future.successful(AuditResult.Success))

      val result = auditService.auditGrsReturnForLLP(validRegisterWithIdResponseForLLP).futureValue
      result mustBe AuditResult.Success
    }

    "return success for auditGrsReturnNfmForLimitedCompany" in withMockedAuditConnector { (auditService, auditConnector) =>
      when(auditConnector.sendExtendedEvent(any())(any(), any()))
        .thenReturn(Future.successful(AuditResult.Success))

      val result = auditService.auditGrsReturnNfmForLimitedCompany(validRegisterWithIdResponse).futureValue
      result mustBe AuditResult.Success
    }

    "return success for auditGrsReturnNfmForLLP" in withMockedAuditConnector { (auditService, auditConnector) =>
      when(auditConnector.sendExtendedEvent(any())(any(), any()))
        .thenReturn(Future.successful(AuditResult.Success))

      val result = auditService.auditGrsReturnNfmForLLP(validRegisterWithIdResponseForLLP).futureValue
      result mustBe AuditResult.Success
    }

    "return success for auditRepayments" in withMockedAuditConnector { (auditService, auditConnector) =>
      when(auditConnector.sendExtendedEvent(any())(any(), any()))
        .thenReturn(Future.successful(AuditResult.Success))

      val result = auditService.auditRepayments(validRepayment).futureValue
      result mustBe AuditResult.Success
    }

    "return success for auditReplaceFilingMember" in withMockedAuditConnector { (auditService, auditConnector) =>
      when(auditConnector.sendExtendedEvent(any())(any(), any()))
        .thenReturn(Future.successful(AuditResult.Success))

      val result = auditService.auditReplaceFilingMember(validReplaceFilingMemberNoId).futureValue
      result mustBe AuditResult.Success
    }

    val pillarReference = "PLR1234567890"
    val apStartDate     = "2024-03-20"
    val apEndDate       = "2025-03-20"

    "auditing a BTN submission" should {
      val responseOk                  = 200
      val responseInternalServerError = 500
      val responseProcessedAt         = "2024-03-20T07:32:03Z"
      val responseSuccessMessage      = "Success"
      val responseErrorCode           = "InternalIssueError"
      val responseErrorMessage        = "Failure"

      "return Success when audit call is successful" when {

        "BTN was successful" in withMockedAuditConnector { (auditService, auditConnector) =>
          when(auditConnector.sendExtendedEvent(any())(any(), any()))
            .thenReturn(Future.successful(AuditResult.Success))

          val result = auditService
            .auditBTNSubmission(
              pillarReference = pillarReference,
              accountingPeriod = AccountingPeriod(
                startDate = LocalDate.parse(apStartDate),
                endDate = LocalDate.parse(apEndDate)
              ),
              entitiesInsideAndOutsideUK = true,
              apiResponseData = models.audit.ApiResponseData(
                statusCode = responseOk,
                processingDate = responseProcessedAt,
                errorCode = None,
                responseMessage = responseSuccessMessage
              )
            )
            .futureValue

          result mustBe AuditResult.Success

          val captor = ArgCaptor[ExtendedDataEvent]
          verify(auditConnector).sendExtendedEvent(captor)(any, any)

          captor.value.auditSource mustBe "pillar2-frontend"
          captor.value.auditType mustBe "belowThresholdNotification"
          captor.value.detail mustBe Json.obj(
            "pillarReference"            -> pillarReference,
            "accountingPeriodStart"      -> apStartDate,
            "accountingPeriodEnd"        -> apEndDate,
            "entitiesInsideAndOutsideUK" -> true,
            "apiResponseData" -> Json.obj(
              "statusCode"     -> responseOk,
              "processingDate" -> responseProcessedAt,
              // no errorCode
              "responseMessage" -> "Success"
            )
          )
        }

        "BTN failed" in withMockedAuditConnector { (auditService, auditConnector) =>
          when(auditConnector.sendExtendedEvent(any())(any(), any()))
            .thenReturn(Future.successful(AuditResult.Success))

          val result = auditService
            .auditBTNSubmission(
              pillarReference = pillarReference,
              accountingPeriod = AccountingPeriod(
                startDate = LocalDate.parse(apStartDate),
                endDate = LocalDate.parse(apEndDate)
              ),
              entitiesInsideAndOutsideUK = false,
              apiResponseData = models.audit.ApiResponseData(
                statusCode = responseInternalServerError,
                processingDate = responseProcessedAt,
                errorCode = Some(responseErrorCode),
                responseMessage = responseErrorMessage
              )
            )
            .futureValue

          result mustBe AuditResult.Success

          val captor = ArgCaptor[ExtendedDataEvent]
          verify(auditConnector).sendExtendedEvent(captor)(any, any)

          captor.value.auditSource mustBe "pillar2-frontend"
          captor.value.auditType mustBe "belowThresholdNotification"
          captor.value.detail mustBe Json.obj(
            "pillarReference"            -> pillarReference,
            "accountingPeriodStart"      -> apStartDate,
            "accountingPeriodEnd"        -> apEndDate,
            "entitiesInsideAndOutsideUK" -> false,
            "apiResponseData" -> Json.obj(
              "statusCode"      -> responseInternalServerError,
              "processingDate"  -> responseProcessedAt,
              "errorCode"       -> responseErrorCode,
              "responseMessage" -> responseErrorMessage
            )
          )
        }
      }

      "return Disabled when audit connector is disabled" in withMockedAuditConnector { (auditService, auditConnector) =>
        when(auditConnector.sendExtendedEvent(any())(any(), any()))
          .thenReturn(Future.successful(AuditResult.Disabled))

        auditService
          .auditBTNSubmission(
            pillarReference = pillarReference,
            accountingPeriod = AccountingPeriod(
              startDate = LocalDate.parse(apStartDate),
              endDate = LocalDate.parse(apEndDate)
            ),
            entitiesInsideAndOutsideUK = true,
            apiResponseData = models.audit.ApiResponseData(
              statusCode = responseOk,
              processingDate = responseProcessedAt,
              errorCode = None,
              responseMessage = responseSuccessMessage
            )
          )
          .futureValue mustBe AuditResult.Disabled
      }

      "return Failure when audit connector returns failure" in withMockedAuditConnector { (auditService, auditConnector) =>
        when(auditConnector.sendExtendedEvent(any())(any(), any()))
          .thenReturn(Future.successful(AuditResult.Failure("Audit failure")))

        auditService
          .auditBTNSubmission(
            pillarReference = pillarReference,
            accountingPeriod = AccountingPeriod(
              startDate = LocalDate.parse(apStartDate),
              endDate = LocalDate.parse(apEndDate)
            ),
            entitiesInsideAndOutsideUK = true,
            apiResponseData = models.audit.ApiResponseData(
              statusCode = responseOk,
              processingDate = responseProcessedAt,
              errorCode = None,
              responseMessage = responseSuccessMessage
            )
          )
          .futureValue mustBe AuditResult.Failure("Audit failure")
      }

      "propagate exceptions from audit connector" in withMockedAuditConnector { (auditService, auditConnector) =>
        when(auditConnector.sendExtendedEvent(any())(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Test exception")))

        val result = auditService
          .auditBTNSubmission(
            pillarReference = pillarReference,
            accountingPeriod = AccountingPeriod(
              startDate = LocalDate.parse(apStartDate),
              endDate = LocalDate.parse(apEndDate)
            ),
            entitiesInsideAndOutsideUK = true,
            apiResponseData = models.audit.ApiResponseData(
              statusCode = responseOk,
              processingDate = responseProcessedAt,
              errorCode = None,
              responseMessage = responseSuccessMessage
            )
          )
          .failed
          .futureValue

        result mustBe a[RuntimeException]
        result.getMessage mustBe "Test exception"

      }
    }

    "auditing an attempted BTN resubmission" should {
      "return the audit response" in forAll(
        Gen.oneOf(
          Gen.const(AuditResult.Success),
          Gen.const(AuditResult.Disabled),
          arbitrary[String].map(AuditResult.Failure(_))
        ),
        arbitrary[Boolean]
      ) { (auditResult, insideOutsideUk) =>
        withMockedAuditConnector { (auditService, auditConnector) =>
          when(auditConnector.sendExtendedEvent(any())(any(), any()))
            .thenReturn(Future.successful(auditResult))

          val result = auditService
            .auditBtnAlreadySubmitted(
              pillarReference,
              AccountingPeriod(
                LocalDate.parse(apStartDate),
                LocalDate.parse(apEndDate)
              ),
              entitiesInsideOutsideUk = insideOutsideUk
            )
            .futureValue

          result mustBe auditResult

          val captor = ArgCaptor[ExtendedDataEvent]
          verify(auditConnector).sendExtendedEvent(captor)(any, any)

          captor.value.auditSource mustBe "pillar2-frontend"
          captor.value.auditType mustBe "belowThresholdNotification"
          captor.value.detail mustBe Json.obj(
            "pillarReference"            -> pillarReference,
            "accountingPeriodStart"      -> apStartDate,
            "accountingPeriodEnd"        -> apEndDate,
            "entitiesInsideAndOutsideUK" -> insideOutsideUk
          )
        }
      }

      "propagate any failures sending the audit event" in withMockedAuditConnector { (auditService, auditConnector) =>
        val error = new Exception("failed to send event")
        when(auditConnector.sendExtendedEvent(any())(any(), any()))
          .thenReturn(Future.failed(error))

        val result = auditService
          .auditBtnAlreadySubmitted(
            pillarReference,
            AccountingPeriod(
              LocalDate.parse(apStartDate),
              LocalDate.parse(apEndDate)
            ),
            entitiesInsideOutsideUk = true
          )
          .failed
          .futureValue

        result mustBe error
      }
    }
  }
}
