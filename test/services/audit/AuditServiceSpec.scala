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
import models.grs.{GrsCreateRegistrationResponse, OptServiceName, ServiceName}
import models.registration.{IncorporatedEntityCreateRegistrationRequest, IncorporatedEntityRegistrationData, PartnershipEntityRegistrationData}
import models.subscription.{AccountingPeriod, NewFilingMemberDetail}
import models.{NormalMode, UserType}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.mockito.captor.ArgCaptor
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.running
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import java.time.LocalDate
import scala.concurrent.Future

class AuditServiceSpec extends SpecBase {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder().configure(Map("auditing.enabled" -> "true")).build()

  val service: AuditService = app.injector.instanceOf[AuditService]

  val validGrsCreateRegistrationResponse = new GrsCreateRegistrationResponse("http://journey-start")
  val validRegisterWithIdResponse: IncorporatedEntityRegistrationData =
    Json.parse(validRegistrationWithIdResponse).as[IncorporatedEntityRegistrationData]
  val validRegisterWithIdResponseForLLP: PartnershipEntityRegistrationData =
    Json.parse(validRegistrationWithIdResponseForLLP).as[PartnershipEntityRegistrationData]
  val validReplaceFilingMemberNoId: NewFilingMemberDetail = Json.parse(validReplaceFilingMember).as[NewFilingMemberDetail]
  val validRepayment:               RepaymentsAuditEvent  = Json.parse(validRepaymentDetails).as[RepaymentsAuditEvent]

  val serviceName: ServiceName = ServiceName(OptServiceName("Report Pillar 2 Top-up Taxes"))
  val requestData: IncorporatedEntityCreateRegistrationRequest = IncorporatedEntityCreateRegistrationRequest(
    continueUrl =
      s"http://localhost:10050/report-pillar2-top-up-taxes/grs-return/${NormalMode.toString.toLowerCase}/${UserType.Upe.value.toLowerCase}",
    businessVerificationCheck = false,
    optServiceName = Some(serviceName.en.optServiceName),
    deskProServiceId = "pillar2-frontend",
    signOutUrl = "http://localhost:9553/bas-gateway/sign-out-without-state",
    accessibilityUrl = "/accessibility-statement/pillar2-frontend",
    labels = serviceName
  )

  "AuditService" when {
    "successful for auditGrsReturnForLimitedCompany" in {
      when(mockAuditConnector.sendExtendedEvent(any())(any(), any()))
        .thenReturn(Future.successful(AuditResult.Success))

      val result = service.auditGrsReturnForLimitedCompany(validRegisterWithIdResponse).futureValue
      result mustBe AuditResult.Success

    }

    "successful for auditGrsReturnForLLP" in {
      when(mockAuditConnector.sendExtendedEvent(any())(any(), any()))
        .thenReturn(Future.successful(AuditResult.Success))

      val result = service.auditGrsReturnForLLP(validRegisterWithIdResponseForLLP).futureValue
      result mustBe AuditResult.Success

    }

    "successful for auditGrsReturnNfmForLimitedCompany" in {
      when(mockAuditConnector.sendExtendedEvent(any())(any(), any()))
        .thenReturn(Future.successful(AuditResult.Success))

      val result = service.auditGrsReturnNfmForLimitedCompany(validRegisterWithIdResponse).futureValue
      result mustBe AuditResult.Success

    }

    "successful for auditGrsReturnNfmForLLP" in {
      when(mockAuditConnector.sendExtendedEvent(any())(any(), any()))
        .thenReturn(Future.successful(AuditResult.Success))

      val result = service.auditGrsReturnNfmForLLP(validRegisterWithIdResponseForLLP).futureValue
      result mustBe AuditResult.Success

    }

    "successful for auditRepayments" in {
      when(mockAuditConnector.sendExtendedEvent(any())(any(), any()))
        .thenReturn(Future.successful(AuditResult.Success))

      val result = service.auditRepayments(validRepayment).futureValue
      result mustBe AuditResult.Success
    }

    "successful for auditReplaceFilingMember" in {
      when(mockAuditConnector.sendExtendedEvent(any())(any(), any()))
        .thenReturn(Future.successful(AuditResult.Success))

      val result = service.auditReplaceFilingMember(validReplaceFilingMemberNoId).futureValue
      result mustBe AuditResult.Success
    }

    "auditing a BTN submission" should {

      val application: Application = applicationBuilder()
        .overrides(
          bind[AuditConnector].toInstance(mockAuditConnector)
        )
        .build()

      val pillarReference             = "PLR1234567890"
      val apStartDate                 = "2024-03-20"
      val apEndDate                   = "2025-03-20"
      val responseOk                  = 200
      val responseInternalServerError = 500
      val responseProcessedAt         = "2024-03-20T07:32:03Z"
      val responseSuccessMessage      = "Success"
      val responseErrorCode           = "InternalIssueError"
      val responseErrorMessage        = "Failure"

      "return Success when audit call is successful" when {

        "BTN was successful" in running(application) {
          when(mockAuditConnector.sendExtendedEvent(any())(any(), any()))
            .thenReturn(Future.successful(AuditResult.Success))

          val service = application.injector.instanceOf[AuditService]

          val result = service
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
            )(hc)
            .futureValue

          result mustBe AuditResult.Success

          val captor = ArgCaptor[ExtendedDataEvent]
          verify(mockAuditConnector).sendExtendedEvent(captor)(any, any)

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

        "BTN failed" in running(application) {
          when(mockAuditConnector.sendExtendedEvent(any())(any(), any()))
            .thenReturn(Future.successful(AuditResult.Success))

          val service = application.injector.instanceOf[AuditService]

          val result = service
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
            )(hc)
            .futureValue

          result mustBe AuditResult.Success

          val captor = ArgCaptor[ExtendedDataEvent]
          verify(mockAuditConnector).sendExtendedEvent(captor)(any, any)

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

      "return Disabled when audit connector is disabled" in {
        running(application) {
          when(mockAuditConnector.sendExtendedEvent(any())(any(), any()))
            .thenReturn(Future.successful(AuditResult.Disabled))

          val service = application.injector.instanceOf[AuditService]
          val result = service
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
            )(hc)
            .futureValue

          result mustBe AuditResult.Disabled
        }
      }

      "return Failure when audit connector returns failure" in {
        running(application) {
          when(mockAuditConnector.sendExtendedEvent(any())(any(), any()))
            .thenReturn(Future.successful(AuditResult.Failure("Audit failure")))

          val service = application.injector.instanceOf[AuditService]
          val result = service
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
            )(hc)
            .futureValue

          result mustBe AuditResult.Failure("Audit failure")
        }
      }

      "propagate exceptions from audit connector" in {
        running(application) {
          when(mockAuditConnector.sendExtendedEvent(any())(any(), any()))
            .thenReturn(Future.failed(new RuntimeException("Test exception")))

          val service = application.injector.instanceOf[AuditService]
          val resultFuture = service.auditBTNSubmission(
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
          )(hc)

          whenReady(resultFuture.failed) { exception =>
            exception mustBe a[RuntimeException]
            exception.getMessage mustBe "Test exception"
          }

        }
      }
    }
  }
}
