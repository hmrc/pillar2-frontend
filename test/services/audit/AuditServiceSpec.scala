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
import models.{NormalMode, UserType}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.Json
import uk.gov.hmrc.play.audit.http.connector.AuditResult

import scala.concurrent.Future

class AuditServiceSpec extends SpecBase {
  val service: AuditService = app.injector.instanceOf[AuditService]

  val validGrsCreateRegistrationResponse = new GrsCreateRegistrationResponse("http://journey-start")
  val validRegisterWithIdResponse        = Json.parse(validRegistrationWithIdResponse).as[IncorporatedEntityRegistrationData]
  val validRegisterWithIdResponseForLLP  = Json.parse(validRegistrationWithIdResponseForLLP).as[PartnershipEntityRegistrationData]
  val validRepayment: RepaymentsAuditEvent = Json.parse(validRepaymentDetails).as[RepaymentsAuditEvent]

  val serviceName = ServiceName(
    OptServiceName("Report Pillar 2 top-up taxes"),
    OptServiceName("Report Pillar 2 top-up taxes")
  )
  val requestData = IncorporatedEntityCreateRegistrationRequest(
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

  }
}
